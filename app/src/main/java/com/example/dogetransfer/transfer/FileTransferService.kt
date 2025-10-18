package com.example.dogetransfer.transfer

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.dogetransfer.model.FileInfo
import com.example.dogetransfer.model.TransferProgress
import com.example.dogetransfer.model.TransferStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class FileTransferService(private val context: Context) {

    companion object {
        private const val TAG = "FileTransferService"
        private const val SOCKET_TIMEOUT = 10000
        const val FILE_TRANSFER_PORT = 8988
        private const val BUFFER_SIZE = 8192
    }

    private val _transferProgress = MutableStateFlow<TransferProgress?>(null)
    val transferProgress: StateFlow<TransferProgress?> = _transferProgress.asStateFlow()

    private val _receivedFiles = MutableStateFlow<List<FileInfo>>(emptyList())
    val receivedFiles: StateFlow<List<FileInfo>> = _receivedFiles.asStateFlow()

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null

    fun startFileServer(scope: CoroutineScope) {
        serverJob?.cancel()
        serverJob = scope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(FILE_TRANSFER_PORT)
                Log.d(TAG, "File server started on port $FILE_TRANSFER_PORT")

                while (isActive) {
                    try {
                        val client = serverSocket?.accept()
                        client?.let {
                            launch { handleFileReceive(it) }
                        }
                    } catch (e: Exception) {
                        if (isActive) {
                            Log.e(TAG, "Error accepting connection", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Server error", e)
            }
        }
    }

    fun stopFileServer() {
        serverJob?.cancel()
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing server socket", e)
        }
        serverSocket = null
    }

    suspend fun sendFile(fileInfo: FileInfo, hostAddress: String): Boolean {
        return withContext(Dispatchers.IO) {
            var socket: Socket? = null
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                _transferProgress.value = TransferProgress(
                    fileInfo = fileInfo,
                    bytesTransferred = 0,
                    totalBytes = fileInfo.size,
                    status = TransferStatus.PENDING
                )

                socket = Socket()
                socket.connect(InetSocketAddress(hostAddress, FILE_TRANSFER_PORT), SOCKET_TIMEOUT)
                socket.soTimeout = SOCKET_TIMEOUT

                outputStream = socket.getOutputStream()
                val dataOutputStream = DataOutputStream(outputStream)

                // 发送文件元数据
                dataOutputStream.writeUTF(fileInfo.name)
                dataOutputStream.writeLong(fileInfo.size)
                dataOutputStream.writeUTF(fileInfo.mimeType ?: "application/octet-stream")
                dataOutputStream.flush()

                // 发送文件内容
                inputStream = context.contentResolver.openInputStream(fileInfo.uri)
                if (inputStream == null) {
                    Log.e(TAG, "Cannot open input stream for file")
                    _transferProgress.value = _transferProgress.value?.copy(status = TransferStatus.FAILED)
                    return@withContext false
                }

                _transferProgress.value = _transferProgress.value?.copy(status = TransferStatus.TRANSFERRING)

                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    _transferProgress.value = _transferProgress.value?.copy(
                        bytesTransferred = totalBytesRead
                    )
                }

                outputStream.flush()
                _transferProgress.value = _transferProgress.value?.copy(status = TransferStatus.COMPLETED)
                
                Log.d(TAG, "File sent successfully: ${fileInfo.name}")
                true

            } catch (e: Exception) {
                Log.e(TAG, "Error sending file", e)
                _transferProgress.value = _transferProgress.value?.copy(status = TransferStatus.FAILED)
                false
            } finally {
                inputStream?.close()
                outputStream?.close()
                socket?.close()
            }
        }
    }

    private suspend fun handleFileReceive(client: Socket) {
        withContext(Dispatchers.IO) {
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null

            try {
                inputStream = client.getInputStream()
                val dataInputStream = DataInputStream(inputStream)

                // 读取文件元数据
                val fileName = dataInputStream.readUTF()
                val fileSize = dataInputStream.readLong()
                val mimeType = dataInputStream.readUTF()

                Log.d(TAG, "Receiving file: $fileName, size: $fileSize")

                // 创建接收文件
                val downloadsDir = File(context.getExternalFilesDir(null), "DogeTransfer")
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }

                val file = File(downloadsDir, fileName)
                outputStream = FileOutputStream(file)

                val fileInfo = FileInfo(
                    uri = Uri.fromFile(file),
                    name = fileName,
                    size = fileSize,
                    mimeType = mimeType
                )

                _transferProgress.value = TransferProgress(
                    fileInfo = fileInfo,
                    bytesTransferred = 0,
                    totalBytes = fileSize,
                    status = TransferStatus.TRANSFERRING
                )

                // 接收文件内容
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (totalBytesRead < fileSize) {
                    bytesRead = inputStream.read(buffer, 0, 
                        minOf(BUFFER_SIZE.toLong(), fileSize - totalBytesRead).toInt())
                    if (bytesRead == -1) break

                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    _transferProgress.value = _transferProgress.value?.copy(
                        bytesTransferred = totalBytesRead
                    )
                }

                _transferProgress.value = _transferProgress.value?.copy(status = TransferStatus.COMPLETED)

                // 添加到接收文件列表
                _receivedFiles.value = _receivedFiles.value + fileInfo

                Log.d(TAG, "File received successfully: $fileName")

            } catch (e: Exception) {
                Log.e(TAG, "Error receiving file", e)
                _transferProgress.value = _transferProgress.value?.copy(status = TransferStatus.FAILED)
            } finally {
                inputStream?.close()
                outputStream?.close()
                client.close()
            }
        }
    }

    fun clearTransferProgress() {
        _transferProgress.value = null
    }
} 