package com.connectify.connectifyNow.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.connectify.connectifyNow.models.Volunteer
import com.connectify.connectifyNow.domains.VolunteerDomain

class VolunteerViewModel: ViewModel() {
    val volunteerDomain: VolunteerDomain = VolunteerDomain()

    fun createUserAsVolunteer(volunteer: Volunteer, onSuccessCallBack: (String?) -> Unit, onFailureCallBack: (String?) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        volunteerDomain.fireStoreVolunteerRepository.addVolunteer(volunteer, { userId ->
            onSuccessCallBack(userId)
        })
    }

    fun getAllVolunteers() = viewModelScope.launch(Dispatchers.IO) {
        volunteerDomain.getVolunteers()
    }

    fun getVolunteer(volunteerId: String, callback: (volunteer: Volunteer) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        volunteerDomain.getVolunteerById(volunteerId, callback);
    }

    fun addVolunteer(volunteer: Volunteer) = viewModelScope.launch(Dispatchers.IO) {
        volunteerDomain.addVolunteer(volunteer)
    }

    fun update(volunteer: Volunteer, data: Map<String, Any>, onSuccessCallBack: () -> Unit, onFailureCallBack: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            volunteerDomain.updateVolunteer(volunteer, data, onSuccessCallBack, onFailureCallBack)
        } catch (e: Exception) {
            onFailureCallBack()
        }
    }
}
