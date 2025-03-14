package com.fidoauth.pqcclient.dto

data class RegistrationRequestDto(
    val challenge: String,
    val userid: Long
)