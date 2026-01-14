package com.example.linkkoy3.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkkoy3.data.AuthManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterViewModel(
    private val authManager: AuthManager
) : ViewModel() {

    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun onRegister(onSuccess: () -> Unit) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage = "Please fill all fields"
            return
        }

        if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                // 1. Firebase Auth ile kayıt oluştur
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid

                if (userId != null) {
                    // 2. Kullanıcı bilgilerini Firestore'a kaydet
                    val userMap = mapOf(
                        "uid" to userId,
                        "name" to name,
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )
                    firestore.collection("users").document(userId).set(userMap).await()
                    
                    // 3. Token niyetine UID'yi kaydedebiliriz (mevcut yapıyı bozmamak için)
                    authManager.saveToken(userId)
                    onSuccess()
                } else {
                    errorMessage = "Registration failed"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
