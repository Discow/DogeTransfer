package com.example.dogetransfer

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogetransfer.adapter.DeviceAdapter
import com.example.dogetransfer.adapter.FileAdapter
import com.example.dogetransfer.databinding.ActivityMainBinding
import com.example.dogetransfer.model.DeviceInfo
import com.example.dogetransfer.model.FileInfo
import com.example.dogetransfer.model.TransferStatus
import com.example.dogetransfer.transfer.FileTransferService
import com.example.dogetransfer.wifi.WiFiDirectManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var wifiDirectManager: WiFiDirectManager
    private lateinit var fileTransferService: FileTransferService
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var fileAdapter: FileAdapter

    private var selectedFile: FileInfo? = null

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedFile(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeServices()
        setupRecyclerViews()
        setupClickListeners()
        observeStateChanges()
        checkAndRequestPermissions()
    }

    private fun initializeServices() {
        wifiDirectManager = WiFiDirectManager(this)
        wifiDirectManager.initialize()

        fileTransferService = FileTransferService(this)
    }

    private fun setupRecyclerViews() {
        deviceAdapter = DeviceAdapter { device ->
            onDeviceClicked(device)
        }
        
        binding.devicesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = deviceAdapter
        }

        fileAdapter = FileAdapter { file ->
            onFileClicked(file)
        }
        
        binding.receivedFilesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = fileAdapter
        }
    }

    private fun setupClickListeners() {
        binding.discoverButton.setOnClickListener {
            discoverPeers()
        }

        binding.selectFileButton.setOnClickListener {
            openFilePicker()
        }

        binding.sendFileButton.setOnClickListener {
            sendFile()
        }

        binding.disconnectButton.setOnClickListener {
            disconnect()
        }
    }

    private fun observeStateChanges() {
        lifecycleScope.launch {
            wifiDirectManager.isWifiP2pEnabled.collect { enabled ->
                updateWiFiStatus(enabled)
            }
        }

        lifecycleScope.launch {
            wifiDirectManager.peers.collect { peers ->
                deviceAdapter.updateDevices(peers)
                binding.devicesEmptyText.visibility = 
                    if (peers.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            wifiDirectManager.isConnected.collect { connected ->
                updateConnectionStatus(connected)
            }
        }

        lifecycleScope.launch {
            wifiDirectManager.connectionInfo.collect { info ->
                info?.let {
                    if (it.groupFormed) {
                        fileTransferService.startFileServer(lifecycleScope)
                        binding.connectionInfo.text = if (it.isGroupOwner) {
                            "已连接 (群组所有者)\nIP: ${it.groupOwnerAddress.hostAddress}"
                        } else {
                            "已连接 (客户端)\n服务器: ${it.groupOwnerAddress.hostAddress}"
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            wifiDirectManager.thisDevice.collect { device ->
                device?.let {
                    binding.deviceNameText.text = "本机: ${it.deviceName}"
                }
            }
        }

        lifecycleScope.launch {
            fileTransferService.transferProgress.collect { progress ->
                progress?.let {
                    when (it.status) {
                        TransferStatus.TRANSFERRING -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.progressBar.progress = it.progress
                            binding.transferStatus.text = 
                                "传输中: ${it.fileInfo.name} - ${it.progress}%"
                        }
                        TransferStatus.COMPLETED -> {
                            binding.progressBar.visibility = View.GONE
                            binding.transferStatus.text = "传输完成: ${it.fileInfo.name}"
                            Toast.makeText(this@MainActivity, "文件传输成功", Toast.LENGTH_SHORT).show()
                            fileTransferService.clearTransferProgress()
                        }
                        TransferStatus.FAILED -> {
                            binding.progressBar.visibility = View.GONE
                            binding.transferStatus.text = "传输失败: ${it.fileInfo.name}"
                            Toast.makeText(this@MainActivity, "文件传输失败", Toast.LENGTH_SHORT).show()
                            fileTransferService.clearTransferProgress()
                        }
                        else -> {}
                    }
                }
            }
        }

        lifecycleScope.launch {
            fileTransferService.receivedFiles.collect { files ->
                fileAdapter.updateFiles(files)
                binding.receivedFilesEmptyText.visibility = 
                    if (files.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 1001)
        }
    }

    private fun discoverPeers() {
        wifiDirectManager.discoverPeers(
            onSuccess = {
                Toast.makeText(this, "开始搜索设备...", Toast.LENGTH_SHORT).show()
            },
            onFailure = { reason ->
                Toast.makeText(this, "搜索失败: $reason", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun onDeviceClicked(device: DeviceInfo) {
        AlertDialog.Builder(this)
            .setTitle("连接设备")
            .setMessage("是否连接到 ${device.deviceName}?")
            .setPositiveButton("连接") { _, _ ->
                connectToDevice(device)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun connectToDevice(device: DeviceInfo) {
        wifiDirectManager.connectToDevice(
            device = device,
            onSuccess = {
                Toast.makeText(this, "正在连接到 ${device.deviceName}...", Toast.LENGTH_SHORT).show()
            },
            onFailure = { reason ->
                Toast.makeText(this, "连接失败: $reason", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun disconnect() {
        wifiDirectManager.disconnect(
            onSuccess = {
                Toast.makeText(this, "已断开连接", Toast.LENGTH_SHORT).show()
                fileTransferService.stopFileServer()
            },
            onFailure = { reason ->
                Toast.makeText(this, "断开连接失败: $reason", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }

    private fun handleSelectedFile(uri: Uri) {
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                
                val name = if (nameIndex >= 0) it.getString(nameIndex) else "unknown"
                val size = if (sizeIndex >= 0) it.getLong(sizeIndex) else 0L
                val mimeType = contentResolver.getType(uri)

                selectedFile = FileInfo(uri, name, size, mimeType)
                binding.selectedFileText.text = "已选择: $name (${selectedFile?.getSizeString()})"
                binding.sendFileButton.isEnabled = true
            }
        }
    }

    private fun sendFile() {
        val file = selectedFile ?: return
        val info = wifiDirectManager.connectionInfo.value ?: return

        if (!info.groupFormed) {
            Toast.makeText(this, "未连接到设备", Toast.LENGTH_SHORT).show()
            return
        }

        val targetAddress = if (info.isGroupOwner) {
            // 作为群组所有者，需要客户端的地址
            // 在实际实现中，可能需要通过其他方式获取客户端地址
            Toast.makeText(this, "请在另一台设备上发送文件", Toast.LENGTH_SHORT).show()
            return
        } else {
            info.groupOwnerAddress.hostAddress ?: return
        }

        lifecycleScope.launch {
            val success = fileTransferService.sendFile(file, targetAddress)
            if (success) {
                runOnUiThread {
                    selectedFile = null
                    binding.selectedFileText.text = "未选择文件"
                    binding.sendFileButton.isEnabled = false
                }
            }
        }
    }

    private fun onFileClicked(file: FileInfo) {
        AlertDialog.Builder(this)
            .setTitle(file.name)
            .setMessage("文件大小: ${file.getSizeString()}\n位置: ${file.uri.path}")
            .setPositiveButton("确定", null)
            .show()
    }

    private fun updateWiFiStatus(enabled: Boolean) {
        binding.wifiStatus.text = if (enabled) "WiFi Direct: 已启用" else "WiFi Direct: 未启用"
        binding.wifiStatus.setTextColor(
            ContextCompat.getColor(this, if (enabled) R.color.green else R.color.red)
        )
    }

    private fun updateConnectionStatus(connected: Boolean) {
        binding.connectionStatus.text = if (connected) "状态: 已连接" else "状态: 未连接"
        binding.connectionStatus.setTextColor(
            ContextCompat.getColor(this, if (connected) R.color.green else R.color.red)
        )
        
        binding.disconnectButton.isEnabled = connected
        binding.selectFileButton.isEnabled = connected
        
        if (!connected) {
            binding.connectionInfo.text = ""
            binding.selectedFileText.text = "未选择文件"
            binding.sendFileButton.isEnabled = false
            selectedFile = null
        }
    }

    override fun onResume() {
        super.onResume()
        wifiDirectManager.register()
    }

    override fun onPause() {
        super.onPause()
        wifiDirectManager.unregister()
    }

    override fun onDestroy() {
        super.onDestroy()
        fileTransferService.stopFileServer()
    }
} 