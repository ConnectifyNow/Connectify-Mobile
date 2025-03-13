package com.connectify.connectifyNow.viewModel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.connectify.connectifyNow.models.Post
import com.connectify.connectifyNow.domains.PostDomain

class PostViewModel: ViewModel() {

    private val _posts: MutableLiveData<MutableList<Post>> = MutableLiveData()
    val posts: LiveData<MutableList<Post>> = _posts

    private val postDomain: PostDomain = PostDomain()

    init {
        postDomain.posts.observeForever { posts ->
            _posts.postValue(posts.toMutableList())
        }
    }

    fun getPosts() = viewModelScope.launch (Dispatchers.IO) {
        postDomain.posts
    }

    fun getPostsByOwnerId(ownerId: String, callback: (posts: List<Post>) -> Unit) = viewModelScope.launch (Dispatchers.IO) {
        postDomain.getPostsByUserId(ownerId, callback);
    }

    fun getPostById(id: String, callback: (Post?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            postDomain.getPostById(id) { post ->
                callback(post)
            }
        }
    }

   fun addPost(post: Post, callback: (Boolean) -> Unit) {
       viewModelScope.launch(Dispatchers.IO) {
           try {
               postDomain.addPost(post)
               withContext(Dispatchers.Main) {
                   callback(true)
               }
           } catch (e: Exception) {
               withContext(Dispatchers.Main) {
                   callback(false)
               }
           }
       }
   }


    fun deletePost(post: Post) = viewModelScope.launch(Dispatchers.IO) {
        postDomain.deletePost(post)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshPosts() {
        viewModelScope.launch(Dispatchers.IO) {
            postDomain.refreshPosts()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun update(postId: String, data: Map<String, Any>): Boolean {
        return try {
            postDomain.updatePost(postId, data)
            true
        } catch (e: Exception) {
            false
        }
    }

}
