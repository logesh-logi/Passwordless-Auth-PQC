package com.fidoauth.pqcclient.dto

data class LoginRequestDto(
    val userid: String,
    val challenge: String
)
