package com.example.hybridcryptographywithfirebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.hybridcryptographywithfirebase.databinding.ActivityMainBinding
import com.example.hybridcryptographywithfirebase.utils.Constants.CRYPTOGRAPHY
import com.example.hybridcryptographywithfirebase.utils.Constants.VALUE
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var currentValue: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val databaseReference: DatabaseReference = database.getReference(CRYPTOGRAPHY)

        binding.add.setOnClickListener {
            currentValue += 1
            databaseReference.child(VALUE).setValue(currentValue)
        }

    }
}