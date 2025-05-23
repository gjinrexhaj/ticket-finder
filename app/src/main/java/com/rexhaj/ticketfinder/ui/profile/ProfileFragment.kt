package com.rexhaj.ticketfinder.ui.profile

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rexhaj.ticketfinder.User
import com.rexhaj.ticketfinder.databinding.FragmentProfileBinding

private const val TAG = "ProfileFragment"


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root


        // FIREBASE AUTH

        // If signed in, disable sign in / register button and replace with sign out button
        // also fill in account details
        updateAuthUI()

        // Handle sign out button clicked
        binding.buttonSignout.setOnClickListener {
            Log.d(TAG, "signout button clicked, logging the user out")
            FirebaseAuth.getInstance().signOut()
            updateAuthUI()
        }

        // Manage sign in
        val signInLauncher = registerForActivityResult(
            FirebaseAuthUIActivityResultContract(),
        ) { res ->
            this.onSignInResult(res)
        }

        _binding!!.buttonLoginOrRegister.setOnClickListener {
            Log.d(TAG, "login/register button clicked")
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                //AuthUI.IdpConfig.PhoneBuilder().build(),
                //AuthUI.IdpConfig.GoogleBuilder().build(),
                //AuthUI.IdpConfig.FacebookBuilder().build(),
                //AuthUI.IdpConfig.TwitterBuilder().build(),
            )

            // Create and launch sign-in intent
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
            signInLauncher.launch(signInIntent)

        }

        return root
    }


    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse

        if (result.resultCode == Activity.RESULT_OK) {
            // Successfully signed in
            Log.d(TAG, "Sign in success!")
            updateAuthUI()
        } else {
            // Sign in failed
            Log.d(TAG, "Sign in failed!")
        }
    }

    override fun onPause() {
        super.onPause()
    }

    private fun updateAuthUI() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        // if successfully signed in, show user info in view
        if (currentUser != null) {
            // if successful sign in, create user on firestore
            handleAccountCreation()
            binding.textViewEmailField.text = currentUser.email
            binding.buttonLoginOrRegister.setEnabled(false)
            binding.buttonSignout.setEnabled(true)

        } else {
            FirebaseAuth.getInstance().signOut()
            binding.textViewEmailField.text = "When you sign in, your email will show up here"
            binding.buttonLoginOrRegister.setEnabled(true)
            binding.buttonSignout.setEnabled(false)
        }
    }

    private fun handleAccountCreation() {
        // check if uid exists for user in db, create db entry for account if not
        val currentUser = FirebaseAuth.getInstance().currentUser

        val docRef = FirebaseFirestore.getInstance().collection("users").document(currentUser!!.uid)
        docRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Value exists
                Log.d(TAG, "uid exists, no account creation needed!")
            } else {
                // Value does not exist
                Log.d(TAG, "uid DNE, creating account")
                if (currentUser.metadata?.creationTimestamp == currentUser.metadata?.lastSignInTimestamp) {
                    Log.d(TAG, "ACCOUNT HAS BEEN CREATED")
                    val db = FirebaseFirestore.getInstance()


                    Log.d(TAG, "storing data: ${currentUser.email}, ${currentUser.uid}, null")
                    val userData = User(currentUser.email, currentUser.uid, mutableListOf())

                    // Add user data to Firestore
                    db.collection("users").document(currentUser.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            // Data stored successfully
                            Log.d(TAG, "User successfully created in db")
                        }
                        .addOnFailureListener { e ->
                            // Handle the error
                            Log.w(TAG, "User db creation failed!")
                        }
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}