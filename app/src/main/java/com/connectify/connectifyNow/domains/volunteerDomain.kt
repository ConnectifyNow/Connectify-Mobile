package com.connectify.connectifyNow.domains

import androidx.lifecycle.LiveData
import com.connectify.connectifyNow.models.Volunteer.Volunteer
import com.connectify.connectifyNow.repoistory.Auth.FireStoreAuthRepository
import com.connectify.connectifyNow.repoistory.Volunteer.FireStoreVolunteerRepository
import com.connectify.connectifyNow.repoistory.Volunteer.LocalStoreUserRepository

class volunteerDomain {

    val localStoreVolunteerRepository: LocalStoreUserRepository = LocalStoreUserRepository()
    val fireStoreVolunteerRepository: FireStoreVolunteerRepository = FireStoreVolunteerRepository()
    val fireStoreAuthRepository: FireStoreAuthRepository = FireStoreAuthRepository()

    val volunteerLiveData: LiveData<MutableList<Volunteer>> get() = localStoreVolunteerRepository.volunteer

    fun getVolunteerById(id: String, callback: (volunteer: Volunteer) -> Unit) {
        fireStoreVolunteerRepository.getVolunteerById(id, callback);
    }

    fun getVolunteers() {
        volunteerLiveData
    }

    suspend fun updateVolunteer(
        volunteer: Volunteer,
        data: Map<String, Any>,
        onSuccessCallBack: () -> Unit,
        onFailureCallBack: () -> Unit
    ) {
        fireStoreVolunteerRepository.updateVolunteer(
            volunteer,
            data,
            onSuccessCallBack,
            onFailureCallBack
        )
        localStoreVolunteerRepository.updateVolunteer(volunteer)
    }

    suspend fun addVolunteer(volunteer: Volunteer) {
        fireStoreVolunteerRepository.addVolunteer(volunteer) { id ->
            fireStoreVolunteerRepository.setVolunteerUserType(id)
        }
        localStoreVolunteerRepository.addVolunteer(volunteer)
    }

}