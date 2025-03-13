package com.connectify.connectifyNow.repositories.Organization

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.connectify.connectifyNow.models.Organization
import com.connectify.connectifyNow.models.dao.AppLocalDatabase

class LocalStoreOrganizationRepository {
    
    val appLocalDB = AppLocalDatabase
    val organizationDao = appLocalDB.db.getOrganizationDao()

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
    fun getAllOrganizations(): LiveData<MutableList<Organization>> {
        return organizationDao.getAllOrganizations()
    }
}