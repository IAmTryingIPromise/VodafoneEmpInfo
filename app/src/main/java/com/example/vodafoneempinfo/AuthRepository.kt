package com.example.vodafoneempinfo

import android.app.Activity
import android.content.Context
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.collections.first
import kotlin.collections.isNullOrEmpty
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepository @Inject constructor(
    private val context: Context
) {

    private var publicClientApp: IPublicClientApplication? = null

    private val scopes = listOf(
        "https://graph.microsoft.com/User.Read",
        "https://graph.microsoft.com/Sites.ReadWrite.All",
        "https://graph.microsoft.com/Files.ReadWrite.All"
    )

    suspend fun initializeMsal(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            PublicClientApplication.createMultipleAccountPublicClientApplication(
                context,
                R.raw.auth_config_multiple_account,
                object : IPublicClientApplication.IMultipleAccountApplicationCreatedListener {
                    override fun onCreated(application: IMultipleAccountPublicClientApplication?) {
                        publicClientApp = application
                        continuation.resume(true)
                    }

                    override fun onError(exception: MsalException?) {
                        continuation.resumeWithException(
                            exception ?: Exception("Failed to initialize MSAL")
                        )
                    }
                }
            )
        }
    }

    suspend fun acquireTokenSilent(): String? {
        return suspendCancellableCoroutine { continuation ->
            val app = publicClientApp as? IMultipleAccountPublicClientApplication
            if (app == null) {
                continuation.resumeWithException(Exception("MSAL not initialized"))
                return@suspendCancellableCoroutine
            }

            app.getAccounts(object : IPublicClientApplication.LoadAccountsCallback {
                override fun onTaskCompleted(result: MutableList<IAccount>?) {
                    if (result.isNullOrEmpty()) {
                        // No accounts found, need interactive sign-in
                        continuation.resumeWithException(Exception("No accounts found"))
                        return
                    }

                    val account = result.first()
                    val silentParameters = AcquireTokenSilentParameters.Builder()
                        .fromAuthority(account.authority)
                        .withScopes(scopes)
                        .forAccount(account)
                        .withCallback(object : SilentAuthenticationCallback {
                            override fun onSuccess(authenticationResult: IAuthenticationResult?) {
                                continuation.resume(authenticationResult?.accessToken)
                            }

                            override fun onError(exception: MsalException?) {
                                continuation.resumeWithException(
                                    exception ?: Exception("Silent token acquisition failed")
                                )
                            }
                        })
                        .build()

                    app.acquireTokenSilentAsync(silentParameters)
                }

                override fun onError(exception: MsalException?) {
                    continuation.resumeWithException(
                        exception ?: Exception("Failed to load accounts")
                    )
                }
            })
        }
    }

    suspend fun acquireTokenInteractive(): String? {
        return suspendCancellableCoroutine { continuation ->
            val app = publicClientApp as? IMultipleAccountPublicClientApplication

            if (app == null) {
                continuation.resumeWithException(Exception("MSAL not initialized"))
                return@suspendCancellableCoroutine
            }

            // Use the context directly - it should be an Activity context from the ActivityComponent
            val activity = context as? Activity
            if (activity == null) {
                continuation.resumeWithException(Exception("Context is not an Activity"))
                return@suspendCancellableCoroutine
            }

            val parameters = AcquireTokenParameters.Builder()
                .startAuthorizationFromActivity(activity)
                .withScopes(scopes)
                .withCallback(object : AuthenticationCallback {
                    override fun onSuccess(authenticationResult: IAuthenticationResult?) {
                        continuation.resume(authenticationResult?.accessToken)
                    }

                    override fun onError(exception: MsalException?) {
                        continuation.resumeWithException(
                            exception ?: Exception("Interactive token acquisition failed")
                        )
                    }

                    override fun onCancel() {
                        continuation.resumeWithException(Exception("Token acquisition cancelled"))
                    }
                })
                .build()

            app.acquireToken(parameters)
        }
    }

    suspend fun signOut() {
        return suspendCancellableCoroutine { continuation ->
            val app = publicClientApp as? IMultipleAccountPublicClientApplication
            if (app == null) {
                continuation.resume(Unit)
                return@suspendCancellableCoroutine
            }

            app.getAccounts(object : IPublicClientApplication.LoadAccountsCallback {
                override fun onTaskCompleted(result: MutableList<IAccount>?) {
                    result?.forEach { account ->
                        app.removeAccount(
                            account,
                            object : IMultipleAccountPublicClientApplication.RemoveAccountCallback {
                                override fun onRemoved() {
                                    continuation.resume(Unit)
                                }

                                override fun onError(exception: MsalException) {
                                    continuation.resumeWithException(exception)
                                }
                            })
                    }
                    if (result.isNullOrEmpty()) {
                        continuation.resume(Unit)
                    }
                }

                override fun onError(exception: MsalException?) {
                    continuation.resumeWithException(exception ?: Exception("Failed to sign out"))
                }
            })
        }
    }

    // Method for getting access token - primarily tries silent authentication
    suspend fun getAccessToken(): String? {
        return try {
            // Try silent token acquisition first
            acquireTokenSilent()
        } catch (e: Exception) {
            // For Excel repository, we'll only try silent authentication
            // Interactive authentication should be handled through the main UI flow
            throw Exception("Authentication required. Please sign in through the main screen.")
        }
    }
}