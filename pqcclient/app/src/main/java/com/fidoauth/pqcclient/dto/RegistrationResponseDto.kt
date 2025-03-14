package com.fidoauth.pqcclient.dto

data class RegistrationResponseDto(
    val userid: Long,
    val challenge: String,
    val publicKeyRSA: String,
    val signatureRSA: String,
    val publicKeyDilithium: String,
    val signatureDilithium: String
)
