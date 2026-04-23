package com.thdwjdrl.yejeong.beluga.user;

public record UpdatePasswordRequest(
		String currentPassword,
		String newPassword
) {
}
