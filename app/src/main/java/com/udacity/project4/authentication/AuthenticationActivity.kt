package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.utils.UserAuthenticationState

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    lateinit var binding: ActivityAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)//R.layout.activity_authentication

        UserAuthenticationState.observe(this) {
            it?.let {
                finishWithLaunch(RemindersActivity::class.java)
            }
        }

        binding.login.setOnClickListener {
            launchSignIn()
        }

//        onBackPressedDispatcher.addCallback(this) {
//        }

//          TODO: a bonus is to customize the sign in flow to look nice using : -> done
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

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

//    override fun onStart() {
//        super.onStart()
//        UserAuthenticationState.value?.let {
//            this.finishWithLaunch(RemindersActivity::class.java)
//            //launchRemindersActivity(this)
//        }
//        Log.i("tag","${UserAuthenticationState.value}")
//    }

//    private fun launchRemindersActivity(act: Activity) {
//        act.finish()
//        val intent = Intent(act.baseContext, RemindersActivity::class.java)
//        startActivity(intent)
//    }
}

fun Activity.finishWithLaunch(newClass: Class<*>) {
    finish()
    val intent = Intent(this, newClass)
    startActivity(intent)
}

//// You must provide a custom layout XML resource and configure at least one
//// provider button ID. It's important that that you set the button ID for every provider
//// that you have enabled.
//AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
//    .Builder(R.layout.your_custom_layout_xml)
//    .setGoogleButtonId(R.id.bar)
//    .setEmailButtonId(R.id.foo)
//    // ...
//    .setTosAndPrivacyPolicyId(R.id.baz)
//    .build();
//
//Intent signInIntent =
//    AuthUI.getInstance(this).createSignInIntentBuilder()
//        // ...
//        .setAuthMethodPickerLayout(customLayout)
//        .build();