package com.connectify.connectifyNow.repositories.Auth

import com.connectify.connectifyNow.repositories.ApiManager

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

    fun signInUser(email: String, password: String, onSuccessCallBack: () -> Unit, onFailureCallBack: (String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccessCallBack() }
            .addOnFailureListener { onFailureCallBack(it.message) }
    }

    fun logOutUser() {
        firebaseAuth.signOut()
    }

    fun resetPassword(email: String, onSuccessCallBack: () -> Unit, onFailureCallBack: (String?) -> Unit) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccessCallBack() }
            .addOnFailureListener { onFailureCallBack(it.message) }
    }

    fun getUserType(userId: String, onSuccessCallBack: (String?) -> Unit) {
        val documentRef = apiManager.db.collection(USER_TYPE_COLLECTION_PATH).document(userId)
        documentRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userType = document.getString("type")
                    onSuccessCallBack(userType)
                }
            }

    }


}