package com.example.dogetransfer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dogetransfer.R
import com.example.dogetransfer.model.FileInfo

class FileAdapter(
    private var files: List<FileInfo> = emptyList(),
    private val onFileClick: (FileInfo) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    inner class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileName: TextView = view.findViewById(R.id.fileName)
        val fileSize: TextView = view.findViewById(R.id.fileSize)

        fun bind(file: FileInfo) {
            fileName.text = file.name
            fileSize.text = file.getSizeString()
            
            itemView.setOnClickListener {
                onFileClick(file)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int = files.size

    fun updateFiles(newFiles: List<FileInfo>) {
        files = newFiles
        notifyDataSetChanged()
    }
} 