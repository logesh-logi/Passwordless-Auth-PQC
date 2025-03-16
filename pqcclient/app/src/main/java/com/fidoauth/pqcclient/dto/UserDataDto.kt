package com.fidoauth.pqcclient.dto

data class UserDataDto(
    val username: String,
    val email: String,
    val publicKeyRSA: String,
    val publicKeyDilithium: String
)
