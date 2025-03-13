package com.connectify.connectifyNow.domains

import com.connectify.connectifyNow.models.Post
import com.connectify.connectifyNow.models.UserInfo
import com.connectify.connectifyNow.repositories.Auth.FireStoreAuthRepository


class UserDomain {
    private val organizationDomain: OrganizationDomain = OrganizationDomain()
    private val postDomain: PostDomain = PostDomain()
    private val fireStoreAuthRepository: FireStoreAuthRepository = FireStoreAuthRepository()
    private val volunteerDomain: VolunteerDomain = VolunteerDomain()

    fun signIn(email: String, password: String, onSuccessCallBack: () -> Unit, onFailureCallBack: (String?) -> Unit) {
        Post.lastUpdated = 0
        fireStoreAuthRepository.signInUser(email, password, onSuccessCallBack, onFailureCallBack)
    }

    fun logOutUser() {
        fireStoreAuthRepository.logOutUser()
    }

    fun getUserId(): String? {
        return fireStoreAuthRepository.firebaseAuth.currentUser?.uid
    }

    fun resetPassword(email: String, onSuccessCallBack: () -> Unit, onFailureCallBack: (String?) -> Unit) {
        fireStoreAuthRepository.resetPassword(email, onSuccessCallBack, onFailureCallBack)
    }

    fun getUserInfo(id: String, callback: (userInfo: UserInfo?, error: String?) -> Unit) {
        fireStoreAuthRepository.getUserType(id) { userType ->
            when(userType) {
                "VOLUNTEER" -> volunteerDomain.getVolunteerById(id) { volunteerInfo ->
                    callback(UserInfo.UserVolunteer(volunteerInfo.copy(id = id)), null)
                }
                "ORGANIZATION" -> organizationDomain.getOrganizationById(id) { organizationInfo ->
                    callback(UserInfo.UserOrganization(organizationInfo.copy(id = id)), null)
                }
                else -> callback(null, "Unknown user type")
            }
        }
    }
}
