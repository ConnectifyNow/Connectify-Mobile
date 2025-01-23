package com.connectify.connectifyNow.domains

import com.connectify.connectifyNow.models.Post
import com.connectify.connectifyNow.models.UserInfo
import com.connectify.connectifyNow.models.Volunteer
import com.connectify.connectifyNow.repositories.Auth.FireStoreAuthRepository

class UserUseCases {
    private val fireStoreAuthRepository: FireStoreAuthRepository = FireStoreAuthRepository()
    private val volunteerUseCases: VolunteerUseCases = VolunteerUseCases()
    private val organizationUseCases: OrganizationUseCases = OrganizationUseCases()
    private val postUseCases: PostUseCases = PostUseCases()

    fun signInUser(email: String, password: String, onSuccessCallBack: () -> Unit, onFailureCallBack: (String?) -> Unit) {
        Post.lastUpdated = 0
        fireStoreAuthRepository.signInUser(email, password, onSuccessCallBack, onFailureCallBack)
    }

    fun logOutUser() {
        fireStoreAuthRepository.logOutUser()
    }

    fun resetPassword(email: String, onSuccessCallBack: () -> Unit, onFailureCallBack: (String?) -> Unit) {
        fireStoreAuthRepository.resetPassword(email, onSuccessCallBack, onFailureCallBack)
    }

    fun getUserId(): String? {
        return fireStoreAuthRepository.firebaseAuth.currentUser?.uid
    }

    fun getUserInfo(id: String, callback: (userInfo: UserInfo?, error: String?) -> Unit) {
        fireStoreAuthRepository.getUserType(id) { userType ->
            when(userType) {
                "VOLUNTEER" -> volunteerUseCases.getVolunteer(id) { volunteerInfo ->
                    callback(UserInfo.UserVolunteer(volunteerInfo.copy(id = id)), null)
                }
                "ORGANIZATION" -> organizationUseCases.getOrganization(id) { organizationInfo ->
                    callback(UserInfo.UserOrganization(organizationInfo.copy(id = id)), null)
                }
                else -> callback(null, "Unknown user type") // Handle other types if needed
            }
        }
    }
}
