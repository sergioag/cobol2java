package com.res.java.lib;

import java.io.IOException;

public interface FileAdapter {

	public boolean read()  throws IOException;
	public boolean write()  throws IOException;
	public boolean rewrite()  throws IOException;
	public boolean start() throws IOException; 
	public boolean startEq(Object key) throws IOException; 
	public boolean startGt(Object key) throws IOException; 
	public boolean startGe(Object key) throws IOException; 
	public boolean delete()  throws IOException;
	
}
