package com.connectify.connectifyNow.models;


sealed class UserInfo {
    data class UserVolunteer(val volunteerInfo: Volunteer?) : UserInfo()
    data class UserOrganization(val organizationInfo: Organization?) : UserInfo()
}