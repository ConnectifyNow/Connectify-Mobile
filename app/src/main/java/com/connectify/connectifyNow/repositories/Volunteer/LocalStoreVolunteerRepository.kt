package com.connectify.connectifyNow.repositories.Volunteer

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class LocalStoreVolunteerRepository {
    val appLocalDB = AppLocalDatabase
    val volunteerDao = appLocalDB.db.getVolunteerDao()
    val volunteer: LiveData<MutableList<Volunteer>> = volunteerDao.getAllvolunteers()

    @WorkerThread
    suspend fun addVolunteer(volunteer: Volunteer) {
        volunteerDao.addvolunteer(volunteer)
    }

    @WorkerThread
    suspend fun updateVolunteer(volunteer: Volunteer) {
        volunteerDao.updatevolunteer(volunteer)
    }

    @WorkerThread
    suspend fun deleteAllVolunteers() {
        volunteerDao.deleteAllvolunteers()
    }
}