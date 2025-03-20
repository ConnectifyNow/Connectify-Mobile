package com.connectify.connectifyNow.repositories.Post

import com.connectify.connectifyNow.models.Post
import com.connectify.connectifyNow.repositories.ApiManager
import com.google.firebase.Timestamp

class FireStorePostRepository {

    private val apiManager = ApiManager()

    companion object {
        const val POSTS_COLLECTION_PATH = "posts"
    }

    fun addPost(post: Post) {
        apiManager.db.collection(POSTS_COLLECTION_PATH).add(post.json)
    }

    fun deletePost(post: Post) {
        apiManager.db.collection(POSTS_COLLECTION_PATH).whereEqualTo("id", post.id).get()
            .addOnSuccessListener {
                apiManager.db.collection(POSTS_COLLECTION_PATH).document(it.documents[0].id)
                    .delete()
            }
    }

    fun updatePost(postId: String, data: Map<String, Any>, callback: (Post?) -> Unit) {
        val postRef =
            apiManager.db.collection(POSTS_COLLECTION_PATH).whereEqualTo(Post.POST_ID, postId)

        postRef.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    documentSnapshot.reference.update(data)
                        .addOnSuccessListener {
                            documentSnapshot.reference.get()
                                .addOnSuccessListener { updatedDocumentSnapshot ->
                                    val updatedPost = updatedDocumentSnapshot.data?.let { it1 ->
                                        Post.fromJSON(
                                            it1
                                        )
                                    }
                                    callback(updatedPost)
                                }
                                .addOnFailureListener {
                                    callback(null)
                                }
                        }
                        .addOnFailureListener {
                            callback(null)
                        }
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun getPostsByOwnerId(ownerId: String, callback: (List<Post>) -> Unit) {
        apiManager.db.collection(POSTS_COLLECTION_PATH)
            .whereEqualTo(Post.OWNER_ID_KEY, ownerId)
            .get().addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val posts: MutableList<Post> = mutableListOf()
                        for (json in it.result) {
                            val post = Post.fromJSON(json.data)
                            posts.add(post)
                        }
                        callback(posts)
                    }

                    false -> callback(listOf())
                }
            }
    }

    fun getPosts(since: Long, callback: (List<Post>) -> Unit) {
        apiManager.db.collection(POSTS_COLLECTION_PATH)
            .whereGreaterThanOrEqualTo(Post.LAST_UPDATED, Timestamp(since, 0))
            .get().addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val posts: MutableList<Post> = mutableListOf()
                        for (json in it.result) {
                            val post = Post.fromJSON(json.data)
                            posts.add(post)
                        }
                        callback(posts)
                    }

                    false -> callback(listOf())
                }
            }
    }

    fun getPostById(postId: String, callback: (post: Post?) -> Unit) {
        apiManager.db.collection(POSTS_COLLECTION_PATH)
            .whereEqualTo("id", postId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    if (result != null && !result.isEmpty) {
                        val json = result.documents[0]
                        val post = json.data?.let { Post.fromJSON(it) }
                        callback(post)
                    } else callback(null)
                } else callback(null)
            }
    }
}