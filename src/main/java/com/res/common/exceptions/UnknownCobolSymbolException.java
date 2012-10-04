package com.res.common.exceptions;

@SuppressWarnings("serial")
public class UnknownCobolSymbolException extends Exception {

	private String message=null;
	
	public UnknownCobolSymbolException(String msg) {
		this.message=msg;
	}
	
	public String getMessage() {
		return this.message;
	}
	
}
