package org.libvirt.jna;

import com.sun.jna.Union;
import com.sun.jna.ptr.ByteByReference;

public class VirUnion extends Union{
	public int i;
	public long ui;
	public long l;
	public long ul;
	public double d;
	public char b;
	public ByteByReference s = new ByteByReference();
}
