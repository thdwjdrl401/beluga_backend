package com.thdwjdrl.yejeong.beluga.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends BelugaException {

	public InvalidRequestException(String message) {
		super("INVALID_REQUEST", message, HttpStatus.BAD_REQUEST);
	}

}
