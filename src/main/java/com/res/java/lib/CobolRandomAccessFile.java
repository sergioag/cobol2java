package com.res.java.lib;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel.MapMode;

public class CobolRandomAccessFile {

	private RandomAccessFile rand=null;
	
	private String name=null;
	private String mode=null;
	
	protected CobolRandomAccessFile(RandomAccessFile rand) {
		this.rand=rand;
	}
	
	public CobolRandomAccessFile(String assignName) {
		if((name=RunConfig.getInstance().get(assignName))!=null) {
		} else {
			name=assignName;
		}
		mode="r";
	}

	public CobolRandomAccessFile(String assignName,String mode) {
		if((name=RunConfig.getInstance().get(assignName))!=null) {
		} else {
			name=assignName;
		}
		this.mode=mode;
	}

	public void open() throws IOException {
		if(rand!=null&&rand.getFD().valid())
			throw new IOException("Attempting to open a file which is already open.");
		create(name,mode);
	}
	
	public void close() throws IOException {
		rand.close();
	}
	
	private void create(String assignName,String mode) throws IOException {
		File file;
		file=new File(assignName);
		file.createNewFile();
		this.rand=new RandomAccessFile(file,mode);
	}
	
	
	public int read() throws IOException {
		return rand.read();
	}

	public int read(byte[] b)  throws IOException {
		return rand.read(b);
	}

	public int read(byte[] b,int offset,int len)  throws IOException {
		return rand.read(b,offset, len);
	}
	
	public void write(int b) throws IOException {
		rand.write(b);
	}

	public void write(byte[] b)  throws IOException {
		rand.write(b);
	}

	public void write(byte[] b,int offset,int len)  throws IOException {
		rand.write(b,offset, len);
	}
	
	public CobolBytes map(long position,long size)  throws IOException {
		return new CobolBytes(rand.getChannel().
				map(MapMode.READ_WRITE, position, size).array());
	}
	
}
