package com.thdwjdrl.yejeong.beluga.participation;

import com.thdwjdrl.yejeong.beluga.event.EventSummaryResponse;

import java.time.LocalDateTime;
import java.util.List;

public record ParticipationsListResponse(
		Long participationId,
		Long eventId,
		Long userId,
		String email,
		String nickname,
		LocalDateTime participatedAt,
		long requestSequence,
		ParticipationResultStatus resultStatus
) {
}
