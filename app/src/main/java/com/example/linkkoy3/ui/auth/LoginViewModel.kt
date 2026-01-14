package com.example.linkkoy3.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkkoy3.data.AuthManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(
    private val authManager: AuthManager
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun onLoginSuccess(onNavigate: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please fill all fields"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid
                
                if (userId != null) {
                    authManager.saveToken(userId)
                    onNavigate()
                } else {
                    errorMessage = "Login failed"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
