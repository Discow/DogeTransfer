package com.example.dogetransfer.wifi

import android.content.Context
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import com.example.dogetransfer.model.DeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WiFiDirectManager(private val context: Context) : WiFiDirectListener {

    companion object {
        private const val TAG = "WiFiDirectManager"
    }

    private val wifiP2pManager: WifiP2pManager? = 
        context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
    
    private var channel: WifiP2pManager.Channel? = null
    private var receiver: WiFiDirectBroadcastReceiver? = null
    
    private val _isWifiP2pEnabled = MutableStateFlow(false)
    val isWifiP2pEnabled: StateFlow<Boolean> = _isWifiP2pEnabled.asStateFlow()
    
    private val _peers = MutableStateFlow<List<DeviceInfo>>(emptyList())
    val peers: StateFlow<List<DeviceInfo>> = _peers.asStateFlow()
    
    private val _connectionInfo = MutableStateFlow<WifiP2pInfo?>(null)
    val connectionInfo: StateFlow<WifiP2pInfo?> = _connectionInfo.asStateFlow()
    
    private val _thisDevice = MutableStateFlow<DeviceInfo?>(null)
    val thisDevice: StateFlow<DeviceInfo?> = _thisDevice.asStateFlow()
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    fun initialize() {
        channel = wifiP2pManager?.initialize(context, context.mainLooper, null)
        receiver = WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this)
    }

    fun register() {
        receiver?.let { 
            context.registerReceiver(it, intentFilter)
        }
    }

    fun unregister() {
        try {
            receiver?.let {
                context.unregisterReceiver(it)
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Receiver not registered", e)
        }
    }

    fun discoverPeers(
        onSuccess: () -> Unit = {},
        onFailure: (Int) -> Unit = {}
    ) {
        wifiP2pManager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Peer discovery initiated")
                onSuccess()
            }

            override fun onFailure(reasonCode: Int) {
                Log.e(TAG, "Peer discovery failed: $reasonCode")
                onFailure(reasonCode)
            }
        })
    }

    fun connectToDevice(
        device: DeviceInfo,
        onSuccess: () -> Unit = {},
        onFailure: (Int) -> Unit = {}
    ) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }

        wifiP2pManager?.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Connection initiated to ${device.deviceName}")
                onSuccess()
            }

            override fun onFailure(reasonCode: Int) {
                Log.e(TAG, "Connection failed: $reasonCode")
                onFailure(reasonCode)
            }
        })
    }

    fun disconnect(
        onSuccess: () -> Unit = {},
        onFailure: (Int) -> Unit = {}
    ) {
        wifiP2pManager?.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Disconnected successfully")
                _isConnected.value = false
                _connectionInfo.value = null
                onSuccess()
            }

            override fun onFailure(reasonCode: Int) {
                Log.e(TAG, "Disconnect failed: $reasonCode")
                onFailure(reasonCode)
            }
        })
    }

    fun cancelConnect() {
        wifiP2pManager?.cancelConnect(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Connection cancelled")
            }

            override fun onFailure(reasonCode: Int) {
                Log.e(TAG, "Cancel connection failed: $reasonCode")
            }
        })
    }

    override fun onWiFiP2pStateChanged(isEnabled: Boolean) {
        _isWifiP2pEnabled.value = isEnabled
        if (!isEnabled) {
            _peers.value = emptyList()
            _isConnected.value = false
        }
    }

    override fun onPeersChanged(peers: WifiP2pDeviceList) {
        val deviceList = peers.deviceList.map { DeviceInfo.from(it) }
        _peers.value = deviceList
    }

    override fun onConnectionInfoAvailable(info: WifiP2pInfo?) {
        _connectionInfo.value = info
        _isConnected.value = info != null
    }

    override fun onDisconnected() {
        _isConnected.value = false
        _connectionInfo.value = null
    }

    override fun onThisDeviceChanged(device: WifiP2pDevice?) {
        device?.let {
            _thisDevice.value = DeviceInfo.from(it)
        }
    }
} 