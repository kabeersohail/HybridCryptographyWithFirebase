package com.example.hybridcryptographywithfirebase.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager.widget.ViewPager
import com.example.hybridcryptographywithfirebase.R
import com.example.hybridcryptographywithfirebase.databinding.FragmentHomeBinding
import com.example.hybridcryptographywithfirebase.utils.Constants.ENCODING
import com.example.hybridcryptographywithfirebase.utils.Constants.MIME_TYPE
import com.example.hybridcryptographywithfirebase.utils.TAG
import com.example.hybridcryptographywithfirebase.viewmodels.MainViewModel
import com.example.hybridcryptographywithfirebase.viewpager.PagerAdapter

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         * Try write to firebase with initial schema
         */
        viewModel.writeSchema().addOnCompleteListener {
            when(it.isSuccessful) {
                true -> {
                    /**
                     * Once write is successful, start listening to firebase node
                     */
                    viewModel.listenToViewPagerNode()
                }
                false -> Toast.makeText(requireContext(), "Write to node failed", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.listener?.let {
            viewModel.listenToViewPagerNode()
        }

        val viewPager: ViewPager = binding.pager
        val views: MutableList<View> = ArrayList()

        val pagerAdapter = PagerAdapter(views).also {
            viewPager.adapter = it
        }

        viewModel.broadcastMessages.observe(viewLifecycleOwner) {

            val webView: WebView = LayoutInflater.from(requireContext()).inflate(R.layout.single_item, ConstraintLayout(requireContext()), false) as WebView
            webView.loadData(it.broadcastMessages.last(), MIME_TYPE , ENCODING)
            views.add(views.size, webView)

            pagerAdapter.notifyDataSetChanged()
        }

        binding.ack.setOnClickListener {
            try {
                val size = pagerAdapter.count - 1
                Log.d(TAG, "$size")

                val currentItem = viewPager.currentItem
                Log.d(TAG, "$currentItem")
                if(currentItem >=  size) {
                    requireActivity().finish()
                } else {
                    viewPager.currentItem = viewPager.currentItem + 1
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeValueEventListener()
    }

}