package com.cdac.esign.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_IMPLEMENTED)
public class InvalidDocSignatureException extends RuntimeException {

	public InvalidDocSignatureException(String msg) {
		super(msg);
	}
}
