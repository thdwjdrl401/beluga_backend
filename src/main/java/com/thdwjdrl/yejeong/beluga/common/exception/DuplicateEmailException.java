package com.thdwjdrl.yejeong.beluga.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends BelugaException {

	public DuplicateEmailException(String message) {
		super("DUPLICATE_EMAIL", message, HttpStatus.CONFLICT);
	}

}
