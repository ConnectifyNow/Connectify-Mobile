package com.connectify.connectifyNow.repositories.Post

class FireStorePostRepository {

    val apiManager = ApiManager()

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
}