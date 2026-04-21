package com.thdwjdrl.yejeong.beluga.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BelugaException {

	public ResourceNotFoundException(String message) {
		super("NOT_FOUND", message, HttpStatus.NOT_FOUND);
	}

}
