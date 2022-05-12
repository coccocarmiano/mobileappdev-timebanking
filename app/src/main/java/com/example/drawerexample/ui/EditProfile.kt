package com.example.drawerexample.ui

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.drawerexample.MainActivity
import com.example.drawerexample.R
import com.example.drawerexample.databinding.EditProfileFragmentBinding
import com.example.drawerexample.viewmodel.UserViewModel
import com.google.android.material.snackbar.Snackbar

class EditProfile : Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var binding : EditProfileFragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EditProfileFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setHasOptionsMenu(true)


        userViewModel.liveUser.observe(viewLifecycleOwner) { user ->
            user.run {
                fullname.let { binding.textInputEditFullName.setText(it) }
                username.let { binding.textInputEditUserName.setText(it) }
                mail.let { binding.textInputEditMail.setText(it) }
                location.let { binding.textInputEditUserLocation.setText(it) }
                username.let { binding.textInputEditUserName.setText(it) }
            }
        }

        userViewModel.livePicture.observe(viewLifecycleOwner) {
            binding.profileImageEditProfile.setImageBitmap(it)
        }

        binding.editProfileManageSkillsButton.setOnClickListener {
            findNavController().navigate(R.id.action_nav_edit_profile_to_nav_edit_skills)
        }

        binding.editProfileSaveButton.setOnClickListener {
            if (!allFieldsAreValid()) Snackbar.make(root, "Please fill all the fields", Snackbar.LENGTH_LONG).show()
            else {
                updateViewModelFields()
                userViewModel.pushChangesToFirebase()
                findNavController().popBackStack()
            }
        }

        binding.editProfileImageButton.setOnClickListener {
            val galleryIntent = Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT)
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val chooserIntent = Intent.createChooser(galleryIntent, "Upload an Image").run { putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent)) }
            @Suppress("DEPRECATION")
            activity?.startActivityForResult(chooserIntent, (activity as MainActivity).requestPhotoForProfileEdit)
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })

        requireActivity()
            .onBackPressedDispatcher
            .addCallback {
                findNavController().popBackStack()
            }
        return root
    }

    private fun allFieldsAreValid(): Boolean {
        return !(binding.textInputEditFullName.text.toString().isEmpty() ||
                binding.textInputEditMail.text.toString().isEmpty() ||
                binding.textInputEditUserLocation.text.toString().isEmpty() ||
                binding.textInputEditUserName.text.toString().isEmpty())
    }

    private fun updateViewModelFields() {
        userViewModel.liveUser.value?.run {
            fullname = binding.textInputEditFullName.text.toString()
            mail = binding.textInputEditMail.text.toString()
            username = binding.textInputEditUserName.text.toString()
            location = binding.textInputEditUserLocation.text.toString()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        findNavController().popBackStack()
        return true
    }

}