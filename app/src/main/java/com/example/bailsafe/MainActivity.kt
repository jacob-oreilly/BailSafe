package com.example.bailsafe

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.suspendCoroutine
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
        lateinit var clock: TextView
        var rideStart: Boolean = false

    }

    lateinit var textValue: String
    val timer = object : CountDownTimer(9000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            clock = findViewById<TextView>(R.id.timer)
            clock.text = timeString(millisUntilFinished)

        }

        override fun onFinish() {
            print("finished")
            clock.text = timeString(0)
            sendSMS()
        }
    }

    var switchReleased: Boolean by Delegates.observable(false) { property, oldValue, newValue ->
        if (switchReleased && rideStart) {
//            Toast.makeText(this@MainActivity, switchReleased.toString(),Toast.LENGTH_SHORT).show()
            clock = findViewById(R.id.timer)
            clock.text = timeString(9000)
            timer.cancel()
            clock.requestLayout()
        } else if (!switchReleased && rideStart) {
//            Toast.makeText(this@MainActivity, switchReleased.toString(),Toast.LENGTH_SHORT).show()
            timer.start()
        }
    }

    private var hasGPS: Boolean = false
    private var hasNetwork: Boolean = false
    private var locationGPS: Location? = null
    private var locationNetwork: Location? = null
    private var latLon: String = ""
    private val requestSendSMS: Int = 2
    lateinit var locationManager: LocationManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != packageManager.) {
//
//        }


        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION), 2)
        var address: String? = intent.getStringExtra(SelectDeviceActivity.EXTRA_ADDRESS)
        if (address !== null) {
            m_address = address
            Toast.makeText(this, m_address, Toast.LENGTH_SHORT).show()
            ConnectToDevice(this).execute()
        }

        clock = findViewById<TextView>(R.id.timer)
        val imageButton = findViewById<ImageButton>(R.id.bikeButton)
        val buttonText = findViewById<TextView>(R.id.startRide)

        getLocation()
        imageButton?.setOnClickListener {
            Log.i("LatLon", latLon)
            clock.text = timeString(9000)
            rideStart = !rideStart
            if (rideStart) {
                timer.start()
                buttonText.text = "Stop Ride"

            }else {
                timer.cancel()
                clock.text = timeString(0)
                buttonText.text = "Start Ride"
            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        if (requestCode == requestSendSMS) {
//            sendSMS()
//        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun sendSMS() {
        val number = "2182321749"
        val text = "I have fallen and I can't get up! location: " + latLon
        Toast.makeText(this, text, Toast.LENGTH_SHORT)
        SmsManager.getDefault().sendTextMessage(number, null, text, null, null)
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGPS || hasNetwork) {
            if (hasGPS) {
                Log.i("CodeAndroidLocation", "hasGPS")
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        if(location != null) {
                            locationGPS = location
                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                    }

                    override fun onProviderEnabled(provider: String?) {

                    }

                    override fun onProviderDisabled(provider: String?) {

                    }

                })

                val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGpsLocation != null) {
                    locationGPS = localGpsLocation
                }
            }

            if (hasNetwork) {
                Log.i("CodeAndroidLocation", "hasGPS")
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0f, object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        if(location != null) {
                            locationNetwork = location
                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

                    }

                    override fun onProviderEnabled(provider: String?) {

                    }

                    override fun onProviderDisabled(provider: String?) {

                    }

                })

                val localNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localNetworkLocation != null) {
                    locationNetwork = localNetworkLocation
                }
            }

            if(locationGPS != null || locationNetwork != null) {
//                if(locationGPS!!.accuracy > locationNetwork!!.accuracy) {
                    latLon = (locationNetwork!!.latitude).toString() + " " + (locationNetwork!!.longitude).toString()
                    Toast.makeText(this, (locationNetwork!!.latitude + locationNetwork!!.longitude).toString(), Toast.LENGTH_LONG)

//                }else {
//                    latLon = (locationGPS!!.latitude + locationGPS!!.longitude).toString()
//                    Toast.makeText(this, (locationGPS!!.latitude + locationGPS!!.longitude).toString(), Toast.LENGTH_LONG)
//                }
            }

        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.settings_action) {
            Toast.makeText(this, "Setting clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SelectDeviceActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    inner class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Connecting...", "please wait")
        }

        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                    if (m_bluetoothSocket != null) {
                        m_progress.dismiss()
                    }
                    CoroutineScope(Dispatchers.IO).launch {  this@MainActivity.listenForPress() }

                }
            } catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.i("data", "couldn't connect")
            } else {
                m_isConnected = true
            }
        }


    }

    // Method to get days hours minutes seconds from milliseconds
    private fun timeString(millisUntilFinished: Long): String {
        var millisUntilFinished: Long = millisUntilFinished
        val days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
        millisUntilFinished -= TimeUnit.DAYS.toMillis(days)

        val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
        millisUntilFinished -= TimeUnit.HOURS.toMillis(hours)

        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
        millisUntilFinished -= TimeUnit.MINUTES.toMillis(minutes)

        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)

        // Format the string
        return String.format(
            Locale.getDefault(),
            "%02d:%02d",
            minutes, seconds
        )
    }

    fun setValue(text: String) {
        textValue = text
    }

    fun setTimer(millisUntilFinished: Long) {
        clock.setText(timeString(900000))
    }

    suspend fun listenForPress()
    {
        return suspendCoroutine {
            var count0 = 0
            var count1 = 0
            while (m_bluetoothSocket != null) {

                try {

                    val available = m_bluetoothSocket!!.inputStream.available()
                    val bytes = ByteArray(available)
                    m_bluetoothSocket!!.inputStream.read(bytes, 0, available)
                    val text = String(bytes)
                    if (text == "0") {
                        count0++
                        this@MainActivity.runOnUiThread(java.lang.Runnable {
                            setValue(text)
                        })

                        Log.i("server", text)
                        if (count0 <= 1) {
                            switchReleased = false
                            if (rideStart) {
                                this@MainActivity.runOnUiThread(java.lang.Runnable {
                                    timer.start()
                                    getLocation()
                                })

                            }
                        }
                        count1 = 0
                    } else if (text == "1") {
                        count1++
                        this@MainActivity.runOnUiThread(java.lang.Runnable {
                            setValue(text)
                        })
                        Log.i("server", text)

                        if (count1 <= 1) {

                            this@MainActivity.runOnUiThread(java.lang.Runnable {
                                switchReleased = true
                                timer.cancel()
                            })
                        }
                        count0 = 0
                    }
                } catch (e: Exception) {
                    Log.e("client", "Cannot read data", e)
                } finally {
//                            m_bluetoothSocket!!.inputStream.close()
//                            m_bluetoothSocket!!.close()
                }
            }
        }
    }
}

