package com.thdwjdrl.yejeong.beluga.common.exception;

import java.time.LocalDateTime;

import com.thdwjdrl.yejeong.beluga.common.api.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BelugaException.class)
	public ResponseEntity<ApiErrorResponse> handleBelugaException(BelugaException exception) {
		return ResponseEntity.status(exception.getStatus())
				.body(new ApiErrorResponse(
						exception.getCode(),
						exception.getMessage(),
						LocalDateTime.now()
				));
	}

	@ExceptionHandler({
			HttpMessageNotReadableException.class,
			MethodArgumentTypeMismatchException.class,
			MissingRequestHeaderException.class
	})
	public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception) {
		return ResponseEntity.badRequest()
				.body(new ApiErrorResponse(
						"INVALID_REQUEST",
						"요청 형식이 올바르지 않습니다.",
						LocalDateTime.now()
					));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNoResourceFound(
			NoResourceFoundException exception,
			HttpServletRequest request
	) {
		log.debug("resource_not_found path={}", request.getRequestURI());
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiErrorResponse(
						"NOT_FOUND",
						"요청한 리소스를 찾을 수 없습니다.",
						LocalDateTime.now()
				));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnhandledException(
			Exception exception,
			HttpServletRequest request
	) {
		log.error("unhandled_exception path={}", request.getRequestURI(), exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ApiErrorResponse(
						"SYSTEM_ERROR",
						"시스템 처리 중 오류가 발생했습니다.",
						LocalDateTime.now()
				));
	}

}
