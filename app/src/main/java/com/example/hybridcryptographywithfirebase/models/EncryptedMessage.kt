package com.example.hybridcryptographywithfirebase.models

data class EncryptedMessage(
    val encryptedSecretKey: String,
    val encryptedMessage: String
)
