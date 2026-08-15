package jp.co.kirokuai.app.viewmodel

import jp.co.kirokuai.app.model.Meeting

sealed interface SearchUiState {
    data object Empty : SearchUiState
    data object Loading : SearchUiState
    data class Results(val meetings: List<Meeting>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}
