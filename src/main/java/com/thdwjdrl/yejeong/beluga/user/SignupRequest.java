package com.thdwjdrl.yejeong.beluga.user;

public record SignupRequest(
		String email,
		String password,
		String nickname
) {
}
