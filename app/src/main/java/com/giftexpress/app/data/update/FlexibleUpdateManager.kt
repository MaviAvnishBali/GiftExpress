package com.giftexpress.app.data.update

import android.app.Activity
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Google Play Flexible In-App Updates.
 *
 * Handles:
 * 1. Checking for available updates via AppUpdateManager.
 * 2. Starting the flexible update flow where the APK downloads in the background.
 * 3. Listening to download progress via InstallStateUpdatedListener.
 * 4. Notifying the UI when the update has finished downloading (InstallStatus.DOWNLOADED).
 * 5. Checking onResume() to catch updates that finished downloading while the app was backgrounded.
 * 6. Invoking completeUpdate() to restart the app and install the downloaded update.
 */
@Singleton
class FlexibleUpdateManager @Inject constructor(
    private val appUpdateManager: AppUpdateManager
) {

    companion object {
        private const val TAG = "FlexibleUpdateManager"
        const val REQUEST_CODE_FLEXIBLE_UPDATE = 5123
    }

    private val _updateStatus = MutableStateFlow<FlexibleUpdateStatus>(FlexibleUpdateStatus.Idle)
    val updateStatus: StateFlow<FlexibleUpdateStatus> = _updateStatus.asStateFlow()

    private var isListenerRegistered = false

    /**
     * Listener that receives progress updates and completion events
     * during a flexible update download.
     */
    val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytes = state.totalBytesToDownload()
                val percent = if (totalBytes > 0) {
                    (bytesDownloaded.toFloat() / totalBytes) * 100f
                } else {
                    0f
                }
                Log.d(TAG, "Downloading update: $bytesDownloaded / $totalBytes (${percent.toInt()}%)")
                _updateStatus.value = FlexibleUpdateStatus.Downloading(
                    bytesDownloaded = bytesDownloaded,
                    totalBytesToDownload = totalBytes,
                    progressPercent = percent
                )
            }
            InstallStatus.DOWNLOADED -> {
                Log.d(TAG, "Update download complete, ready for restart.")
                _updateStatus.value = FlexibleUpdateStatus.Downloaded
            }
            InstallStatus.FAILED -> {
                Log.e(TAG, "Update download failed with error code: ${state.installErrorCode()}")
                _updateStatus.value = FlexibleUpdateStatus.Failed(
                    errorCode = state.installErrorCode(),
                    message = "Flexible update download failed with error code ${state.installErrorCode()}"
                )
            }
            InstallStatus.CANCELED -> {
                Log.d(TAG, "Update download was canceled.")
                _updateStatus.value = FlexibleUpdateStatus.Canceled
            }
            InstallStatus.PENDING -> {
                Log.d(TAG, "Update download pending.")
            }
            InstallStatus.INSTALLING -> {
                Log.d(TAG, "Update is installing.")
            }
            InstallStatus.INSTALLED -> {
                Log.d(TAG, "Update installed successfully.")
                _updateStatus.value = FlexibleUpdateStatus.Idle
                unregisterListener()
            }
            else -> {
                Log.d(TAG, "Install status: ${state.installStatus()}")
            }
        }
    }

    /**
     * Registers the InstallStateUpdatedListener to receive download status updates.
     */
    fun registerListener() {
        if (!isListenerRegistered) {
            appUpdateManager.registerListener(installStateUpdatedListener)
            isListenerRegistered = true
            Log.d(TAG, "InstallStateUpdatedListener registered")
        }
    }

    /**
     * Unregisters the listener to prevent memory leaks.
     */
    fun unregisterListener() {
        if (isListenerRegistered) {
            appUpdateManager.unregisterListener(installStateUpdatedListener)
            isListenerRegistered = false
            Log.d(TAG, "InstallStateUpdatedListener unregistered")
        }
    }

    private val directExecutor = java.util.concurrent.Executor { it.run() }

    /**
     * Checks Google Play for available updates and starts the flexible update flow
     * if an update is available.
     *
     * @param activity The host Activity to start the update flow for result.
     * @param requestCode The request code used in onActivityResult (defaults to REQUEST_CODE_FLEXIBLE_UPDATE).
     */
    fun checkForUpdate(
        activity: Activity,
        requestCode: Int = REQUEST_CODE_FLEXIBLE_UPDATE
    ) {
        registerListener()
        _updateStatus.value = FlexibleUpdateStatus.Checking

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener(directExecutor) { appUpdateInfo ->
            handleAppUpdateInfo(activity, appUpdateInfo, requestCode)
        }.addOnFailureListener(directExecutor) { exception ->
            Log.w(TAG, "Failed to check for app update: ${exception.message}", exception)
            _updateStatus.value = FlexibleUpdateStatus.Failed(
                errorCode = -1,
                message = exception.message
            )
        }
    }

    /**
     * Internal handler for AppUpdateInfo result.
     */
    private fun handleAppUpdateInfo(
        activity: Activity,
        appUpdateInfo: AppUpdateInfo,
        requestCode: Int
    ) {
        val isUpdateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
        val isFlexibleAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)

        when {
            // Already downloaded and waiting for restart
            appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED -> {
                Log.d(TAG, "Update already downloaded, prompting to complete.")
                _updateStatus.value = FlexibleUpdateStatus.Downloaded
            }
            // Update available and flexible mode is supported
            isUpdateAvailable && isFlexibleAllowed -> {
                Log.d(TAG, "Flexible update available! Priority: ${appUpdateInfo.updatePriority()}, Staleness: ${appUpdateInfo.clientVersionStalenessDays()}")
                _updateStatus.value = FlexibleUpdateStatus.Available(
                    stalenessDays = appUpdateInfo.clientVersionStalenessDays(),
                    updatePriority = appUpdateInfo.updatePriority()
                )
                startFlexibleUpdate(activity, appUpdateInfo, requestCode)
            }
            else -> {
                Log.d(TAG, "No flexible update available (available: $isUpdateAvailable, allowed: $isFlexibleAllowed)")
                _updateStatus.value = FlexibleUpdateStatus.NotAvailable
            }
        }
    }

    /**
     * Starts the flexible update flow with Google Play dialogs.
     */
    fun startFlexibleUpdate(
        activity: Activity,
        appUpdateInfo: AppUpdateInfo,
        requestCode: Int = REQUEST_CODE_FLEXIBLE_UPDATE
    ) {
        val options = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                activity,
                options,
                requestCode
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start flexible update flow: ${e.message}", e)
            _updateStatus.value = FlexibleUpdateStatus.Failed(
                errorCode = -1,
                message = e.message
            )
        }
    }

    /**
     * Checks if a flexible update completed downloading while the app was in the background.
     * Should be called from Activity.onResume().
     */
    fun onResume() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener(directExecutor) { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                Log.d(TAG, "Update was downloaded while in background, notifying UI.")
                _updateStatus.value = FlexibleUpdateStatus.Downloaded
            }
        }.addOnFailureListener(directExecutor) { e ->
            Log.w(TAG, "Error checking update status in onResume: ${e.message}")
        }
    }

    /**
     * Completes the update by restarting the app and applying the downloaded update.
     */
    fun completeUpdate(): Task<Void> {
        Log.d(TAG, "Calling completeUpdate() to restart and install.")
        return appUpdateManager.completeUpdate()
    }
}
