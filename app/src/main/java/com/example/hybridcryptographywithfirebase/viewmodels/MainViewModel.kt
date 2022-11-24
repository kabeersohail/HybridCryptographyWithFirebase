package com.example.hybridcryptographywithfirebase.viewmodels

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hybridcryptographywithfirebase.utils.Constants
import com.example.hybridcryptographywithfirebase.utils.Constants.CRYPTOGRAPHY
import com.example.hybridcryptographywithfirebase.utils.Constants.CRYPTO_ALGORITHM
import com.example.hybridcryptographywithfirebase.utils.Constants.KEY_SIZE
import com.example.hybridcryptographywithfirebase.utils.TAG
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.security.KeyPair
import java.security.KeyPairGenerator
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainViewModel: ViewModel() {

    private val _privateKeyOfServer: MutableLiveData<String> = MutableLiveData()
    val privateKeyOfServer: LiveData<String> = _privateKeyOfServer

    private val _publicKeyOfServer: MutableLiveData<String> = MutableLiveData()
    val publicKeyOfServer: LiveData<String> = _publicKeyOfServer

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    val databaseReference: DatabaseReference = firebaseDatabase.getReference(CRYPTOGRAPHY)

    /**
     * This method fetches user name available on Firebase node
     */
    suspend fun getCurrentValueFromFirebaseNode(): Long = suspendCoroutine { continuation ->
        databaseReference.child(Constants.VALUE).get().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                when(val currentValue = task.result?.value.also { Log.d(TAG, "$it") }) {
                    is Long -> continuation.resume(currentValue)
                    else -> Log.d(TAG, "Current value is null")
                }
            }
        }
    }

    fun generateServerKeyPair() {
        val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance(CRYPTO_ALGORITHM)
        keyPairGenerator.initialize(KEY_SIZE)
        val keyPair: KeyPair = keyPairGenerator.generateKeyPair()
        Base64.encodeToString(keyPair.private.encoded, Base64.DEFAULT).also { privateKey ->
            _privateKeyOfServer.postValue(privateKey)
        }

        Base64.encodeToString(keyPair.public.encoded, Base64.DEFAULT).also { publicKey ->
            _publicKeyOfServer.postValue(publicKey)
        }
    }
}