package com.cdac.esign.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_IMPLEMENTED)
public class SignatureEmbeddingException extends RuntimeException {
	public SignatureEmbeddingException(String msg) {
		super(msg);
	}
}
