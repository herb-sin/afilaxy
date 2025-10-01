package com.afilaxy.domain.model

data class User(
    val uid: String,
    val email: String,
    val name: String? = null,
    val fcmToken: String? = null,
    val isHelper: Boolean = false
)