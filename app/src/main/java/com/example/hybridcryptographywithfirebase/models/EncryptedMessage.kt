package com.example.hybridcryptographywithfirebase.models

data class EncryptedMessage(val encryptedSecretKey: String, val encryptedMessage: String) {
    companion object {
        fun from(map: Map<String, String>) = object {
            val encryptedSecretKey by map
            val encryptedMessage by map

            val data = EncryptedMessage(encryptedSecretKey, encryptedMessage)
        }.data
    }
}
