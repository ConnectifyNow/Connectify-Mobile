package com.connectify.connectifyNow.adapters

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.connectify.connectifyNow.models.Post
import com.connectify.connectifyNow.R
import com.connectify.connectifyNow.FeedFragment
import com.connectify.connectifyNow.domains.UserDomain
import com.connectify.connectifyNow.viewModel.PostViewModel
import com.squareup.picasso.Picasso

class PostAdapter(var posts: MutableList<Post>, private var isFromFeed: Boolean, private var containArgs: Boolean = false) : RecyclerView.Adapter<PostAdapter.PostHolder>() {

    private var listener: FeedFragment.OnPostClickListener? = null
    fun setOnPostClickListener(listener: FeedFragment.OnPostClickListener) {
        this.listener = listener
    }

    class PostHolder(
        itemView: View,
        private val posts: List<Post>,
        private val listener: FeedFragment.OnPostClickListener?,
        isFromFeed: Boolean
    ) : RecyclerView.ViewHolder(itemView) {
        val ownerNameLabel: TextView = itemView.findViewById(R.id.ownerName)
        val contentLabel: TextView = itemView.findViewById(R.id.content)
        val image: ImageView = itemView.findViewById(R.id.imagePost)

        val editPostButton: ImageView = itemView.findViewById(R.id.post_edit_button)
        val deletePostButton: ImageView = itemView.findViewById(R.id.deletePostButton)
        val actionsToolbar: LinearLayout = itemView.findViewById(R.id.post_actions_toolbar);

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION && isFromFeed) {
                    listener?.onPostClicked(posts[position])
                    Log.d("onClickPost", posts[position].ownerId)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return PostHolder(view, posts, listener, isFromFeed)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        val post = posts[position]


        val currentUserID = UserDomain().getUserId()
        val isOwner = currentUserID == post.ownerId

        if (!isFromFeed && isOwner && !containArgs) {
            holder.actionsToolbar.visibility = View.VISIBLE;

            holder.editPostButton.setOnClickListener {
                if (holder.bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    val args = Bundle()
                    args.putString("postId", posts[position].id)

                    Navigation.findNavController(it)
                        .navigate(R.id.action_profileFragment_to_editPost, args)
                }
            }
        } else {
            holder.actionsToolbar.visibility = View.GONE;
        }

        holder.deletePostButton.setOnClickListener {
            deletePost(post)
        }

        if (post.imagePath.isNotBlank()) {
            holder.image.visibility = View.VISIBLE
            val imageUri = Uri.parse(post.imagePath)
            loadImage(imageUri, holder.image)
        } else {
            holder.image.visibility = View.GONE
        }
        holder.ownerNameLabel.text = post.title
        holder.contentLabel.text = post.content
    }

    private fun loadImage(imageUri: Uri, imageView: ImageView) {
        Picasso.get().load(imageUri).into(imageView)
    }


    fun clear() {
        posts.clear()
    }

    fun addAll(newPosts: MutableList<Post>) {
        newPosts.sortByDescending { it.lastUpdated };
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    private fun deletePost(post: Post) {
        val position = posts.indexOf(post)
        if (position != -1) {
            PostViewModel().deletePost(post)
            posts.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, posts.size)
        }
    }
}
