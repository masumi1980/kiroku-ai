package jp.co.kirokuai.app.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.kirokuai.app.model.Meeting
import jp.co.kirokuai.app.model.MeetingStatus
import jp.co.kirokuai.app.util.formatDuration
import jp.co.kirokuai.app.viewmodel.HistoryViewModel
import jp.co.kirokuai.app.viewmodel.SearchUiState
import jp.co.kirokuai.app.viewmodel.SearchViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    searchViewModel: SearchViewModel,
    onMeetingClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val meetings by viewModel.meetings.collectAsStateWithLifecycle()
    val keyword by searchViewModel.keyword.collectAsStateWithLifecycle()
    val searchState by searchViewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "会議履歴",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
        )
        OutlinedTextField(
            value = keyword,
            onValueChange = searchViewModel::onKeywordChanged,
            label = { Text("会議を検索") },
            singleLine = true,
            trailingIcon = {
                if (keyword.isNotEmpty()) {
                    TextButton(onClick = searchViewModel::clearKeyword) { Text("クリア") }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
        when (val state = searchState) {
            SearchUiState.Empty -> if (meetings.isEmpty()) {
                EmptyMessage("会議はありません", Modifier.weight(1f))
            } else {
                MeetingList(meetings, onMeetingClick, Modifier.weight(1f))
            }
            SearchUiState.Loading -> LoadingSearch(Modifier.weight(1f))
            is SearchUiState.Results -> if (state.meetings.isEmpty()) {
                EmptyMessage("検索結果がありません", Modifier.weight(1f))
            } else {
                MeetingList(state.meetings, onMeetingClick, Modifier.weight(1f))
            }
            is SearchUiState.Error -> EmptyMessage(state.message, Modifier.weight(1f))
        }
    }
}

@Composable
private fun EmptyMessage(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(message)
    }
}

@Composable
private fun LoadingSearch(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MeetingList(
    meetings: List<Meeting>,
    onMeetingClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = meetings,
            key = Meeting::id,
        ) { meeting ->
            MeetingCard(meeting = meeting, onClick = { onMeetingClick(meeting.id) })
        }
    }
}

@Composable
private fun MeetingCard(
    meeting: Meeting,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = meeting.title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "作成日時: ${formatCreatedAt(meeting.createdAt)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "録音時間: ${formatDuration(meeting.duration)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "状態: ${meeting.status.displayName()}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

internal fun formatCreatedAt(createdAt: Long): String =
    DATE_TIME_FORMATTER.format(
        Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()),
    )

private fun MeetingStatus.displayName(): String = when (this) {
    MeetingStatus.RECORDING -> "録音中"
    MeetingStatus.TRANSCRIBING -> "文字起こし中"
    MeetingStatus.COMPLETED -> "完了"
    MeetingStatus.ERROR -> "エラー"
}

private val DATE_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.JAPAN)
