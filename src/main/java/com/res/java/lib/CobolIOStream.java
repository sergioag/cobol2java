package com.res.java.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel.MapMode;

@Deprecated
public class CobolIOStream {
	
	@Deprecated
	private OutputStream out;
	@Deprecated
	private InputStream in;
	
	private String name=null;
	
	public CobolIOStream(String assignName) throws IOException {
		String s;
		if((s=RunConfig.getInstance().get(assignName))!=null) {
				name=s;
		} else {
			name=assignName;
		}
	}
	public void open() throws IOException {
		if(out!=null)
			throw new IOException("Attempting to open a file which is already open.");
		create(name);
	}
	
	public void close() throws IOException {
		out.close();
	}
	
	public int read() throws IOException {
		return in.read();
	}

	public int read(byte[] b)  throws IOException {
		return in.read(b);
	}

	public int read(byte[] b,int offset,int len)  throws IOException {
		return in.read(b,offset, len);
	}
	
	public void write(int b) throws IOException {
		out.write(b);
	}

	public void write(byte[] b)  throws IOException {
		out.write(b);
	}

	public void write(byte[] b,int offset,int len)  throws IOException {
		out.write(b,offset, len);
	}
	
	public CobolBytes map(long position,int size)  throws IOException {
		return new CobolBytes(((FileOutputStream)out).getChannel().
				map(MapMode.READ_WRITE, position, size).array());
	}
	
	private void create(String assignName) throws IOException {
		File file;
		file=new File(assignName);
		file.createNewFile();
		this.in=new FileInputStream(file);
		this.out=new FileOutputStream(file);
	}
	

}
