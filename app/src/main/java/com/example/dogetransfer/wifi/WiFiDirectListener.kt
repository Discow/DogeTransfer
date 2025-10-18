package com.example.dogetransfer.wifi

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo

interface WiFiDirectListener {
    fun onWiFiP2pStateChanged(isEnabled: Boolean)
    fun onPeersChanged(peers: WifiP2pDeviceList)
    fun onConnectionInfoAvailable(info: WifiP2pInfo?)
    fun onDisconnected()
    fun onThisDeviceChanged(device: WifiP2pDevice?)
} 