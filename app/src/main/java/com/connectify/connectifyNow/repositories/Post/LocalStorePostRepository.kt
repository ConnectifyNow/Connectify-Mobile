package com.connectify.connectifyNow.repositories.Post

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.connectify.connectifyNow.models.Post
import com.connectify.connectifyNow.models.dao.AppLocalDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalStorePostRepository {

    private val appLocalDB = AppLocalDatabase
    private val postDao = appLocalDB.db.getPostDao()

    @WorkerThread
    fun insert(post: Post) {
        postDao.insertPost(post)
    }

    @WorkerThread
    suspend fun update(post: Post) {
        withContext(Dispatchers.IO) {
            postDao.updatePost(post)
        }
    }

    @WorkerThread
    fun delete(post: Post) {
        postDao.deletePost(post)
    }

    @WorkerThread
    fun getAllPosts(): LiveData<MutableList<Post>> {
        return postDao.getAllPosts()
    }
}