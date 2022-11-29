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
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.example.hybridcryptographywithfirebase.R
import com.example.hybridcryptographywithfirebase.databinding.FragmentHomeBinding
import com.example.hybridcryptographywithfirebase.models.SingleItem
import com.example.hybridcryptographywithfirebase.models.Status
import com.example.hybridcryptographywithfirebase.utils.Constants.ENCODING
import com.example.hybridcryptographywithfirebase.utils.Constants.MIME_TYPE
import com.example.hybridcryptographywithfirebase.utils.TAG
import com.example.hybridcryptographywithfirebase.viewmodels.MainViewModel
import com.example.hybridcryptographywithfirebase.viewpager.PagerAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val viewModel: MainViewModel by activityViewModels()

    private val broadcastMessages: MutableList<SingleItem> = mutableListOf()

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

                    if(viewModel.listener == null) viewModel.listenToViewPagerNode()
                }
                false -> Toast.makeText(requireContext(), "Write to node failed", Toast.LENGTH_SHORT).show()
            }
        }

        val viewPager: ViewPager = binding.pager
        val views: MutableList<View> = ArrayList()

        val pagerAdapter = PagerAdapter(views).also {
            viewPager.adapter = it
        }

        viewModel.broadcastMessages.observe(viewLifecycleOwner) { singleItem ->

            broadcastMessages.add(singleItem)

            Log.d(TAG, "$broadcastMessages")

            val webView: WebView = LayoutInflater.from(requireContext()).inflate(R.layout.single_item, ConstraintLayout(requireContext()), false) as WebView
            webView.loadData(singleItem.htmlPage, MIME_TYPE , ENCODING)
            views.add(views.size, webView)
            pagerAdapter.notifyDataSetChanged()
        }

        binding.ack.setOnClickListener {
            try {
                /**
                 * On click of acknowledge button, update the list as well
                 */
                broadcastMessages[getCurrentItem(viewPager)].status = Status.ACKNOWLEDGED.ordinal

                Log.d(TAG, "$broadcastMessages")

                val size = getPagerAdapterSize(pagerAdapter)
                val currentItem = getCurrentItem(viewPager)

                Log.d(TAG, "[${currentItem}] $size")

                /**
                 * If current item is last item, finish the activity, else go to next page
                 */
                if(currentItem >=  size) {
                    requireActivity().finish()
                } else {
                    /**
                     * Enable previous button
                     */
                    binding.previous.visibility = View.VISIBLE
                    viewPager.currentItem = getNextItem(viewPager)
                }
            } catch (e: Exception) {
                throw e
            }
        }

        binding.previous.setOnClickListener {

            val size = getPagerAdapterSize(pagerAdapter)
            val previous = getPreviousItem(viewPager)

            Log.d(TAG, "[${previous}] $size")

            /**
             * If previous item is the first, hide previous button
             */
            if(previous == 0) binding.previous.visibility = View.INVISIBLE

            /**
             * If previous is already acknowledged, then make next button visible, and disable acknowledged button
             */
            if(broadcastMessages[previous].status == Status.ACKNOWLEDGED.ordinal) {
                binding.next.visibility = View.VISIBLE
                binding.ack.isEnabled = false
            }

            viewPager.currentItem = getPreviousItem(viewPager)
        }

        binding.next.setOnClickListener {

            /**
             * Make previous button enabled, if current item is already acknowledged
             */
            if(broadcastMessages[getCurrentItem(viewPager)].status == Status.ACKNOWLEDGED.ordinal) binding.previous.visibility = View.VISIBLE

            /**
             * If next item is the last item, then hide the next button
             */
            val size = getPagerAdapterSize(pagerAdapter)
            val nextItem = getNextItem(viewPager)

            if(nextItem == size) {
                binding.next.visibility = View.VISIBLE
            }

            if(nextItem > size) {
                binding.next.visibility = View.INVISIBLE
                binding.ack.isEnabled = true
            }

            /**
             * Enable Acknowledge button and hide next button, if the next item is not yet acknowledged
             */

            when(val next: SingleItem? = broadcastMessages.getOrNull(nextItem)) {
                null -> {
                    Log.d(TAG, "null")
                }
                else -> if(next.status == Status.NOT_ACKNOWLEDGED.ordinal) {
                    binding.next.visibility = View.INVISIBLE
                    binding.ack.isEnabled = true
                }
            }

            /**
             * OnClick of next button, Navigate to next page
             */
            viewPager.currentItem = getNextItem(viewPager)

        }

        binding.add.setOnClickListener {
            lifecycleScope.launch {
                viewModel.addPage()
            }
        }
    }

    private fun getCurrentItem(viewPager: ViewPager) = viewPager.currentItem

    private fun getPreviousItem(viewPager: ViewPager) = viewPager.currentItem - 1

    private fun getNextItem(viewPager: ViewPager) = viewPager.currentItem + 1

    private fun getPagerAdapterSize(pagerAdapter: PagerAdapter) = pagerAdapter.count - 1

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeValueEventListener()
    }

}