package com.example.hybridcryptographywithfirebase.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.hybridcryptographywithfirebase.databinding.FragmentClientBinding
import com.example.hybridcryptographywithfirebase.utils.Constants.SERVER_PUBLIC_KEY
import com.example.hybridcryptographywithfirebase.utils.TAG
import com.example.hybridcryptographywithfirebase.viewmodels.MainViewModel

class ClientFragment : Fragment() {

    private lateinit var binding: FragmentClientBinding
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.encryptAndSend.setOnClickListener {

            val inputMessage: String = binding.messageToBeEncrypted.text.toString()

            requireActivity().getPreferences(Context.MODE_PRIVATE).apply {

                val serverPublicKey: String = getString(SERVER_PUBLIC_KEY, "") ?: run {
                    Log.d(TAG, "Server's public key is null")
                    return@setOnClickListener
                }

                viewModel.encryptMessage(serverPublicKey, inputMessage)
            }
        }

        viewModel.encryptedMessage.observe(viewLifecycleOwner) { encryptedMessage ->
            viewModel.postEncryptedMessage(encryptedMessage)
        }
    }
}