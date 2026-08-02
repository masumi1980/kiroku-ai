package jp.co.kirokuai.app.export

sealed class MarkdownExportException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NoSummary : MarkdownExportException("No meeting summary exists")
    class FileCreationCancelled : MarkdownExportException("File creation was cancelled")
    class WriteFailure(cause: Throwable) : MarkdownExportException("Failed to write Markdown file", cause)
    class PermissionDenied(cause: Throwable) : MarkdownExportException("Permission to write the file was denied", cause)
}
