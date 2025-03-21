package com.connectify.connectifyNow.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.connectify.connectifyNow.models.Volunteer
import com.connectify.connectifyNow.domains.VolunteerDomain

class VolunteerViewModel: ViewModel() {
    private val volunteerDomain: VolunteerDomain = VolunteerDomain()

    fun createUserAsVolunteer(email: String,password:String, onSuccessCallBack: (String?) -> Unit, onFailureCallBack: (String?) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        volunteerDomain.fireStoreAuthRepository.createUser(email, password, { userId ->
            onSuccessCallBack(userId)
        }, onFailureCallBack)
    }

    fun getVolunteer(volunteerId: String, callback: (volunteer: Volunteer) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        volunteerDomain.getVolunteerById(volunteerId, callback)
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
