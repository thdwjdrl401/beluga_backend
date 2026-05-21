package com.thdwjdrl.yejeong.beluga.event;

import java.time.LocalDateTime;

public record EventResultResponse(
		Long eventId,
		String eventName,
		String productName,
		int winnerCount,
		int participantCount,
		LocalDateTime startAt,
		LocalDateTime endAt,
		EventStatus status
) {
}
