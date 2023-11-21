package sk.upjs.kopr.file_copy;

import java.io.Serializable;

public class FileRequest implements Serializable{
	private static final long serialVersionUID = -9175100941839168076L;
	public final long offset;
	public final long length;
	public final String name;
	
	public FileRequest(long offset, long length, String name) {
		this.offset = offset;
		this.length = length;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "FileRequest [name="+ name +", offset=" + offset + ", length=" + length + "]";
	}
	
	public long getOffset() {
		return offset;
	}
	public long getLength() {
		return length;
	}
	public String getName() {
		return name;
	}
}
