package com.autoyoutube.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media.MediaBrowser.MediaItem
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.autoyoutube.databinding.ItemVideoBinding

/**
 * RecyclerView Adapter for displaying YouTube videos
 */
class VideoAdapter(
    private val onVideoClick: (VideoItem) -> Unit
) : ListAdapter<MediaItem, VideoAdapter.VideoViewHolder>(VideoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VideoViewHolder(
        private val binding: ItemVideoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MediaItem) {
            val title = item.description.title?.toString() ?: "Unknown"
            val subtitle = item.description.subtitle?.toString() ?: ""
            
            binding.videoTitle.text = title
            binding.videoChannel.text = subtitle
            
            // Load thumbnail using an image loader (Glide, Coil, etc.)
            // For now, just show placeholder
            binding.videoThumbnail.setImageResource(android.R.drawable.ic_media_play)
            
            binding.root.setOnClickListener {
                // Convert MediaItem back to VideoItem for callback
                val videoItem = VideoItem(
                    id = item.mediaId ?: "",
                    title = title,
                    channel = subtitle,
                    category = ""
                )
                onVideoClick(videoItem)
            }
        }
    }

    class VideoDiffCallback : DiffUtil.ItemCallback<MediaItem>() {
        override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * Simple video data class for click handlers
 */
data class VideoItem(
    val id: String,
    val title: String,
    val channel: String,
    val category: String
)
