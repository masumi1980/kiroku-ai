package jp.co.kirokuai.app.ui.meeting

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.kirokuai.app.model.MeetingSummary
import jp.co.kirokuai.app.viewmodel.MarkdownExportViewModel
import jp.co.kirokuai.app.viewmodel.MeetingViewModel

@Composable
fun MeetingDetailScreen(
    viewModel: MeetingViewModel,
    markdownExportViewModel: MarkdownExportViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val exportState by markdownExportViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val createDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(MARKDOWN_MIME_TYPE),
        onResult = { uri -> markdownExportViewModel.export(uri?.toString()) },
    )

    LaunchedEffect(exportState.message) {
        exportState.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            markdownExportViewModel.messageShown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Text("会議詳細", style = MaterialTheme.typography.headlineMedium) }
            state.summary?.let { summary -> summaryContent(summary) }
            item {
                Button(
                    onClick = viewModel::generateSummary,
                    enabled = !state.isGenerating,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.isGenerating) {
                        CircularProgressIndicator()
                    } else {
                        Text(if (state.summary == null) "要約を生成" else "要約を再生成")
                    }
                }
            }
            item {
                Button(
                    onClick = { createDocument.launch(markdownExportViewModel.defaultFileName()) },
                    enabled = state.summary != null && !exportState.isExporting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (exportState.isExporting) {
                        CircularProgressIndicator()
                    } else {
                        Text("Markdownをエクスポート")
                    }
                }
            }
            state.errorMessage?.let { message ->
                item { Text(message, color = MaterialTheme.colorScheme.error) }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.summaryContent(summary: MeetingSummary) {
    item { SummarySection("要約", listOf(summary.summary)) }
    item { SummarySection("決定事項", summary.decisions) }
    item { SummarySection("議論", summary.discussion) }
    item { SummarySection("次のアクション", summary.nextActions) }
    item { SummarySection("リスク", summary.risks) }
}

@Composable
private fun SummarySection(title: String, entries: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (entries.isEmpty()) {
            Text("なし", style = MaterialTheme.typography.bodyMedium)
        } else {
            entries.forEach { entry -> Text(entry, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

private const val MARKDOWN_MIME_TYPE = "text/markdown"
