package com.res.java.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel.MapMode;

public class CobolInputStream  {
	
	private InputStream in=null;;
	private String name=null;
	
	public CobolInputStream(InputStream in) {
		this.in=in;
	}

	public CobolInputStream(String assignName) {
		String s;
		if((s=RunConfig.getInstance().get(assignName))!=null) {
				name=s;
		} else {
			name=assignName;
		}
	}
	public void open() throws IOException {
		if(in!=null)
			throw new IOException("Attempting to open a file which is already open.");
		create(name);
	}
	
	public void close() throws IOException {
		in.close();
		in=null;
	}
	
	public int read() throws IOException {
		return in.read();
	}

	public int read(byte[] b)  throws IOException {
		return in.read(b);
	}

	public int read(CobolBytes b)  throws IOException {
		return in.read(((CobolBytes)b).get());
	}

	
	public int read(byte[] b,int offset,int len)  throws IOException {
		return in.read(b,offset, len);
	}
	
	public CobolBytes map(long position,int size)  throws IOException {
		return new CobolBytes(((FileInputStream)in).getChannel().
				map(MapMode.READ_WRITE, position, size).array());
	}
	
	private void create(String assignName) throws IOException {
		File file;
		file=new File(assignName);
		//file.createNewFile();
		this.in=new FileInputStream(file);
	}

}
