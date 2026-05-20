package com.thdwjdrl.yejeong.beluga.event;

import java.time.LocalDateTime;

public record EventSummaryResponse(
		Long eventId,
		String eventName,
		String productName,
		Long representativeAttachId,
		int winnerLimit,
		LocalDateTime startAt,
		LocalDateTime endAt,
		EventStatus status
) {
}
