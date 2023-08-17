package com.cdac.esign.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_IMPLEMENTED)
public class EsignException extends RuntimeException {
	public EsignException(String msg) {
		super(msg);
	}

}
