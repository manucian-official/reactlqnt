package com.example.data.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

/**
 * BiometricHelper manages system-level biometric identification (Fingerprint, Face unlock).
 */
object BiometricHelper {

    fun isBiometricAvailable(context: Context): BiometricStatus {
        return try {
            val biometricManager = BiometricManager.from(context)
            val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            
            when (biometricManager.canAuthenticate(authenticators)) {
                BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.UNAVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
                else -> BiometricStatus.UNAVAILABLE
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            BiometricStatus.UNAVAILABLE
        }
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Xác thực bảo mật",
        subtitle: String = "Sử dụng dấu vân tay hoặc khuôn mặt của bạn",
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit,
        onFailed: () -> Unit
    ) {
        try {
            val executor: Executor = ContextCompat.getMainExecutor(activity)
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess(result)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }

            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build()

            biometricPrompt.authenticate(promptInfo)
        } catch (t: Throwable) {
            t.printStackTrace()
            onError(-1, t.message ?: "Lỗi khởi tạo sinh trắc học")
        }
    }

    enum class BiometricStatus {
        AVAILABLE,
        NO_HARDWARE,
        UNAVAILABLE,
        NOT_ENROLLED
    }
}
