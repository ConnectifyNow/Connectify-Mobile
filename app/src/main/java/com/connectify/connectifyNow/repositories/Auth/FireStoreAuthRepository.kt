package com.connectify.connectifyNow.repositories.Auth

class FireStoreAuthRepository {

    companion object {
        const val USER_TYPE_COLLECTION_PATH = "userType"
    }

    val firebaseAuth = ApiManager().firebaseAuth
    val apiManager = ApiManager()

    fun createUser(email: String, password: String, onSuccessCallBack: (String?) -> Unit, onFailureCallBack: (String?) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                onSuccessCallBack(userId)
            }
            .addOnFailureListener { e ->
                onFailureCallBack(e.message)
            }
    }

}