package com.connectify.connectifyNow.viewModules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.connectify.connectifyNow.models.Volunteer
import com.connectify.connectifyNow.domains.VolunteerDomain

class VolunteerViewModel: ViewModel() {
    val volunteerDomain: VolunteerDomain = VolunteerDomain()

    fun createUserAsVolunteer(email: String, password: String, onSuccessCallBack: (String?) -> Unit, onFailureCallBack: (String?) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        volunteerDomain.fireStoreAuthRepository.createUser(email, password, { userId ->
            onSuccessCallBack(userId)
        }, onFailureCallBack)
    }

    fun getAllVolunteers() = viewModelScope.launch(Dispatchers.IO) {
        volunteerDomain.getAllVolunteers()
    }

    fun getVolunteer(volunteerId: String, callback: (volunteer: Volunteer) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        volunteerDomain.getVolunteer(volunteerId, callback);
    }

    fun addVolunteer(volunteer: Volunteer) = viewModelScope.launch(Dispatchers.IO) {
        volunteerDomain.addVolunteer(volunteer)
    }

    fun update(volunteer: Volunteer, data: Map<String, Any>, onSuccessCallBack: () -> Unit, onFailureCallBack: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            volunteerDomain.update(volunteer, data, onSuccessCallBack, onFailureCallBack)
        } catch (e: Exception) {
            onFailureCallBack()
        }
    }
}
