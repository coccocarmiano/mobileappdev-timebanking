package com.example.drawerexample.ui

import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.drawerexample.R
import com.example.drawerexample.databinding.ShowProfileFragmentBinding
import com.example.drawerexample.viewmodel.UserViewModel

class ShowProfile : Fragment() {

    private val userViewModel by viewModels<UserViewModel>()
    private lateinit var binding : ShowProfileFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ShowProfileFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val userIDToDisplay = arguments?.getString("UID")
        val allowEdit = arguments?.getBoolean("allowEdit") ?: true
        userIDToDisplay?.also { userViewModel.loadUser(it) }
        startListeningForChanges()

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })

        setHasOptionsMenu(allowEdit)
        return root
    }

    private fun startListeningForChanges() {
        userViewModel.fullname.observe(viewLifecycleOwner) {
            binding.fullNameTV.text = it
        }

        userViewModel.email.observe(viewLifecycleOwner) {
            binding.emailTV.text = it
        }

        userViewModel.location.observe(viewLifecycleOwner) {
            binding.locationTV.text = it
        }

        userViewModel.username.observe(viewLifecycleOwner) {
            binding.usernameTV.text = it
        }

        userViewModel.skills.observe(viewLifecycleOwner) {
            if (it.isEmpty())
                binding.skillsTV.text = getString(R.string.no_skills_placeholder_text)
            else
                binding.skillsTV.text = it.joinToString(", ")
        }

        userViewModel.propic.observe(viewLifecycleOwner) {
            binding.profileImageShowProfile.setImageBitmap(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_edit_only, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_edit -> {
                val b = Bundle()
                b.putBoolean("showCaller", true)
                findNavController().navigate(R.id.action_nav_show_profile_to_nav_edit_profile, b)
            }
            else -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

}