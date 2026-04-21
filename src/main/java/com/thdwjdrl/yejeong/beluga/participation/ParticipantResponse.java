package com.thdwjdrl.yejeong.beluga.participation;

import java.time.LocalDateTime;

public record ParticipantResponse(
		Long userId,
		String userEmail,
		ParticipationResultStatus resultStatus,
		long requestSequence,
		LocalDateTime participatedAt
) {
}
