package com.example.hybridcryptographywithfirebase.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.hybridcryptographywithfirebase.utils.Constants
import com.example.hybridcryptographywithfirebase.utils.Constants.CRYPTOGRAPHY
import com.example.hybridcryptographywithfirebase.utils.TAG
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainViewModel: ViewModel() {
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
}