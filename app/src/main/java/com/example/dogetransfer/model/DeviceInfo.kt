package com.example.dogetransfer.model

import android.net.wifi.p2p.WifiP2pDevice

data class DeviceInfo(
    val deviceName: String,
    val deviceAddress: String,
    val status: Int,
    val isGroupOwner: Boolean = false
) {
    companion object {
        fun from(device: WifiP2pDevice): DeviceInfo {
            return DeviceInfo(
                deviceName = device.deviceName ?: "Unknown Device",
                deviceAddress = device.deviceAddress,
                status = device.status
            )
        }
    }
    
    fun getStatusText(): String {
        return when (status) {
            WifiP2pDevice.CONNECTED -> "已连接"
            WifiP2pDevice.INVITED -> "邀请中"
            WifiP2pDevice.FAILED -> "连接失败"
            WifiP2pDevice.AVAILABLE -> "可用"
            WifiP2pDevice.UNAVAILABLE -> "不可用"
            else -> "未知"
        }
    }
} 