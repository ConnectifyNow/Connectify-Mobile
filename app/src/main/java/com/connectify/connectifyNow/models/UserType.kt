package com.connectify.connectifyNow.models;

enum class Type {
    VOLUNTEER,
    ORGANIZATION
}
data class UserType(val type: Type);