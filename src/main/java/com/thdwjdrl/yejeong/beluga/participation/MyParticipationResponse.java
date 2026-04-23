package com.thdwjdrl.yejeong.beluga.participation;

import java.time.LocalDateTime;

public record MyParticipationResponse(
		Long eventId,
		String eventName,
		String productName,
		ParticipationResultStatus resultStatus,
		LocalDateTime participatedAt,
		LocalDateTime startAt,
		LocalDateTime endAt,
		boolean gifticonAvailable
) {
}
