package com.fidoauth.pqcclient.dto

data class LoginResponseDto(
    val userid: String,
    val signatureRSA: String,
    val signatureDilithium: String
)
