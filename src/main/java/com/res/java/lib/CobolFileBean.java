package com.res.java.lib;

import java.io.IOException;

public interface CobolFileBean {

	public boolean initialize(CobolIndexedFile file) throws IOException;
	public void setSQLProperties(CobolIndexedFile file) throws IOException;
	public void getSQLResults(CobolIndexedFile file) throws IOException;
	public Object primaryKey();
	public int numberOfColumns() ;
	
}
