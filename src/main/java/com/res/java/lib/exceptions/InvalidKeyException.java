package com.res.java.lib.exceptions;

import java.io.IOException;

public class InvalidKeyException extends IOException {

	private static final long serialVersionUID = 1L;

	private String message = "Invalid key in com.res.java.lib.CobolFile or com.res.java.lib.CobolIndexedFile.";

	public String getMessage() {
		return message;
	}
	
}
