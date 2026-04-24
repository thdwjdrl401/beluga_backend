package com.thdwjdrl.yejeong.beluga.event;

import java.time.LocalDateTime;

public record EventCreateRequest(
	String eventName,
	String productName,
    LocalDateTime startAt,
	LocalDateTime endAt,
	int winnerLimit
) {
}
