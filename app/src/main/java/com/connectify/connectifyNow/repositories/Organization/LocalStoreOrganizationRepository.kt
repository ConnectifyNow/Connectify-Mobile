package com.connectify.connectifyNow.repositories.Organization

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class LocalStoreOrganizationRepository {
    
    val appLocalDB = AppLocalDatabase
    val organizationDao = appLocalDB.db.getorganizationDao()

    @WorkerThread
    fun add(organization: Organization) {
        organizationDao.insert(organization)
    }

    @WorkerThread
    fun update(organization: Organization) {
        organizationDao.update(organization)
    }

    @WorkerThread
    fun delete(organization: Organization) {
        organizationDao.delete(organization)
    }

    @WorkerThread
    fun getAllCompanies(): LiveData<MutableList<Organization>> {
        return organizationDao.getAllCompanies()
    }
}