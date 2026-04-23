package com.thdwjdrl.yejeong.beluga.user;

import java.time.LocalDateTime;

public record UserProfileResponse(
		Long userId,
		String email,
		String nickname,
		UserRole role,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
}
