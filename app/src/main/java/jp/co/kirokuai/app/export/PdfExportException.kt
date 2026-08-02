package jp.co.kirokuai.app.export

sealed class PdfExportException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NoSummary : PdfExportException("No meeting summary exists")
    class FileCreationCancelled : PdfExportException("File creation was cancelled")
    class WriteFailure(cause: Throwable) : PdfExportException("Failed to write PDF file", cause)
    class PermissionDenied(cause: Throwable) : PdfExportException("Permission to write the file was denied", cause)
}
