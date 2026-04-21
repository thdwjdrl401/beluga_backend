package com.thdwjdrl.yejeong.beluga.event;

import java.time.LocalDateTime;

public record EventResponse(
		Long eventId,
		String title,
		String description,
		LocalDateTime startAt,
		LocalDateTime endAt,
		int winnerLimit,
		int currentWinnerCount,
		EventStatus status,
		LocalDateTime createdAt
) {
}
