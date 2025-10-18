package com.example.dogetransfer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dogetransfer.R
import com.example.dogetransfer.model.DeviceInfo

class DeviceAdapter(
    private var devices: List<DeviceInfo> = emptyList(),
    private val onDeviceClick: (DeviceInfo) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    inner class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceName: TextView = view.findViewById(R.id.deviceName)
        val deviceStatus: TextView = view.findViewById(R.id.deviceStatus)
        val deviceAddress: TextView = view.findViewById(R.id.deviceAddress)

        fun bind(device: DeviceInfo) {
            deviceName.text = device.deviceName
            deviceStatus.text = device.getStatusText()
            deviceAddress.text = device.deviceAddress
            
            itemView.setOnClickListener {
                onDeviceClick(device)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int = devices.size

    fun updateDevices(newDevices: List<DeviceInfo>) {
        devices = newDevices
        notifyDataSetChanged()
    }
} 