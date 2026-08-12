package downlaod

sealed interface DownloadResult {

    object Success : DownloadResult

    data class Failure(val message: String) : DownloadResult

}