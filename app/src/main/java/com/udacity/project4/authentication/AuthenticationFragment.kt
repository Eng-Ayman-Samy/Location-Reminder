package com.udacity.project4.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentAuthenticationBinding


class AuthenticationFragment : Fragment() {

    private lateinit var binding: FragmentAuthenticationBinding
    private val viewModel by viewModels<AuthenticationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthenticationBinding.inflate(layoutInflater)

        //UserAuthenticationState.observe(viewLifecycleOwner) {}
        viewModel.firebaseUser.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(
                    AuthenticationFragmentDirections.actionAuthenticationFragmentToReminderListFragment()
                )
            }
        }

        binding.login.setOnClickListener {
            launchSignIn()
        }

        return binding.root
    }

    //
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    private fun launchSignIn() {

        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.map) // Set logo drawable
            .setTheme(R.style.AppTheme) // Set theme
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
//        val response = result.idpResponse
//        if (result.resultCode == RESULT_OK) {
//            // Successfully signed in
//            //val user = FirebaseAuth.getInstance().currentUser
//            //launchRemindersActivity(this)
//        } else {
//            // Sign in failed. If response is null the user canceled the
//            // sign-in flow using the back button. Otherwise check
//            // response.getError().getErrorCode() and handle the error.
//            // ...
//
//        }
    }

}