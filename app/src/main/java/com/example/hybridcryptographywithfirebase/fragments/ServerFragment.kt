package com.example.hybridcryptographywithfirebase.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.hybridcryptographywithfirebase.databinding.FragmentServerBinding
import com.example.hybridcryptographywithfirebase.models.EncryptedMessage
import com.example.hybridcryptographywithfirebase.utils.Constants.SERVER_PRIVATE_KEY
import com.example.hybridcryptographywithfirebase.utils.Constants.SERVER_PUBLIC_KEY
import com.example.hybridcryptographywithfirebase.utils.TAG
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

        viewModel.privateKeyOfServer.observe(viewLifecycleOwner) { privateKey ->
            saveInSharedPreference(SERVER_PRIVATE_KEY, privateKey).also {
                Log.d(TAG, "Private key is $privateKey")
            }
        }

        viewModel.publicKeyOfServer.observe(viewLifecycleOwner) { publicKey ->
            saveInSharedPreference(SERVER_PUBLIC_KEY, publicKey).also {
                Log.d(TAG, "Public key is $publicKey")
            }
        }

        requireActivity().getPreferences(Context.MODE_PRIVATE).apply {
            if(getString(SERVER_PUBLIC_KEY, "").isNullOrEmpty() && getString(SERVER_PRIVATE_KEY, "").isNullOrEmpty()) viewModel.generateServerKeyPair().also {
                Log.d(TAG, "Generating public key")
            }
        }

        binding.subtraction.setOnClickListener {
            requireActivity().getPreferences(Context.MODE_PRIVATE).apply {
                val privateKey: String = getString(SERVER_PRIVATE_KEY, "") ?: return@setOnClickListener

                if(privateKey.isNotEmpty()) {
                    lifecycleScope.launch {
                        val encryptedMessage: EncryptedMessage = viewModel.getEncryptedMessageFromFirebase()
                        viewModel.decryptMessage(privateKey, encryptedMessage.encryptedSecretKey, encryptedMessage.encryptedMessage)
                    }
                }
            }
        }
    }

    private fun saveInSharedPreference(keyType: String, keyValue: String) {
        requireActivity().getPreferences(Context.MODE_PRIVATE).apply {
            with(this.edit()) {
                putString(keyType, keyValue)
                apply()
            }
        }
    }
}