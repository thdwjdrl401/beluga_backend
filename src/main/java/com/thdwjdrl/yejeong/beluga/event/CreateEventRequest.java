package com.thdwjdrl.yejeong.beluga.event;

import java.time.LocalDateTime;

public record CreateEventRequest(
		String title,
		String description,
		LocalDateTime startAt,
		LocalDateTime endAt,
		int winnerLimit
) {
}
