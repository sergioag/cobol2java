package com.res.java.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel.MapMode;

public class CobolOutputStream {

	private OutputStream out=null;
	private String name=null;
	
	public CobolOutputStream(OutputStream out) {
		this.out=out;
	}

	public CobolOutputStream(String assignName) {
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
		out=null;
	}
	
	private void create(String assignName) throws IOException {
		File file;
		file=new File(assignName);
		//file.createNewFile();
		this.out=new FileOutputStream(file);
	}
	
	public void write(int b) throws IOException {
		out.write(b);
	}

	public void write(byte[] b)  throws IOException {
		out.write(b);
		out.write(13);
		out.write(10);
	}

	public void write(CobolBytes b)  throws IOException {
		write(b.getBytes());
	}
	
	public void write(byte[] b,int offset,int len)  throws IOException {
		out.write(b,offset, len);
	}
	
	public CobolBytes map(long position,int size)  throws IOException {
		return new CobolBytes(((FileOutputStream)out).getChannel().
				map(MapMode.READ_WRITE, position, size).array());
	}
		
}
