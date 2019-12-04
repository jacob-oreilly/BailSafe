package com.example.bailsafe

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Chronometer
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String

    }

    lateinit var textValue: String

    var switchReleased: Boolean by Delegates.observable(false) {property, oldValue, newValue ->
        if (switchReleased) {
            Toast.makeText(this@MainActivity, switchReleased.toString(),Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(this@MainActivity, switchReleased.toString(),Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var address: String? = intent.getStringExtra(SelectDeviceActivity.EXTRA_ADDRESS)
        if (address !== null) {
            m_address = address
            Toast.makeText(this, m_address, Toast.LENGTH_SHORT).show()
            ConnectToDevice(this).execute()

        }






        val imageButton = findViewById<ImageButton>(R.id.bikeButton)
        imageButton?.setOnClickListener {
            Toast.makeText(this@MainActivity, "Button has been clicked", Toast.LENGTH_LONG).show()
            switchReleased = !switchReleased
            Toast.makeText(this@MainActivity, textValue, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.settings_action){
            Toast.makeText(this,"Setting clicked",Toast.LENGTH_SHORT).show()
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
                        try {
                            val available = m_bluetoothSocket!!.inputStream.available()
                            val bytes = ByteArray(available)
                            Log.i("server", "Reading")
                            m_bluetoothSocket!!.inputStream.read(bytes, 0, available)
                            val text= String(bytes)
                            this@MainActivity.setValue(text)
                            Log.i("server", "Message received")
                            Log.i("server", text)


                        } catch (e: Exception) {
                            Log.e("client", "Cannot read data", e)
                        } finally {
                            m_bluetoothSocket!!.inputStream.close()
//                            m_bluetoothSocket!!.close()
                        }
                    }

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
            m_progress.dismiss()
        }
    }

    // Method to get days hours minutes seconds from milliseconds
    private fun timeString(millisUntilFinished:Long):String{
        var millisUntilFinished:Long = millisUntilFinished
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
            minutes,seconds
        )
    }

    fun setValue(text: String) {
        textValue = text
    }
}

