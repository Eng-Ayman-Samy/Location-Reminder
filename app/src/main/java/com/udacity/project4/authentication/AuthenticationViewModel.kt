package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel
import com.udacity.project4.utils.UserAuthenticationState

class AuthenticationViewModel :ViewModel(){
    val firebaseUser = UserAuthenticationState
}