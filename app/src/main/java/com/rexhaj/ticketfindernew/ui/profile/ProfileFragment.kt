package com.rexhaj.ticketfindernew.ui.profile

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.rexhaj.ticketfindernew.databinding.FragmentProfileBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult

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

        //val textView: TextView = binding.textProfile
        //profileViewModel.text.observe(viewLifecycleOwner) {
        //    textView.text = it
        //}


        // FIREBASE AUTH


        // If signed in, disable sign in / register button and replace with sign out button
        // also fill in account details
        updateAuthUI()

        // Handle signout button clicked
        binding.buttonSignout.setOnClickListener {
            Log.d(TAG, "signout button clicked")
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
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            Log.d(TAG, "Sign in failed!")
        }
    }

    override fun onPause() {
        super.onPause()
    }

    private fun updateAuthUI() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            binding.textViewEmailField.text = currentUser.email
            binding.buttonLoginOrRegister.setEnabled(false)
            binding.buttonSignout.setEnabled(true)
        } else {
            binding.textViewEmailField.text = "UNREGISTERED"
            binding.buttonLoginOrRegister.setEnabled(true)
            binding.buttonSignout.setEnabled(false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}