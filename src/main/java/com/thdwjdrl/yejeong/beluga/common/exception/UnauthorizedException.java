package com.thdwjdrl.yejeong.beluga.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BelugaException {

	public UnauthorizedException(String message) {
		super("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
	}

}
