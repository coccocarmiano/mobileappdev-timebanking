package com.example.drawerexample.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.drawerexample.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.net.URL


class UserViewModel(val app : Application) : AndroidViewModel(app) {

    // Class Fields
    val fullname = MutableLiveData<String>()
    val username = MutableLiveData<String>()
    val location = MutableLiveData<String>()
    val email    = MutableLiveData<String>()
    val userID   = MutableLiveData<String?>(Firebase.auth.currentUser?.uid)
    val skills   = MutableLiveData<List<String>>()
    val propic   = MutableLiveData<Bitmap>()
    private val nullUID = "null"

    private val db = FirebaseFirestore.getInstance()
    private var userDocumentReference = db.collection("users").document(userID.value ?: nullUID)

    init {
        userDocumentReference.get().addOnFailureListener { createDefaultUserDocument() }
        userID.observeForever { uid ->
            uid?.also {
                userDocumentReference = db.collection("users").document(it)
                userDocumentReference.addSnapshotListener { doc, err ->
                    when {
                        err != null -> Log.w("UserViewModel", "Listen failed.", err)
                        doc != null -> {
                            updateUserFromDocument(doc)
                        }
                    }
                }
                updateProfilePicture()
            }
        }
    }

    fun loadUser(userID: String) {
        this.userID.value = userID
    }

    private fun createDefaultUserDocument() {
        val currentUser = Firebase.auth.currentUser
        val newUID = currentUser?.uid
        val userHashMap = hashMapOf(
            "fullName" to  ( currentUser?.displayName ?: app.getString(R.string.username_placeholder_text) ),
            "username" to app.getString(R.string.username_placeholder_text),
            "email" to ( currentUser?.email ?: app.getString(R.string.email_placeholder_text) ),
            "location" to app.getString(R.string.location_placeholder_text),
            "skills" to listOf<String>(),
        )

        newUID?.also {
            userDocumentReference = db.collection("users").document(newUID)
            userDocumentReference.set(userHashMap).addOnFailureListener {
                Log.w("UserViewModel", "Failed to create user document", it)
            }
            uploadDefaultPhoto()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateUserFromDocument(doc : DocumentSnapshot) {
        doc.run {
            fullname.value = getString("fullName")
            username.value = getString("username")
            location.value = getString("location")
            email.value = getString("email")
            skills.value = get("skills") as? List<String> ?: listOf()
            updateProfilePicture()
        }
    }

    fun applyChangesToFirebase() {
        val userHashMap = hashMapOf(
            "fullName" to fullname.value, 
            "username" to username.value,
            "email" to email.value,
            "location" to location.value,
            "skills" to skills.value,
        )
        userDocumentReference.update(userHashMap)
    }

    fun updateSkills(skills : List<String>) {
        this.skills.value = skills
        applyChangesToFirebase()
    }

    private fun compressBitmap(bmp: Bitmap): ByteArray{
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 70, baos)
        return baos.toByteArray()
    }

    private fun updateProfilePicture() {
        // First we should try getting it from the Firebase Storage
        val storageRef = Firebase.storage.reference
        storageRef.child("users_profile_pictures/${userID.value}").downloadUrl
            .addOnSuccessListener { uri ->
                updateProfilePictureFromURI(uri)
            }
            .addOnFailureListener {
                // If that fails, we should try getting it from Google Account
                // TODO: Ensure we're not getting the wrong user PFP here...
                val googleAccountRef = Firebase.auth.currentUser
                when (googleAccountRef?.photoUrl) {
                    null -> {
                        googleAccountRef?.photoUrl?.also { Uri ->
                            val url = URL(Uri.toString())
                            val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                            val compressed = compressBitmap(bmp)
                            Firebase.storage.reference.child("users_profile_pictures/${userID.value}").putBytes(compressed)
                                .addOnFailureListener {
                                    Log.w("UserViewModel", "Failed to upload profile picture", it)
                                }
                        }
                    }
                    else -> uploadDefaultPhoto()
                }
            }
    }

    private fun uploadDefaultPhoto() {
        val bmp = BitmapFactory.decodeResource(app.resources, R.drawable.default_pfp)
        val compressed = compressBitmap(bmp)
        Firebase.storage.reference.child("users_profile_pictures/${userID.value}").putBytes(compressed)
            .addOnFailureListener {
                Log.w("UserViewModel", "Failed to upload profile picture", it)
            }
    }

    fun uploadPhoto(bmp : Bitmap) {
        val compressed = compressBitmap(bmp)
        Firebase.storage.reference
            .child("users_profile_pictures/${userID.value}")
            .putBytes(compressed)
            .addOnFailureListener {
                Log.w("UserViewModel", "Failed to upload profile picture", it)
                // TODO Add user warning for failure
            }
    }

    private fun updateProfilePictureFromURI(uri : Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            URL(uri.toString()).let { BitmapFactory.decodeStream(it.openConnection().getInputStream()) }
                .also { viewModelScope.launch { propic.value = it } }
        }
    }
}

