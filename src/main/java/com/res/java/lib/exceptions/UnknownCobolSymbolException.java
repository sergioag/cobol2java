package com.res.java.lib.exceptions;

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
