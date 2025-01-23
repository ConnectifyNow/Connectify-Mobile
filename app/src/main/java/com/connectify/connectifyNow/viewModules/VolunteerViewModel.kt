package com.connectify.connectifyNow.viewModules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.connectify.connectifyNow.models.Volunteer

class VolunteerViewModel: ViewModel() {
    val volunteerUseCases: VolunteerUseCases = VolunteerUseCases()

    fun createUserAsVolunteer(email: String, password: String, onSuccessCallBack: (String?) -> Unit, onFailureCallBack: (String?) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        volunteerUseCases.fireStoreAuthRepository.createUser(email, password, { userId ->
            onSuccessCallBack(userId)
        }, onFailureCallBack)
    }

    fun getAllVolunteers() = viewModelScope.launch(Dispatchers.IO) {
        volunteerUseCases.getAllVolunteers()
    }

    fun getVolunteer(volunteerId: String, callback: (volunteer: Volunteer) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        volunteerUseCases.getVolunteer(volunteerId, callback);
    }

    fun addVolunteer(volunteer: Volunteer) = viewModelScope.launch(Dispatchers.IO) {
        volunteerUseCases.addVolunteer(volunteer)
    }

    fun update(volunteer: Volunteer, data: Map<String, Any>, onSuccessCallBack: () -> Unit, onFailureCallBack: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            volunteerUseCases.update(volunteer, data, onSuccessCallBack, onFailureCallBack)
        } catch (e: Exception) {
            onFailureCallBack()
        }
    }




}
