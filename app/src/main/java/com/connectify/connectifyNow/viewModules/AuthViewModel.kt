package com.connectify.connectifyNow.viewModules

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.connectify.connectifyNow.domains.UserDomain
import com.connectify.connectifyNow.models.UserInfo

class AuthViewModel: ViewModel() {
    val firebaseAuth = FirebaseAuth.getInstance()

    val userUseCases: UserDomain = UserDomain()
    private val _userId = MutableLiveData<String?>()
    val userId: LiveData<String?> get() = _userId

    init {
        setCurrentUserId()
    }

    fun signInUser(email: String, password: String, onSuccess: () -> Unit, onFailure: (String?) -> Unit) {
        userUseCases.signIn(email, password, onSuccessCallBack = {
            // Set the user ID in the LiveData upon successful sign-in
            setCurrentUserId()
            onSuccess.invoke()
        }, onFailure)
    }

    fun getUserId(): String? {
        return userId.value
    }

    fun setCurrentUserId() {
        _userId.value = firebaseAuth.currentUser?.uid
    }

    fun logOutUser() {
        userUseCases.logOutUser()
    }

    fun resetPassword(email: String, onSuccessCallBack: () -> Unit, onFailureCallBack: (String?) -> Unit) {
        userUseCases.resetPassword(email, onSuccessCallBack, onFailureCallBack)
    }

    fun getInfoOnUser(id: String, onCallBack: (userInfo: UserInfo?, error: String?) -> Unit) {
        userUseCases.getUserInfo(id, onCallBack)
    }
}
