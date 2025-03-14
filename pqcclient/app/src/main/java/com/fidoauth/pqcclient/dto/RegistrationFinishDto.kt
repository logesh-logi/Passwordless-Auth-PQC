package com.fidoauth.pqcclient.dto

import android.media.session.MediaSession.Token

data class RegistrationFinishDto(
    val success: String,
    val message: String,
    val token: String
)
