package com.example.hybridcryptographywithfirebase.viewmodels

import androidx.lifecycle.ViewModel
import com.example.hybridcryptographywithfirebase.utils.Constants.CRYPTOGRAPHY
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainViewModel: ViewModel() {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseReference: DatabaseReference = firebaseDatabase.getReference(CRYPTOGRAPHY)

}