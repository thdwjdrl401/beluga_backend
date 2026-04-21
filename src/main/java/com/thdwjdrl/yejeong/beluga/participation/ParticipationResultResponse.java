package com.thdwjdrl.yejeong.beluga.participation;

import java.time.LocalDateTime;

public record ParticipationResultResponse(
		Long eventId,
		Long userId,
		boolean participated,
		ParticipationResultStatus resultStatus,
		Long requestSequence,
		LocalDateTime participatedAt
) {
}
