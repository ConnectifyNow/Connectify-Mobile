package com.connectify.connectifyNow.domains

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.concurrent.Executors
import androidx.lifecycle.MediatorLiveData
import com.connectify.connectifyNow.models.Post
import com.connectify.connectifyNow.repositories.Post.FireStorePostRepository
import com.connectify.connectifyNow.repositories.Post.LocalStorePostRepository

class PostDomain {
    val localStorePostRepository: LocalStorePostRepository = LocalStorePostRepository()
    val fireStorePostRepository: FireStorePostRepository = FireStorePostRepository()
    private val coroutineScope = CoroutineScope(Job())

    private var executor = Executors.newSingleThreadExecutor()
    private val _posts: MutableLiveData<List<Post>> = MutableLiveData()

    val posts: LiveData<List<Post>> get() = _posts
    private val mediatorLiveData = MediatorLiveData<List<Post>>()

    init {
        val localPostsLiveData = localStorePostRepository.getAllPosts()
        localPostsLiveData.observeForever { localPosts ->
            _posts.value = localPosts
        }
    }

    fun getPostById(id: String, callback: (post: Post?) -> Unit) {
        return fireStorePostRepository.getPostById(id, callback)
    }

    fun getPostsByUserId(userId: String, callback: (posts: List<Post>) -> Unit) {
        fireStorePostRepository.getPostsByOwnerId(userId, callback);
    }

    fun addPost(post: Post) {
        fireStorePostRepository.addPost(post)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshPosts() {
        val lastUpdated: Long = Post.lastUpdated

        fireStorePostRepository.getPosts(lastUpdated) { posts ->
            executor.execute {
                for (post in posts) {
                    localStorePostRepository.insert(post)

                }

                Post.lastUpdated = Instant.now().epochSecond
            }
        }
    }

    suspend fun deletePost(post: Post) {
        fireStorePostRepository.deletePost(post)
        localStorePostRepository.delete(post)
    }

    fun deleteAllPosts() {
        localStorePostRepository.deleteAllPosts()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updatePost(id: String, data: Map<String, Any>) {
        fireStorePostRepository.updatePost(id, data) { updatedPost ->
            if (updatedPost != null) {
                updateLocalCache(updatedPost)
            }
        }
    }

    private var isUpdatingCache = false

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateLocalCache(updatedPost: Post) {
        if (isUpdatingCache) {
            return
        }
        isUpdatingCache = true

        val localPostsLiveData = localStorePostRepository.getAllPosts()

        mediatorLiveData.addSource(localPostsLiveData) { localPosts ->
            val index = localPosts.indexOfFirst { it.id == updatedPost.id }

            try {
                if (index != -1) {
                    localPosts[index] = updatedPost
                    coroutineScope.launch {
                        localStorePostRepository.update(updatedPost)
                        Post.lastUpdated = Instant.now().epochSecond
                        isUpdatingCache = false
                    }
                }
            } catch (error: Exception) {
                println(error.message)
                Log.d("Cache", error.message.toString())
            }
        }
    }
}