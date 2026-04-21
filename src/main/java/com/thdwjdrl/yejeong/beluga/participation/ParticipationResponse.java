package com.thdwjdrl.yejeong.beluga.participation;

import java.time.LocalDateTime;

public record ParticipationResponse(
		Long eventId,
		Long userId,
		ParticipationResultStatus resultStatus,
		ParticipationResultStatus existingResultStatus,
		Long requestSequence,
		LocalDateTime participatedAt,
		String requestId,
		String message
) {
}
