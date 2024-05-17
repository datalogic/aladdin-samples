package com.datalogic.aladdinsample.websocketclient

import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.datalogic.aladdinsample.websocketclient.databinding.ActivityMainBinding
import com.datalogic.aladdinsample.websocketclient.databinding.PopupWebsocketSettingsBinding
import com.datalogic.aladdinsample.websocketclient.databinding.PopupWebsocketSettingsErrorBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.*
import java.net.InetAddress
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private val tag = "AladdinWebsocketClient"
    private lateinit var binding: ActivityMainBinding
    private lateinit var socket: Socket
    private var SERVER_PORT = 8000
    private var isConnectedToServer = false
    private var isDisconnectedToServer = false
    private lateinit var portChangeDialog: Dialog
    private lateinit var portChangeErrorDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityMainBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        val view = binding.root
        setContentView(view)
        initView()
    }

    private fun initView() {
        binding.btnConnectToServer.setOnClickListener {
            Log.d(tag, "connectToServer clicked")
            showPortChangeDialog()
        }
        binding.btnLatestBarcode.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                askForLastBarcode()
            }
        }
        binding.btnScannerState.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                askForConnectionState()
            }
        }
        binding.btnDisconnect.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                disconnectClient()
            }
        }
    }

    private fun connectToServer() {
        Log.d(tag, "connectToServer")
        try {
            val serverAdd: InetAddress = InetAddress.getByName("127.0.0.1")
            socket = Socket(serverAdd, SERVER_PORT)
            while (!isConnectedToServer) {
                if (socket.isConnected) {
                    Log.d(tag, "client socket is connected to the server.")
                    isConnectedToServer = true
                    isDisconnectedToServer = false
                    runOnUiThread {
                        Toast.makeText(this, "connected to server...", Toast.LENGTH_SHORT).show()
                        binding.btnConnectToServer.isEnabled = false
                        binding.btnScannerState.isEnabled = true
                        binding.btnLatestBarcode.isEnabled = true
                        binding.btnDisconnect.isEnabled = true
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        while (!socket.isClosed) {
                            Log.d(tag, "connected: ${socket.isConnected}")
                            handleClientCommunication()
                        }
                    }
                } else {
                    Log.d(tag, "client socket is not connected to the server.")
                }
            }
        } catch (e: IOException) {
            runOnUiThread {
                Toast.makeText(this, "failed to connect to server...", Toast.LENGTH_SHORT).show()
            }
            Log.e(tag, "caught error while connecting server socket: ${e.printStackTrace()}")
        }
    }

    private fun handleClientCommunication() {
        Log.d(tag, "handleClientCommunication, is socket closed: ${socket.isClosed}")
        if (::socket.isInitialized) {
            try {
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))
                val message = input.readLine()
                Log.d(tag, "message from server: $message")
                if (message != null && message.isNotEmpty()) {
                    val jsonObj = JSONObject(message)
                    val eventType = jsonObj.getString("event_type")
                    if (eventType.isNotEmpty() && eventType.equals("scan")) {
                        if (jsonObj.getString("scan_code").isEmpty()) {
                            Log.d(tag, "scan code is empty")
                            runOnUiThread {
                                Toast.makeText(this, "No scan event recorded.", Toast.LENGTH_LONG)
                                    .show()
                            }
                            return
                        }
                    }
                    runOnUiThread {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    tag, "caught exception while reading data from server: ${e.printStackTrace()}"
                )
                runOnUiThread {
                    Toast.makeText(this, "disconnected from server...", Toast.LENGTH_SHORT).show()
                    binding.btnConnectToServer.isEnabled = true
                    binding.btnScannerState.isEnabled = false
                    binding.btnLatestBarcode.isEnabled = false
                    binding.btnDisconnect.isEnabled = false
                }
            }
        } else {
            Log.d(
                tag,
                "handleClientCommunication: client socket is not initialized or socket is not connected."
            )
        }
    }

    private fun askForLastBarcode() {
        Log.d(tag, "askForLastBarcode")
        try {
            if (::socket.isInitialized) {
                val out = PrintWriter(
                    BufferedWriter(
                        OutputStreamWriter(socket.getOutputStream())
                    ), true
                )
                out.println(createJsonObjForLastBarcode())
            }
        } catch (e: Exception) {
            Log.e(tag, "caught error while asking for last barcode: ${e.printStackTrace()}")
        }
    }

    private fun askForConnectionState() {
        Log.d(tag, "askForConnectionState")
        try {
            if (::socket.isInitialized) {
                val out = PrintWriter(
                    BufferedWriter(
                        OutputStreamWriter(socket.getOutputStream())
                    ), true
                )
                out.println(createJsonObjForConnectionState())
            }
        } catch (e: Exception) {
            Log.e(tag, "caught error while asking for connection state: ${e.printStackTrace()}")
        }
    }

    private fun disconnectClient() {
        Log.d(tag, "disconnectClient")
        if (::socket.isInitialized && socket.isConnected) {
            socket.close()
            while (!isDisconnectedToServer) {
                if (socket.isClosed) {
                    Log.d(tag, "client socket is disconnected from server.")
                    isDisconnectedToServer = true
                    isConnectedToServer = false
                    runOnUiThread {
                        Toast.makeText(this, "disconnected from server...", Toast.LENGTH_SHORT)
                            .show()
                        binding.btnConnectToServer.isEnabled = true
                        binding.btnScannerState.isEnabled = false
                        binding.btnLatestBarcode.isEnabled = false
                        binding.btnDisconnect.isEnabled = false
                    }
                } else {
                    Log.d(tag, "client socket is not disconnected from server.")
                }
            }
        } else {
            Log.d(tag, "client socket is not initialized or socket is not connected.")
        }
    }

    private fun createJsonObjForLastBarcode(): JSONObject {
        val jsonObj = JSONObject()
        jsonObj.put("event_type", "lastBarcode")
        jsonObj.put("event_send_time", System.currentTimeMillis())
        Log.d(tag, "createJsonObjForLastBarcode: $jsonObj")
        return jsonObj
    }

    private fun createJsonObjForConnectionState(): JSONObject {
        val jsonObj = JSONObject()
        jsonObj.put("event_type", "connectionState")
        jsonObj.put("event_send_time", System.currentTimeMillis())
        Log.d(tag, "createJsonObjForConnectionState: $jsonObj")
        return jsonObj
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectClient()
        if (::portChangeDialog.isInitialized && portChangeDialog.isShowing) {
            portChangeDialog.dismiss()
        }
        if (::portChangeErrorDialog.isInitialized && portChangeErrorDialog.isShowing) {
            portChangeErrorDialog.dismiss()
        }
    }

    private fun showPortChangeDialog() {
        val webSocketSettingsBinding = PopupWebsocketSettingsBinding.inflate(layoutInflater)
        portChangeDialog = Dialog(this, R.style.CustomAlertDialog)
        portChangeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        portChangeDialog.setCancelable(false)
        portChangeDialog.setContentView(webSocketSettingsBinding.root)
        webSocketSettingsBinding.etPortEnter.setText(getString(R.string.default_port))
        webSocketSettingsBinding.okBtn.setOnClickListener {
            try {
                val portInputStr = webSocketSettingsBinding.etPortEnter.text.trim().toString()
                if (portInputStr.isNotEmpty() && portInputStr.toInt() in 1024..65535) {
                    portChangeDialog.dismiss()
                    SERVER_PORT = portInputStr.toInt()
                    CoroutineScope(Dispatchers.IO).launch {
                        connectToServer()
                    }
                } else {
                    showPortChangeErrorDialog("Please enter port address between 1024 and 65535.")
                }
            } catch (e: NumberFormatException) {
                Log.d(tag, "number format is not correct.")
                showPortChangeErrorDialog("Please enter port address in number format.")
            }
        }
        portChangeDialog.show()
        val displayMetrics = DisplayMetrics()
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        val displayWidth = displayMetrics.widthPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(portChangeDialog.window?.attributes);
        val dialogWindowWidth = (displayWidth * 0.9f).toInt()
        layoutParams.width = dialogWindowWidth;
        portChangeDialog.window?.attributes = layoutParams;
    }

    private fun showPortChangeErrorDialog(errorReason: String) {
        portChangeErrorDialog = Dialog(this, R.style.CustomAlertDialog)
        val webSocketSettingsErrorBinding =
            PopupWebsocketSettingsErrorBinding.inflate(layoutInflater)
        portChangeErrorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        portChangeErrorDialog.setCancelable(false)
        portChangeErrorDialog.setContentView(webSocketSettingsErrorBinding.root)
        webSocketSettingsErrorBinding.tvPortAddChangeError.text = errorReason
        webSocketSettingsErrorBinding.okBtn.setOnClickListener {
            portChangeErrorDialog.dismiss()
        }
        portChangeErrorDialog.show()
        val displayMetrics = DisplayMetrics()
        this.windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        val displayWidth = displayMetrics.widthPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(portChangeErrorDialog.window?.attributes);
        val dialogWindowWidth = (displayWidth * 0.9f).toInt()
        layoutParams.width = dialogWindowWidth;
        portChangeErrorDialog.window?.attributes = layoutParams;
    }
}