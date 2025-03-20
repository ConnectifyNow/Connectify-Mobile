package com.connectify.connectifyNow.repositories.Organization

import androidx.annotation.WorkerThread
import com.connectify.connectifyNow.models.Organization
import com.connectify.connectifyNow.models.dao.AppLocalDatabase

class LocalStoreOrganizationRepository {
    
    private val appLocalDB = AppLocalDatabase
    private val organizationDao = appLocalDB.db.getOrganizationDao()

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
}