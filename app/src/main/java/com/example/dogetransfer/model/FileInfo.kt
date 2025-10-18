package com.example.dogetransfer.model

import android.net.Uri

data class FileInfo(
    val uri: Uri,
    val name: String,
    val size: Long,
    val mimeType: String?
) {
    fun getSizeString(): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$size B"
        }
    }
}

data class TransferProgress(
    val fileInfo: FileInfo,
    val bytesTransferred: Long,
    val totalBytes: Long,
    val status: TransferStatus
) {
    val progress: Int
        get() = if (totalBytes > 0) {
            ((bytesTransferred * 100) / totalBytes).toInt()
        } else 0
}

enum class TransferStatus {
    PENDING,
    TRANSFERRING,
    COMPLETED,
    FAILED,
    CANCELLED
} 