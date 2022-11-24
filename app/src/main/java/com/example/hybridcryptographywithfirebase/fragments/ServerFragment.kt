package com.example.hybridcryptographywithfirebase.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.hybridcryptographywithfirebase.databinding.FragmentServerBinding
import com.example.hybridcryptographywithfirebase.utils.Constants.VALUE
import com.example.hybridcryptographywithfirebase.viewmodels.MainViewModel
import kotlinx.coroutines.launch

class ServerFragment : Fragment() {

    private lateinit var binding: FragmentServerBinding

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentServerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.subtraction.setOnClickListener {
            lifecycleScope.launch {
                viewModel.apply { databaseReference.child(VALUE).setValue(getCurrentValueFromFirebaseNode() - 1) }
            }
        }
    }

}