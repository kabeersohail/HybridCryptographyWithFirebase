package com.example.hybridcryptographywithfirebase.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hybridcryptographywithfirebase.models.ViewPagerItem
import com.example.hybridcryptographywithfirebase.utils.Constants.View_Pager
import com.example.hybridcryptographywithfirebase.utils.TAG
import com.google.firebase.database.*

class MainViewModel: ViewModel() {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val viewPagerReference: DatabaseReference = firebaseDatabase.getReference(View_Pager)

    private val _broadcastMessages: MutableLiveData<ViewPagerItem> = MutableLiveData()
    val broadcastMessages: LiveData<ViewPagerItem> = _broadcastMessages

    private val viewPagerValueEventListener: ValueEventListener = object: ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val data: ViewPagerItem = snapshot.getValue(ViewPagerItem::class.java) ?: run {
                Log.d(TAG, "Failed to convert snapshot to ViewPagerItem")
                return
            }

            Log.d(TAG, "$data")
            _broadcastMessages.postValue(data)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.d(TAG, error.message)
        }
    }

    fun writeSchema() = viewPagerReference.setValue(ViewPagerItem())

    fun listenToViewPagerNode() = viewPagerReference.addValueEventListener(viewPagerValueEventListener)

}