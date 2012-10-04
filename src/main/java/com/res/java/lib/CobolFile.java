package com.res.java.lib;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import com.res.java.lib.exceptions.InvalidKeyException;

public class CobolFile {

	private InputStream in=null;;
	private OutputStream out=null;
	private RandomAccessFile io=null;
	private String assignName=null;

	private int mode=-1;
	
	private boolean CRLF = true;
	
	private int minRecLen=0;
	private int maxRecLen=0;
	private int accessMode=0;
	
	private int linageCounter=0;
	private int fileStatus=0;

	private boolean isLastOpRead=false;
	
	public static final int OPENED_INPUT = 0;
	public static final int OPENED_OUTPUT = 1;
	public static final int OPENED_IO = 2;
	
	public CobolFile(String assignName, int accessMode) {
		if((this.assignName=RunConfig.getInstance().get(assignName))==null) {
			this.assignName=assignName;
		}
		this.accessMode=accessMode;
	}
        
	public CobolFile(String assignName,boolean crlf) {
		if((this.assignName=RunConfig.getInstance().get(assignName))==null) {
			this.assignName=assignName;
		}
		CRLF=crlf;
	}
	private void clearStatus() {
		fileStatus=0;isLastOpRead=false;
	}
	
	public CobolFile(String assignName,int accessMode,boolean crlf) {
		if((this.assignName=RunConfig.getInstance().get(assignName))==null) {
			this.assignName=assignName;
		}
		CRLF=crlf;
		this.accessMode=accessMode;
	}


	public CobolFile(String assignName,int recLen,int accessMode) {
		if((this.assignName=RunConfig.getInstance().get(assignName))==null) {
			this.assignName=assignName;
		}
		this.setMinRecLen(recLen);this.setMaxRecLen(recLen);
		this.accessMode=accessMode;
	}
	
	public CobolFile(String assignName,int minRecLen,int maxRecLen,int accessMode) {
		if((this.assignName=RunConfig.getInstance().get(assignName))==null) {
			this.assignName=assignName;
		}
		this.setMinRecLen(minRecLen);this.setMaxRecLen(maxRecLen);
		this.accessMode=accessMode;
	}
	
	public void openIO() throws IOException {
		clearStatus();
		if(in!=null||out!=null||io!=null)
			setOpeningOpened();
		io=new RandomAccessFile(this.assignName,"rw");
		//io.seek(0);
		mode=OPENED_IO;
	}

	private void delete(long startPosition,long endPosition) throws IOException {
		if(io==null)
			setRewriteUnOpened();
		long recLen=endPosition-startPosition;
		ByteBuffer copy = ByteBuffer.allocate((int)recLen);
		
		FileChannel fc = io.getChannel();
		
		try {
			
			int readBytes=0;
			
			if(endPosition<=startPosition||endPosition>io.length()) throw new InvalidKeyException();
			
		    io.setLength(io.length()-recLen);
		    
		    do {
		    	
		    	io.seek(endPosition);
		    	
		    	if((readBytes=fc.read(copy))<=0) break;
		    	
			    fc.position(startPosition);
			    
			    copy.limit(readBytes);copy.position(0);
			    
			    fc.write(copy);
			    
			    if(io.getFilePointer()>=io.length()) break;
			    
			    endPosition+=recLen;startPosition+=recLen;
			    
			    copy.clear();
			    
		    } while(true);
		    
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
	
		}
	}
	
	public void openInput() throws IOException {
		clearStatus();
		if(in!=null||out!=null||io!=null)
			setOpeningOpened();
		File file;
		file=new File(this.assignName);
		if(!file.exists()||!file.canRead())
			setInputNonExistantFile();
		if(accessMode==Constants.SEQUENTIAL_ACCESS)
			this.in=new FileInputStream(file);
		else
			this.io=new RandomAccessFile(file,"r");
		mode=OPENED_INPUT;
	}

	public void openOutput() throws IOException {
		clearStatus();
		if(in!=null||out!=null||io!=null)
			setOpeningOpened();
		File file;
		file=new File(this.assignName);
		this.out=new FileOutputStream(file);
		mode=OPENED_OUTPUT;
	}

	public void openExtend() throws IOException {
		if(in!=null||out!=null||io!=null)
			setOpeningOpened();
		clearStatus();
		File file;
		file=new File(this.assignName);
		this.out=new FileOutputStream(file,true);
	}
	
	
	public void close() throws IOException {
		if(in==null&&out==null&&io==null)
			setClosingUnOpened();
		clearStatus();
		closeInput();
		closeOutput();
		closeIO();
	}

	private void closeIO() throws IOException {
		if(io!=null) {
			io.close();
			io=null;
		}
	}
	
	private void closeInput() throws IOException {
		if(in!=null) {
			in.close();
			in=null;
		}
	}

	private void closeOutput() throws IOException {
		if(out!=null) {
			out.close();
			out=null;
		}
	}
	
	public void write(int b) throws IOException {
		clearStatus();
		if(mode==OPENED_IO) {
			if(io==null)
				setReadUnopened();
			io.write(b);
		}
		else {
			if(out==null)
				setReadUnopened();
			out.write(b);
		}
	}

	public void write(byte[] b)  throws IOException {
		write(b,0,b.length);
	}

	public void write(CobolBytes b)  throws IOException {
		write(b.getBytes());
	}
	
	public void write(byte[] b,int offset,int len)  throws IOException {
		clearStatus();
		if(mode==OPENED_IO) {
			if(io!=null)
				setWriteIO();
		}
		else {
			if(out==null)
				setWriteUnopened();
			out.write(b,offset, len);
			if(CRLF) {
				out.write(13);
				out.write(10);
			}
		}
	}
	
	public void rewrite(byte[] b)  throws IOException {
		rewrite(b,0,b.length);
	}

	public void rewrite(CobolBytes b)  throws IOException {
		rewrite(b.getBytes());
	}
	
	public void rewrite(byte[] b,int offset,int len)  throws IOException {
		clearStatus();
		if(!isLastOpRead)
			setRewriteWithoutRead();
		if(len>maxRecLen||len<minRecLen)
			setRewriteRecordLengthMismatch();
		if(mode==OPENED_IO) {
			if(io==null)
				setWriteUnopened();
			if(CRLF)
				io.seek(io.getFilePointer()-(len+2));
			else
				io.seek(io.getFilePointer()-len);
			io.write(b,offset, len);
			if(CRLF)
				io.seek(io.getFilePointer()+2);
			return ;
		}
		throw new IOException("Rewrite without opening file " +assignName + 
				" in I-O mode.");
	}
	

	public CobolBytes mapOutput(long position,int size)  throws IOException {
		return new CobolBytes(((FileOutputStream)out).getChannel().
				map(MapMode.READ_WRITE, position, size).array());
	}
	
	private boolean isEOF=false;
		
	public int read() throws IOException,EOFException {
		clearStatus();
		int ret=0;
		if(accessMode!=Constants.SEQUENTIAL_ACCESS) {
			if(io==null) setReadUnopened();
			if(io.length()-io.getFilePointer()<=0) {
				setEOF();
			}  
			isEOF=false;
			ret=io.read();
		}
		else {
			if(in==null) setReadUnopened();
			if(in.available()<=0) {
				setEOF();
			} 
			isEOF=false;
			ret = in.read();
		}
		return ret;
	}

	public int read(byte[] b)  throws IOException,EOFException {
		return read(b,0,b.length,-1);
	}
	
	public int read(byte[] b,long relativeKey)  throws IOException,EOFException {
		return read(b,0,b.length,relativeKey);
	}
	
	public int read(CobolBytes b)  throws IOException,EOFException {
		return read(b.get(),0,b.size(),-1);
	}

	public int read(CobolBytes b,long relativeKey)  throws IOException,EOFException {
		return read(b.get(),0,b.size(),relativeKey);
	}

	public int read(byte[] b,int offset,int len,long relativeKey)  throws IOException,EOFException {
		clearStatus();
		int ret=0;
		if(accessMode!=Constants.SEQUENTIAL_ACCESS) {
			if(io==null)
				setReadUnopened();
			if(relativeKey>=0) {
				seekIO(relativeKey);
			} else
			if(io.length()-io.getFilePointer()<=0) {
				setEOF();
			} 
			isEOF=false;
			ret = io.read(b,offset,len);
		}
		else {
			if(in==null)
				setReadUnopened();
			if(in.available()<=0) {
				setEOF();
			} 
			isEOF=false;
			ret = in.read(b,offset, len);
		}
		if(CRLF) {
			read();read();linageCounter++;
		}
		return ret;
	}
	public int read(byte[] b,int offset,int len)  throws IOException,EOFException {
		return read(b,offset,len,-1);
	}
	
	public void delete(long relativeKey) throws IOException {
		long saveFP = seekIO(relativeKey);
		delete(io.getFilePointer(),io.getFilePointer()+maxRecLen+2);
		io.seek(saveFP);
	}

	private long seekIO(long relativeKey) throws InvalidKeyException {
		try {
			long saveFP = (--relativeKey)*(maxRecLen+2);
			io.seek(saveFP);
			return saveFP;
		} catch(IOException ie){
			throw new InvalidKeyException();
		}
	}
	
	public CobolBytes mapInput(long position,int size)  throws IOException {
		return new CobolBytes(((FileInputStream)in).getChannel().
				map(MapMode.READ_WRITE, position, size).array());
	}

	public int linageCounter() {
		return linageCounter;
	}

	public void setMinRecLen(int minRecLen) {
		this.minRecLen = minRecLen;
	}

	public int getMinRecLen() {
		return minRecLen;
	}

	public void setMaxRecLen(int maxRecLen) {
		this.maxRecLen = maxRecLen;
	}

	public int getMaxRecLen() {
		return maxRecLen;
	}
	
	public String getAssignName() {
		return assignName;
	}

	public void setAssignName(String assignName) {
		this.assignName = assignName;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getMode() {
		return mode;
	}

	public void setEOF() throws IOException {
		fileStatus=10;
		if(isEOF)
			throw new IOException("Attempting to read beyond EOF");
		else {
			isEOF=true;
			throw new EOFException();
		}
	}

	public String getFileStatus() {
		if(fileStatus==0) return "00";
		String ret = String.valueOf(fileStatus);
		if(ret.length()==1)return '0'+ret;
		else if(ret.length()==2) return ret;
		else if(ret.length()>2)return ret.substring(0,1);
		else return "99";
	}

	private byte[] spaces=null;
	
	public void advanceLines(int lines) throws IOException {
		if(spaces==null) {
			StringBuffer spBuf=new StringBuffer(maxRecLen);
			for(int i=0;i<maxRecLen;++i)
				spBuf.append(' ');
			spaces=spBuf.toString().getBytes();
		} 
		for (;lines>0;--lines)
			write(spaces);
	}

	public void advancePage() throws IOException {
		if(spaces==null) {
			StringBuffer spBuf=new StringBuffer(maxRecLen);
			for(int i=0;i<maxRecLen;++i)
				spBuf.append(' ');
			spaces=spBuf.toString().getBytes();
		} 
		spaces[0]='\f';
		write(spaces);
		spaces[0]=' ';
	}
	
	//File Status Error/Exception
	
	private void setInputNonExistantFile() throws IOException {
		fileStatus=35;throw new IOException("Open Input on a non-existant/unreadable file.");
		
	}

	private void setOpeningOpened() throws IOException {
		setStatus(41);
		throw new IOException("Attempting to open a file which is already open.");
	}
	
	private void setRewriteUnOpened() throws IOException {
		setStatus(99);//TODO
		throw new IOException("Attempting to rewrite to a file which is not open.");
	}
	
	private void setClosingUnOpened() throws IOException {
		setStatus(42);
		throw new IOException("Attempting to close a file which is not open.");
	}
	
	
	private void setStatus(int stat) {
		fileStatus=stat;
	}

	private Exception setRewriteRecordLengthMismatch() throws IOException {
		fileStatus=44;throw new IOException("Rewrite with record length not in range " +String.valueOf(minRecLen)+" and "+
				String.valueOf(maxRecLen)+".");
	}

	private void setRewriteWithoutRead() throws IOException {
		fileStatus=43;throw new IOException("Rewrite without prior read not allowed on files opened in I-O mode.");
		
	}

	private void setReadUnopened() throws IOException {
		fileStatus=47;throw new IOException("Read operation on closed file.");
		
	}
	private void setWriteUnopened() throws IOException {
		fileStatus=48;throw new IOException("Write operation on closed file.");
		
	}
	private void setWriteIO() throws IOException {
		fileStatus=48;throw new IOException("Write operation on a file opened in I-O mode.");
		
	}
	
}
