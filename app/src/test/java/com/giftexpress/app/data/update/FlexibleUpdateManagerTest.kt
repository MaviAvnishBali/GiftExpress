package com.giftexpress.app.data.update

import android.app.Activity
import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallErrorCode
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class FlexibleUpdateManagerTest {

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var flexibleUpdateManager: FlexibleUpdateManager
    private lateinit var mockActivity: Activity

    @Before
    fun setUp() {
        appUpdateManager = mock(AppUpdateManager::class.java)
        mockActivity = mock(Activity::class.java)
        flexibleUpdateManager = FlexibleUpdateManager(appUpdateManager)
    }

    @Test
    fun `initial status is Idle`() {
        assertEquals(FlexibleUpdateStatus.Idle, flexibleUpdateManager.updateStatus.value)
    }

    @Test
    fun `registerListener registers listener on AppUpdateManager only once`() {
        flexibleUpdateManager.registerListener()
        flexibleUpdateManager.registerListener()

        verify(appUpdateManager, times(1)).registerListener(flexibleUpdateManager.installStateUpdatedListener)
    }

    @Test
    fun `unregisterListener unregisters listener on AppUpdateManager only when registered`() {
        flexibleUpdateManager.registerListener()
        flexibleUpdateManager.unregisterListener()
        flexibleUpdateManager.unregisterListener()

        verify(appUpdateManager, times(1)).unregisterListener(flexibleUpdateManager.installStateUpdatedListener)
    }

    @Test
    fun `when install state is DOWNLOADING, status emits Downloading with percent`() {
        val installState = mock(InstallState::class.java)
        `when`(installState.installStatus()).thenReturn(InstallStatus.DOWNLOADING)
        `when`(installState.bytesDownloaded()).thenReturn(40L)
        `when`(installState.totalBytesToDownload()).thenReturn(100L)

        flexibleUpdateManager.installStateUpdatedListener.onStateUpdate(installState)

        val status = flexibleUpdateManager.updateStatus.value
        assertTrue(status is FlexibleUpdateStatus.Downloading)
        val downloading = status as FlexibleUpdateStatus.Downloading
        assertEquals(40L, downloading.bytesDownloaded)
        assertEquals(100L, downloading.totalBytesToDownload)
        assertEquals(40.0f, downloading.progressPercent, 0.01f)
    }

    @Test
    fun `when install state is DOWNLOADED, status emits Downloaded`() {
        val installState = mock(InstallState::class.java)
        `when`(installState.installStatus()).thenReturn(InstallStatus.DOWNLOADED)

        flexibleUpdateManager.installStateUpdatedListener.onStateUpdate(installState)

        assertEquals(FlexibleUpdateStatus.Downloaded, flexibleUpdateManager.updateStatus.value)
    }

    @Test
    fun `when install state is FAILED, status emits Failed`() {
        val installState = mock(InstallState::class.java)
        `when`(installState.installStatus()).thenReturn(InstallStatus.FAILED)
        `when`(installState.installErrorCode()).thenReturn(InstallErrorCode.ERROR_INSTALL_NOT_ALLOWED)

        flexibleUpdateManager.installStateUpdatedListener.onStateUpdate(installState)

        val status = flexibleUpdateManager.updateStatus.value
        assertTrue(status is FlexibleUpdateStatus.Failed)
        val failed = status as FlexibleUpdateStatus.Failed
        assertEquals(InstallErrorCode.ERROR_INSTALL_NOT_ALLOWED, failed.errorCode)
    }

    @Test
    fun `when install state is CANCELED, status emits Canceled`() {
        val installState = mock(InstallState::class.java)
        `when`(installState.installStatus()).thenReturn(InstallStatus.CANCELED)

        flexibleUpdateManager.installStateUpdatedListener.onStateUpdate(installState)

        assertEquals(FlexibleUpdateStatus.Canceled, flexibleUpdateManager.updateStatus.value)
    }

    @Test
    fun `completeUpdate calls completeUpdate on AppUpdateManager`() {
        `when`(appUpdateManager.completeUpdate()).thenReturn(Tasks.forResult(null))

        flexibleUpdateManager.completeUpdate()

        verify(appUpdateManager).completeUpdate()
    }

    @Test
    fun `checkForUpdate when flexible update is available starts update flow`() {
        val appUpdateInfo = mock(AppUpdateInfo::class.java)
        `when`(appUpdateInfo.updateAvailability()).thenReturn(UpdateAvailability.UPDATE_AVAILABLE)
        `when`(appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)).thenReturn(true)
        `when`(appUpdateInfo.installStatus()).thenReturn(InstallStatus.UNKNOWN)
        `when`(appUpdateInfo.updatePriority()).thenReturn(3)
        `when`(appUpdateInfo.clientVersionStalenessDays()).thenReturn(5)

        `when`(appUpdateManager.appUpdateInfo).thenReturn(Tasks.forResult(appUpdateInfo))

        flexibleUpdateManager.checkForUpdate(mockActivity)

        verify(appUpdateManager).startUpdateFlowForResult(
            eq(appUpdateInfo),
            eq(mockActivity),
            any(AppUpdateOptions::class.java),
            eq(FlexibleUpdateManager.REQUEST_CODE_FLEXIBLE_UPDATE)
        )
    }

    @Test
    fun `checkForUpdate when no update available sets status to NotAvailable`() {
        val appUpdateInfo = mock(AppUpdateInfo::class.java)
        `when`(appUpdateInfo.updateAvailability()).thenReturn(UpdateAvailability.UPDATE_NOT_AVAILABLE)
        `when`(appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)).thenReturn(false)
        `when`(appUpdateInfo.installStatus()).thenReturn(InstallStatus.UNKNOWN)

        `when`(appUpdateManager.appUpdateInfo).thenReturn(Tasks.forResult(appUpdateInfo))

        flexibleUpdateManager.checkForUpdate(mockActivity)

        assertEquals(FlexibleUpdateStatus.NotAvailable, flexibleUpdateManager.updateStatus.value)
        verify(appUpdateManager, never()).startUpdateFlowForResult(
            any(),
            any(Activity::class.java),
            any(),
            any(Int::class.java)
        )
    }

    @Test
    fun `checkForUpdate when update already downloaded sets status to Downloaded without starting flow`() {
        val appUpdateInfo = mock(AppUpdateInfo::class.java)
        `when`(appUpdateInfo.installStatus()).thenReturn(InstallStatus.DOWNLOADED)

        `when`(appUpdateManager.appUpdateInfo).thenReturn(Tasks.forResult(appUpdateInfo))

        flexibleUpdateManager.checkForUpdate(mockActivity)

        assertEquals(FlexibleUpdateStatus.Downloaded, flexibleUpdateManager.updateStatus.value)
        verify(appUpdateManager, never()).startUpdateFlowForResult(
            any(),
            any(Activity::class.java),
            any(),
            any(Int::class.java)
        )
    }

    @Test
    fun `checkForUpdate when appUpdateInfo task fails sets status to Failed`() {
        val error = RuntimeException("Network error")
        `when`(appUpdateManager.appUpdateInfo).thenReturn(Tasks.forException(error))

        flexibleUpdateManager.checkForUpdate(mockActivity)

        val status = flexibleUpdateManager.updateStatus.value
        assertTrue(status is FlexibleUpdateStatus.Failed)
        assertEquals("Network error", (status as FlexibleUpdateStatus.Failed).message)
    }

    @Test
    fun `onResume detects if update finished downloading while app was in background`() {
        val appUpdateInfo = mock(AppUpdateInfo::class.java)
        `when`(appUpdateInfo.installStatus()).thenReturn(InstallStatus.DOWNLOADED)
        `when`(appUpdateManager.appUpdateInfo).thenReturn(Tasks.forResult(appUpdateInfo))

        flexibleUpdateManager.onResume()

        assertEquals(FlexibleUpdateStatus.Downloaded, flexibleUpdateManager.updateStatus.value)
    }
}
