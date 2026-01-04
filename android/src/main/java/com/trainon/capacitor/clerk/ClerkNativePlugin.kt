package com.trainon.capacitor.clerk

import android.util.Log
import com.clerk.android.Clerk
import com.clerk.android.models.Session
import com.clerk.android.models.SignIn
import com.clerk.android.models.SignUp
import com.clerk.android.models.User
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@CapacitorPlugin(name = "ClerkNative")
class ClerkNativePlugin : Plugin() {
    
    private val TAG = "ClerkNativePlugin"
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isConfigured = false

    @PluginMethod
    fun configure(call: PluginCall) {
        val publishableKey = call.getString("publishableKey")
        
        if (publishableKey.isNullOrEmpty()) {
            call.reject("Must provide publishableKey")
            return
        }

        try {
            // Initialize Clerk with the publishable key
            Clerk.configure(context, publishableKey)
            isConfigured = true
            Log.d(TAG, "Clerk configured successfully")
            call.resolve()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to configure Clerk", e)
            call.reject("Failed to configure Clerk: ${e.message}")
        }
    }

    @PluginMethod
    fun load(call: PluginCall) {
        if (!isConfigured) {
            call.reject("Clerk not configured. Call configure() first.")
            return
        }

        scope.launch {
            try {
                val user = Clerk.user
                val result = JSObject()
                if (user != null) {
                    result.put("user", userToJSObject(user))
                } else {
                    result.put("user", null)
                }
                call.resolve(result)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load Clerk", e)
                call.reject("Failed to load Clerk: ${e.message}")
            }
        }
    }

    @PluginMethod
    fun signInWithEmail(call: PluginCall) {
        val email = call.getString("email")
        
        if (email.isNullOrEmpty()) {
            call.reject("Must provide email")
            return
        }

        scope.launch {
            try {
                val signIn = SignIn.create(identifier = email)
                signIn.onSuccess { si ->
                    // Prepare email code verification
                    si.prepareFirstFactor(
                        strategy = SignIn.PrepareFirstFactorStrategy.EmailCode(
                            emailAddressId = si.supportedFirstFactors
                                ?.filterIsInstance<SignIn.Factor.EmailCode>()
                                ?.firstOrNull()
                                ?.emailAddressId ?: ""
                        )
                    ).onSuccess {
                        val result = JSObject()
                        result.put("requiresCode", true)
                        call.resolve(result)
                    }.onFailure { error ->
                        call.reject("Failed to prepare email code: ${error.message}")
                    }
                }.onFailure { error ->
                    call.reject("Sign in with email failed: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sign in with email failed", e)
                call.reject("Sign in with email failed: ${e.message}")
            }
        }
    }

    @PluginMethod
    fun verifyEmailCode(call: PluginCall) {
        val code = call.getString("code")
        
        if (code.isNullOrEmpty()) {
            call.reject("Must provide code")
            return
        }

        scope.launch {
            try {
                val signIn = Clerk.client?.signIn
                if (signIn == null) {
                    call.reject("No active sign in session")
                    return@launch
                }

                signIn.attemptFirstFactor(
                    strategy = SignIn.AttemptFirstFactorStrategy.EmailCode(code = code)
                ).onSuccess { si ->
                    if (si.status == SignIn.Status.COMPLETE) {
                        // Set active session
                        Clerk.client?.activeSessions?.firstOrNull()?.let { session ->
                            Clerk.setActive(sessionId = session.id)
                        }
                        
                        val user = Clerk.user
                        val result = JSObject()
                        result.put("user", user?.let { userToJSObject(it) })
                        call.resolve(result)
                    } else {
                        call.reject("Verification incomplete")
                    }
                }.onFailure { error ->
                    call.reject("Email code verification failed: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Email code verification failed", e)
                call.reject("Email code verification failed: ${e.message}")
            }
        }
    }

    @PluginMethod
    fun signInWithPassword(call: PluginCall) {
        val email = call.getString("email")
        val password = call.getString("password")
        
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            call.reject("Must provide email and password")
            return
        }

        scope.launch {
            try {
                val signIn = SignIn.create(identifier = email)
                signIn.onSuccess { si ->
                    si.attemptFirstFactor(
                        strategy = SignIn.AttemptFirstFactorStrategy.Password(password = password)
                    ).onSuccess { completedSignIn ->
                        if (completedSignIn.status == SignIn.Status.COMPLETE) {
                            // Set active session
                            completedSignIn.createdSessionId?.let { sessionId ->
                                Clerk.setActive(sessionId = sessionId)
                            }
                            
                            // Get user after sign in
                            val user = Clerk.user
                            val result = JSObject()
                            result.put("user", user?.let { userToJSObject(it) })
                            call.resolve(result)
                        } else {
                            call.reject("Sign in incomplete")
                        }
                    }.onFailure { error ->
                        call.reject("Sign in failed: ${error.message}")
                    }
                }.onFailure { error ->
                    call.reject("Sign in failed: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sign in with password failed", e)
                call.reject("Sign in with password failed: ${e.message}")
            }
        }
    }

    @PluginMethod
    fun signUp(call: PluginCall) {
        val emailAddress = call.getString("emailAddress")
        val password = call.getString("password")
        val firstName = call.getString("firstName")
        val lastName = call.getString("lastName")
        
        if (emailAddress.isNullOrEmpty() || password.isNullOrEmpty()) {
            call.reject("Must provide emailAddress and password")
            return
        }

        scope.launch {
            try {
                val signUp = SignUp.create(
                    emailAddress = emailAddress,
                    password = password,
                    firstName = firstName,
                    lastName = lastName
                )
                
                signUp.onSuccess { su ->
                    when (su.status) {
                        SignUp.Status.COMPLETE -> {
                            // Sign up complete, set active session
                            su.createdSessionId?.let { sessionId ->
                                Clerk.setActive(sessionId = sessionId)
                            }
                            
                            val user = Clerk.user
                            val result = JSObject()
                            result.put("user", user?.let { userToJSObject(it) })
                            result.put("requiresVerification", false)
                            call.resolve(result)
                        }
                        SignUp.Status.MISSING_REQUIREMENTS -> {
                            // Needs email verification
                            su.prepareEmailAddressVerification().onSuccess {
                                val result = JSObject()
                                result.put("user", null)
                                result.put("requiresVerification", true)
                                call.resolve(result)
                            }.onFailure { error ->
                                call.reject("Failed to prepare email verification: ${error.message}")
                            }
                        }
                        else -> {
                            call.reject("Sign up incomplete: ${su.status}")
                        }
                    }
                }.onFailure { error ->
                    call.reject("Sign up failed: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sign up failed", e)
                call.reject("Sign up failed: ${e.message}")
            }
        }
    }

    @PluginMethod
    fun verifySignUpEmail(call: PluginCall) {
        val code = call.getString("code")
        
        if (code.isNullOrEmpty()) {
            call.reject("Must provide code")
            return
        }

        scope.launch {
            try {
                val signUp = Clerk.client?.signUp
                if (signUp == null) {
                    call.reject("No active sign up session")
                    return@launch
                }

                signUp.attemptEmailAddressVerification(code = code)
                    .onSuccess { su ->
                        if (su.status == SignUp.Status.COMPLETE) {
                            su.createdSessionId?.let { sessionId ->
                                Clerk.setActive(sessionId = sessionId)
                            }
                            
                            val user = Clerk.user
                            val result = JSObject()
                            result.put("user", user?.let { userToJSObject(it) })
                            call.resolve(result)
                        } else {
                            call.reject("Verification incomplete")
                        }
                    }.onFailure { error ->
                        call.reject("Email verification failed: ${error.message}")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Email verification failed", e)
                call.reject("Email verification failed: ${e.message}")
            }
        }
    }

    @PluginMethod
    fun getUser(call: PluginCall) {
        scope.launch {
            try {
                val user = Clerk.user
                val result = JSObject()
                if (user != null) {
                    result.put("user", userToJSObject(user))
                } else {
                    result.put("user", null)
                }
                call.resolve(result)
            } catch (e: Exception) {
                Log.e(TAG, "Get user failed", e)
                call.reject("Get user failed: ${e.message}")
            }
        }
    }

    @PluginMethod
    fun getToken(call: PluginCall) {
        scope.launch {
            try {
                val session = Clerk.session
                if (session == null) {
                    val result = JSObject()
                    result.put("token", null)
                    call.resolve(result)
                    return@launch
                }

                session.getToken().onSuccess { tokenResult ->
                    val result = JSObject()
                    result.put("token", tokenResult?.jwt)
                    call.resolve(result)
                }.onFailure { error ->
                    call.reject("Get token failed: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Get token failed", e)
                call.reject("Get token failed: ${e.message}")
            }
        }
    }

    @PluginMethod
    fun signOut(call: PluginCall) {
        scope.launch {
            try {
                val session = Clerk.session
                if (session != null) {
                    session.revoke().onSuccess {
                        call.resolve()
                    }.onFailure { error ->
                        call.reject("Sign out failed: ${error.message}")
                    }
                } else {
                    // No active session, consider it signed out
                    call.resolve()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sign out failed", e)
                call.reject("Sign out failed: ${e.message}")
            }
        }
    }

    @PluginMethod
    fun updateUser(call: PluginCall) {
        val firstName = call.getString("firstName")
        val lastName = call.getString("lastName")

        scope.launch {
            try {
                val user = Clerk.user
                if (user == null) {
                    call.reject("No user signed in")
                    return@launch
                }

                user.update(
                    firstName = firstName,
                    lastName = lastName
                ).onSuccess { updatedUser ->
                    val result = JSObject()
                    result.put("user", userToJSObject(updatedUser))
                    call.resolve(result)
                }.onFailure { error ->
                    call.reject("Update user failed: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Update user failed", e)
                call.reject("Update user failed: ${e.message}")
            }
        }
    }

    @PluginMethod
    fun requestPasswordReset(call: PluginCall) {
        val email = call.getString("email")
        
        if (email.isNullOrEmpty()) {
            call.reject("Must provide email")
            return
        }

        scope.launch {
            try {
                val signIn = SignIn.create(identifier = email)
                signIn.onSuccess { si ->
                    si.prepareFirstFactor(
                        strategy = SignIn.PrepareFirstFactorStrategy.ResetPasswordEmailCode(
                            emailAddressId = si.supportedFirstFactors
                                ?.filterIsInstance<SignIn.Factor.ResetPasswordEmailCode>()
                                ?.firstOrNull()
                                ?.emailAddressId ?: ""
                        )
                    ).onSuccess {
                        call.resolve()
                    }.onFailure { error ->
                        call.reject("Failed to request password reset: ${error.message}")
                    }
                }.onFailure { error ->
                    call.reject("Failed to request password reset: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Request password reset failed", e)
                call.reject("Request password reset failed: ${e.message}")
            }
        }
    }

    @PluginMethod
    fun resetPassword(call: PluginCall) {
        val code = call.getString("code")
        val newPassword = call.getString("newPassword")
        
        if (code.isNullOrEmpty() || newPassword.isNullOrEmpty()) {
            call.reject("Must provide code and newPassword")
            return
        }

        scope.launch {
            try {
                val signIn = Clerk.client?.signIn
                if (signIn == null) {
                    call.reject("No active sign in session")
                    return@launch
                }

                signIn.attemptFirstFactor(
                    strategy = SignIn.AttemptFirstFactorStrategy.ResetPasswordEmailCode(code = code)
                ).onSuccess { si ->
                    si.resetPassword(newPassword = newPassword).onSuccess { completedSignIn ->
                        if (completedSignIn.status == SignIn.Status.COMPLETE) {
                            completedSignIn.createdSessionId?.let { sessionId ->
                                Clerk.setActive(sessionId = sessionId)
                            }
                            call.resolve()
                        } else {
                            call.reject("Password reset incomplete")
                        }
                    }.onFailure { error ->
                        call.reject("Failed to reset password: ${error.message}")
                    }
                }.onFailure { error ->
                    call.reject("Failed to verify reset code: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Reset password failed", e)
                call.reject("Reset password failed: ${e.message}")
            }
        }
    }

    @PluginMethod
    fun refreshSession(call: PluginCall) {
        scope.launch {
            try {
                val session = Clerk.session
                if (session == null) {
                    val result = JSObject()
                    result.put("token", null)
                    call.resolve(result)
                    return@launch
                }

                // Force refresh the token
                session.getToken(forceRefresh = true).onSuccess { tokenResult ->
                    val result = JSObject()
                    result.put("token", tokenResult?.jwt)
                    call.resolve(result)
                }.onFailure { error ->
                    call.reject("Refresh session failed: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Refresh session failed", e)
                call.reject("Refresh session failed: ${e.message}")
            }
        }
    }

    private fun userToJSObject(user: User): JSObject {
        val jsObject = JSObject()
        jsObject.put("id", user.id)
        jsObject.put("firstName", user.firstName)
        jsObject.put("lastName", user.lastName)
        jsObject.put("emailAddress", user.primaryEmailAddress?.emailAddress)
        jsObject.put("imageUrl", user.imageUrl)
        jsObject.put("username", user.username)
        return jsObject
    }
}
