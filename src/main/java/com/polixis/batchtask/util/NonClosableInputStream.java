package com.polixis.batchtask.util;

import java.io.FilterInputStream;
import java.io.InputStream;

public class NonClosableInputStream extends FilterInputStream {

	public NonClosableInputStream(InputStream in) {
		super(in);
	}

	@Override
	public void close() {
	}

}
