package com.thdwjdrl.yejeong.beluga.common.exception;

import org.springframework.http.HttpStatus;

public abstract class BelugaException extends RuntimeException {

	private final String code;
	private final HttpStatus status;

	protected BelugaException(String code, String message, HttpStatus status) {
		super(message);
		this.code = code;
		this.status = status;
	}

	public String getCode() {
		return code;
	}

	public HttpStatus getStatus() {
		return status;
	}

}
