package com.example.hybridcryptographywithfirebase.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.hybridcryptographywithfirebase.databinding.FragmentClientBinding
import com.example.hybridcryptographywithfirebase.viewmodels.MainViewModel
import kotlinx.coroutines.launch

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
                lifecycleScope.launch {
                    viewModel.encryptMessage(viewModel.getServerPublicKey(), inputMessage)
                }
            }
        }

        viewModel.encryptedMessage.observe(viewLifecycleOwner) { encryptedMessage ->
            viewModel.postEncryptedMessage(encryptedMessage)
        }
    }
}