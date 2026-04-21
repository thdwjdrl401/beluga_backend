package com.thdwjdrl.yejeong.beluga.common.api;

import java.time.LocalDateTime;

public record ApiErrorResponse(
		String code,
		String message,
		LocalDateTime timestamp
) {
}
