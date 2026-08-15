package jp.co.kirokuai.app.domain

import jp.co.kirokuai.app.model.Meeting

class SearchMeetingsUseCase(
    private val meetingRepository: MeetingRepository,
) {
    suspend operator fun invoke(keyword: String): List<Meeting> {
        val trimmedKeyword = keyword.trim()
        return if (trimmedKeyword.isEmpty()) {
            emptyList()
        } else {
            meetingRepository.search(trimmedKeyword)
        }
    }
}
