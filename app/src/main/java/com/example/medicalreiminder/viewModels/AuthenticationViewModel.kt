package com.example.medicalreiminder.viewModels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.medicalreiminder.R
import com.example.medicalreiminder.model.UserModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthenticationViewModel : ViewModel() {
    val auth = Firebase.auth

    val firestore = Firebase.firestore

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun signUp(
        email: String,
        password: String,
        name: String,
        context: Context,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            onResult(false, context.getString(R.string.missing_signup_fields))
            return
        }
        _isLoading.value = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { Result ->
                if (Result.isSuccessful) {
                    val userId = Result.result.user?.uid.orEmpty()
                    val userModel = UserModel(userId, name, email)
                    firestore.collection("users")
                        .document(userId)
                        .set(userModel)
                        .addOnCompleteListener { dbResult ->
                            _isLoading.value = false
                            if (dbResult.isSuccessful) {
                                verifyEmail(context)
                                onResult(true, null)
                            } else {
                                onResult(false, dbResult.exception?.localizedMessage ?: context.getString(R.string.generic_error))
                            }

                        }

                } else {
                    _isLoading.value = false
                    onResult(false, Result.exception?.localizedMessage)
                }

            }
    }

    fun login(
        email: String,
        password: String,
        context: Context,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, context.getString(R.string.missing_login_fields))
            return
        }
        _isLoading.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                _isLoading.value = false
                if (it.isSuccessful) {
                   if (auth.currentUser!!.isEmailVerified) {
                        onResult(true, null)
                    } else {
                        onResult(false, context.getString(R.string.email_not_verified))
                    }
                } else {
                    onResult(false, it.exception?.localizedMessage)
                }
            }
    }

    fun sendPasswordResetEmail(email: String, context: Context) {
        if (email.isBlank()) {
            Toast.makeText(context, context.getString(R.string.password_reset_email_required), Toast.LENGTH_SHORT).show()
            return
        }

        _isLoading.value = true
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    Toast.makeText(context, context.getString(R.string.password_reset_sent), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, task.exception?.localizedMessage ?: context.getString(R.string.generic_error), Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun verifyEmail(context: Context) {
        auth.currentUser!!.sendEmailVerification()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, context.getString(R.string.check_email), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, context.getString(R.string.generic_error), Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun logOut() {
        auth.signOut()
    }
}
