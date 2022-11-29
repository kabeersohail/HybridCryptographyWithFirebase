package com.example.hybridcryptographywithfirebase.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hybridcryptographywithfirebase.models.SingleItem
import com.example.hybridcryptographywithfirebase.utils.Constants.HTML_PAGE
import com.example.hybridcryptographywithfirebase.utils.Constants.View_Pager
import com.example.hybridcryptographywithfirebase.utils.TAG
import com.google.firebase.database.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainViewModel: ViewModel() {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val viewPagerReference: DatabaseReference = firebaseDatabase.getReference(View_Pager)

    var listener: ValueEventListener? = null

    private val _broadcastMessages: MutableLiveData<SingleItem> = MutableLiveData()
    val broadcastMessages: LiveData<SingleItem> = _broadcastMessages

    private val viewPagerValueEventListener: ValueEventListener = object: ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val data: SingleItem = snapshot.getValue(SingleItem::class.java) ?: run {
                Log.d(TAG, "Failed to convert snapshot to ViewPagerItem")
                return
            }

            _broadcastMessages.postValue(data)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.d(TAG, error.message)
        }
    }

    fun writeSchema() = viewPagerReference.setValue(SingleItem())

    fun listenToViewPagerNode() { listener = viewPagerReference.addValueEventListener(viewPagerValueEventListener) }

    fun removeValueEventListener() = viewPagerReference.removeEventListener(viewPagerValueEventListener)

    suspend fun addPage() = viewPagerReference.child(HTML_PAGE).setValue((getCurrentValue() + 1).toString())

    private suspend fun getCurrentValue() = suspendCoroutine { continuation ->
        viewPagerReference.child(HTML_PAGE).get().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                when(val value = task.result?.value) {
                    is String -> continuation.resume(value.toInt())
                    else -> Log.d(TAG, "error")
                }
            }
        }
    }

}