package com.connectify.connectifyNow.models.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.connectify.connectifyNow.models.Organization

@Dao
interface OrganizationDao {
    @Query("SELECT * FROM organization")
    fun getAllOrganizations(): LiveData<MutableList<Organization>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(organization: Organization)

    @Delete
    fun delete(organization: Organization)

    @Query("DELETE FROM organization")
    fun deleteAllOrganizations()

    @Update
    fun update(organization: Organization)
}
