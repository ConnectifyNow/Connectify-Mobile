package com.connectify.connectifyNow.domains

import com.connectify.connectifyNow.models.Post.Post
import com.connectify.connectifyNow.models.User
import com.connectify.connectifyNow.repoistory.Auth.FireStoreAuthRepository

class UserDomain {
    private val organizationUseCases: OrganizationUseCases = OrganizationUseCases()
    private val postUseCases: PostUseCases = PostUseCases()
    private val fireStoreAuthRepository: FireStoreAuthRepository = FireStoreAuthRepository()
    private val studentUseCases: VolunteerUseCases = VolunteerUseCases()

    fun signIn(email: String, password: String, onSuccessCallBack: () -> Unit, onFailureCallBack: (String?) -> Unit) {
        Post.lastUpdated = 0
        fireStoreAuthRepository.signInUser(email, password, onSuccessCallBack, onFailureCallBack)
    }

    fun logOutUser() {
        fireStoreAuthRepository.logOutUser()
    }

    fun getUserId(): String? {
        return fireStoreAuthRepository.firebaseAuth.currUser?.id
    }

    fun resetPassword(email: String, onSuccessCallBack: () -> Unit, onFailureCallBack: (String?) -> Unit) {
        fireStoreAuthRepository.resetPassword(email, onSuccessCallBack, onFailureCallBack)
    }

    fun getUserInfo(id: String, callback: (userInfo: User?, error: String?) -> Unit) {
        fireStoreAuthRepository.getUserType(id) { userType ->
            when(userType) {
                "VOLUNTEER" -> volunteerUseCases.getVolunteer(id) { volunteerInfo ->
                    callback(User.UserVolunteer(volunteerInfo.copy(id = id)), null)
                }
                "ORGANIZATION" -> organizationUseCases.getOrganization(id) { organizationInfo ->
                    callback(User.UserOrganization(organizationInfo.copy(id = id)), null)
                }
                else -> callback(null, "Unknown user type") // Handle other types if needed
            }
        }
    }
}
