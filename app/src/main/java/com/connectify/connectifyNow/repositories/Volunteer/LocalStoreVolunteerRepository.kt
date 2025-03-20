package com.connectify.connectifyNow.repositories.Volunteer

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.connectify.connectifyNow.models.Volunteer
import com.connectify.connectifyNow.models.dao.AppLocalDatabase

class LocalStoreVolunteerRepository {
    val appLocalDB = AppLocalDatabase
    val volunteerDao = appLocalDB.db.getVolunteerDao()
    val volunteer: LiveData<MutableList<Volunteer>> = volunteerDao.getAllVolunteers()

    @WorkerThread
    fun addVolunteer(volunteer: Volunteer) {
        volunteerDao.addVolunteer(volunteer)
    }

    @WorkerThread
    fun updateVolunteer(volunteer: Volunteer) {
        volunteerDao.updateVolunteer(volunteer)
    }

}