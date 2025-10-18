package com.example.dogetransfer.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log

class WiFiDirectBroadcastReceiver(
    private val manager: WifiP2pManager?,
    private val channel: WifiP2pManager.Channel?,
    private val listener: WiFiDirectListener
) : BroadcastReceiver() {

    companion object {
        private const val TAG = "WiFiDirectReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                listener.onWiFiP2pStateChanged(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
                Log.d(TAG, "WiFi P2P state changed: ${state == WifiP2pManager.WIFI_P2P_STATE_ENABLED}")
            }
            
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                manager?.requestPeers(channel) { peers ->
                    listener.onPeersChanged(peers)
                    Log.d(TAG, "Peers changed: ${peers.deviceList.size} devices")
                }
            }
            
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                
                if (networkInfo?.isConnected == true) {
                    manager?.requestConnectionInfo(channel) { info ->
                        listener.onConnectionInfoAvailable(info)
                        Log.d(TAG, "Connection established. Group Owner: ${info?.isGroupOwner}")
                    }
                } else {
                    listener.onDisconnected()
                    Log.d(TAG, "Disconnected")
                }
            }
            
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                val device = intent.getParcelableExtra<android.net.wifi.p2p.WifiP2pDevice>(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE
                )
                listener.onThisDeviceChanged(device)
                Log.d(TAG, "This device changed: ${device?.deviceName}")
            }
        }
    }
} 