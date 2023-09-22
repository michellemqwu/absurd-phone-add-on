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
import androidx.activity.ComponentActivity
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
var serialDevice : UsbSerialDevice? = null

class NotificationListener : NotificationListenerService() {
    override fun onCreate() {
        super.onCreate()
        // Your initialization code here
    }

    override fun onDestroy() {
        super.onDestroy()
        // Your cleanup code here
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        sbn?.let { notification ->
            val notificationText = notification.notification?.extras?.getCharSequence("android.text").toString()
            serialDevice?.write(notificationText.toByteArray())
            // Log the notification text to the console
            Log.d("NotificationListener", "Received Notification: $notificationText")

        }
    }
}

class MainActivity : ComponentActivity() {
    lateinit var usbManager : UsbManager
    var device : UsbDevice? = null
    var deviceConnection:UsbDeviceConnection? = null
    val ACTION_USB_PERMISSION = "permission"
    override fun onCreate(savedInstanceState: Bundle?) {
        var firstNameFocused : Boolean = false
        var lastNameFocused : Boolean = false

        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED)
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)
        registerReceiver(broadcastReceiver, filter)

//        val firstNameElement = findViewById<EditText>(R.id.firstName)
//        firstNameElement.setOnFocusChangeListener{_, hasFocus:Boolean ->
//            if (hasFocus && !firstNameFocused){
//                firstNameElement.text.clear()
//                firstNameFocused=true
//            }
//        }

//        val lastNameElement = findViewById<EditText>(R.id.lastName)
//        lastNameElement.setOnFocusChangeListener{_, hasFocus:Boolean ->
//            if (hasFocus && !lastNameFocused){
//                lastNameElement.text.clear()
//                lastNameFocused=true
//            }
//        }

        val btn = findViewById<Button>(R.id.button)
        //val connectBtn = findViewById<Button>(R.id.connectBtn)
        btn.setOnClickListener{
            connect()
        }

//        btn.setOnClickListener{
//            Toast.makeText(this,firstNameElement.text.toString()+" " + lastNameElement.text.toString(),Toast.LENGTH_SHORT).show()
//        }

    }

    private fun connect(){
        try{
            Log.i("serial", "HELLO IM IN CONNECT")
            val usbDevices : HashMap<String, UsbDevice>? = usbManager.deviceList
            if(!usbDevices?.isEmpty()!!){
                var keep = true
                usbDevices.forEach{entry->

                    device = entry.value
                    val deviceVendorID : Int? = device?.vendorId
                    Log.i("serial",""+deviceVendorID)
//                    if(deviceVendorID==1027){

                    val intent:PendingIntent = PendingIntent.getBroadcast(this,0,Intent(ACTION_USB_PERMISSION), FLAG_MUTABLE)
                    usbManager.requestPermission(device,intent)
                    keep = false
                    Log.i("serial","Connection successful")

//                    } else {
//                        deviceConnection = null
//                        device = null

//                    }
                }

                if(!keep) {
                    return
                }
            } else {
                Log.i("serial","nothing connected")
            }
        } catch (e:Exception){
            //findViewById<TextView>(R.id.textView).setText(e.toString())
        }
    }
    private fun sendData(msg:String){
        serialDevice?.write(msg.toByteArray())
        Log.i("serial",""+msg.toByteArray())
    }
    private fun disconnect(){
        serialDevice?.close()
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

