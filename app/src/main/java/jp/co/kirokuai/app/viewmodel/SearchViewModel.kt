package jp.co.kirokuai.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.kirokuai.app.domain.SearchMeetingsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchMeetings: SearchMeetingsUseCase,
) : ViewModel() {
    private val mutableKeyword = MutableStateFlow("")
    val keyword: StateFlow<String> = mutableKeyword.asStateFlow()

    private val mutableUiState = MutableStateFlow<SearchUiState>(SearchUiState.Empty)
    val uiState: StateFlow<SearchUiState> = mutableUiState.asStateFlow()

    private var searchJob: Job? = null

    fun onKeywordChanged(keyword: String) {
        mutableKeyword.value = keyword
        searchJob?.cancel()
        if (keyword.trim().isEmpty()) {
            mutableUiState.value = SearchUiState.Empty
            return
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            mutableUiState.value = SearchUiState.Loading
            mutableUiState.value = try {
                SearchUiState.Results(searchMeetings(keyword))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                SearchUiState.Error(exception.message ?: SEARCH_ERROR_MESSAGE)
            }
        }
    }

    fun clearKeyword() {
        onKeywordChanged("")
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
        const val SEARCH_ERROR_MESSAGE = "検索できませんでした"
    }
}
