package com.example.myapplication

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface

var serialDevice : UsbSerialDevice? = null
var numNotification = 0
var textBox : TextView? = null
var displayString : String = ""

class NotificationListener : NotificationListenerService() {
    override fun onCreate() {
        super.onCreate()
        Log.i("serial","hi this is notification listener")
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onListenerConnected() {
        numNotification = 0
        super.onListenerConnected()
        numNotification = activeNotifications.size
        sendSerialToUsb(numNotification.toString())

        Log.i("serial","notification listener connected, current notifications: $numNotification")

        displayString = "notification listener connected, current notifications: $numNotification"
        textBox?.text = displayString
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        numNotification = activeNotifications.size
        sendSerialToUsb(numNotification.toString())

        displayString = "new notification RECEIVED, total number of notifications: $numNotification"
        textBox?.text = displayString
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        numNotification = activeNotifications.size
        sendSerialToUsb(numNotification.toString())
        displayString = "notification REMOVED, total number of notifications: $numNotification"
        textBox?.text = displayString
    }

    private fun sendSerialToUsb(data: String) {
        Log.i("Serial","sending to serial")
        serialDevice?.write(data.toByteArray())
    }
}

class MainActivity : ComponentActivity() {
    lateinit var usbManager : UsbManager
    var device : UsbDevice? = null
    var deviceConnection:UsbDeviceConnection? = null
    val ACTION_USB_PERMISSION = "permission"
    var connected = false
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        Log.i("seiral", "hi")

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED)
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)
        registerReceiver(broadcastReceiver, filter)

        val btn = findViewById<Button>(R.id.button)
        btn.setOnClickListener{
            connect()
        }

        val btnDisconnect = findViewById<Button>(R.id.disconnectButton)
        btnDisconnect.setOnClickListener{
            disconnect()
        }
        textBox = findViewById<TextView>(R.id.textOutput)

    }

    private fun connect(){
        try{
            val usbDevices : HashMap<String, UsbDevice>? = usbManager.deviceList
            val textElement = findViewById<TextView>(R.id.text)
            if(!usbDevices?.isEmpty()!!){
                var keep = true
                usbDevices.forEach{entry->

                    device = entry.value
                    val deviceVendorID : Int? = device?.vendorId
                    Log.i("serial",""+deviceVendorID)

                    val intent:PendingIntent = PendingIntent.getBroadcast(this,0,Intent(ACTION_USB_PERMISSION), FLAG_MUTABLE)
                    usbManager.requestPermission(device,intent)
                    keep = false
                    connected = true
                    textElement.text = "connection successful"
                    Log.i("serial","Connection successful")

                }

                if(!keep) {
                    return
                }
            } else {
                Log.i("serial","nothing connected")
                textElement.setText("nothing connected")
            }
        } catch (e:Exception){
            findViewById<TextView>(R.id.text).setText(e.toString())
        }
    }
    private fun sendData(msg:String){
        serialDevice?.write(msg.toByteArray())
        Log.i("serial",""+msg.toByteArray())
    }
    private fun disconnect(){
        serialDevice?.close()
        val textElement = findViewById<TextView>(R.id.text)
        textElement.setText("disconnected")
    }
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action!! == ACTION_USB_PERMISSION ){
                val granted: Boolean = intent.extras!!.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)
                if(granted){
                    deviceConnection = usbManager.openDevice(device)
                    serialDevice = UsbSerialDevice.createUsbSerialDevice(device,deviceConnection)
                    if(serialDevice != null){
                        if(serialDevice!!.open()){
                            serialDevice!!.setBaudRate(9600)
                            serialDevice!!.setDataBits(UsbSerialInterface.DATA_BITS_8)
                            serialDevice!!.setStopBits(UsbSerialInterface.STOP_BITS_1)
                            serialDevice!!.setParity(UsbSerialInterface.PARITY_NONE)
                            serialDevice!!.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                        } else {
                            Log.i("serial","port is not open")
                        }
                    }else {
                        Log.i("serial","port is null")
                    }
                }else {
                    Log.i("serial","no permission")
                }
            } else if (intent?.action!! == UsbManager.ACTION_USB_ACCESSORY_ATTACHED){
                connect()
            } else if (intent?.action!! == UsbManager.ACTION_USB_ACCESSORY_DETACHED){
                disconnect()
            }
        }
    }
}

