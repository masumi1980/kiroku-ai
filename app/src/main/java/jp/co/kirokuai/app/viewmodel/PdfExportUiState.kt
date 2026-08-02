package jp.co.kirokuai.app.viewmodel

data class PdfExportUiState(
    val isExporting: Boolean = false,
    val message: String? = null,
)
