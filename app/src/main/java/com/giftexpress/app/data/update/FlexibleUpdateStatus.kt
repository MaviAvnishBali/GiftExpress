package com.giftexpress.app.data.update

/**
 * Represents the current status of a Google Play Flexible In-App Update.
 */
sealed interface FlexibleUpdateStatus {
    /** Initial state before update check begins. */
    object Idle : FlexibleUpdateStatus

    /** Checking Google Play for available updates. */
    object Checking : FlexibleUpdateStatus

    /** A flexible update is available on Google Play. */
    data class Available(
        val stalenessDays: Int? = null,
        val updatePriority: Int = 0
    ) : FlexibleUpdateStatus

    /** No update is available or flexible update is not allowed. */
    object NotAvailable : FlexibleUpdateStatus

    /** Flexible update is downloading in the background. */
    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytesToDownload: Long,
        val progressPercent: Float
    ) : FlexibleUpdateStatus

    /**
     * The update has finished downloading and is ready to be installed.
     * The app should prompt the user to restart and call completeUpdate().
     */
    object Downloaded : FlexibleUpdateStatus

    /** Flexible update failed with an error code. */
    data class Failed(
        val errorCode: Int,
        val message: String? = null
    ) : FlexibleUpdateStatus

    /** The user canceled or dismissed the update prompt. */
    object Canceled : FlexibleUpdateStatus
}
