package ma.jmbb;

import java.io.*;

import ma.tools2.util.NotImplementedException;

class ArrayCachedInputStream extends FilterInputStream {

	private int[] cache;
	private int pos;
	private boolean eof;

	ArrayCachedInputStream(InputStream in, int cacheSize)
				throws IOException, MBBFailureException {
		// Buffered reader implies a great performace gain.
		super(new BufferedInputStream(in));

		cache = new int[cacheSize + 1]; // +1 additinal byte for EOF.
		fillCacheInitially();
		eof = false;
		pos = 0;
	}

	private void fillCacheInitially()
				throws IOException, MBBFailureException {
		byte[] cache2 = new byte[cache.length];
		if(in.read(cache2, 0, cache.length) != cache.length) {
			throw new MBBFailureException(
				"Can not fill initial cache. (JMBB: File " +
				"is too small to be a valid input)"
			);
		}
		for(int i = 0; i < cache2.length; i++) {
			// Make unsigned.
			cache[i] = cache2[i] & 0xff;
		}
	}

	@Override
	public int read(byte[] buf, int off, int len) throws IOException {
		if(eof) {
			return -1;
		}
		int i;
		for(i = 0; i < len && !eof; i++) {
			buf[off + i] = (byte)read();
		}
		return i;
	}

	@Override
	public int read() throws IOException {
		int ret = cache[pos];
		if(ret == -1) {
			throw new IOException("Fatal error. Read EOF where " +
							"it was not expected.");
		}

		cacheNext();

		return ret;
	}

	private void cacheNext() throws IOException {
		cache[pos] = in.read();

		if(cache[pos] == -1) {
			eof = true;
		}

		nextPos();
	}

	/**
	 * Implements suitable wrap-around.
	 */
	private void nextPos() {
		pos++;
		if(pos == cache.length) {
			pos = 0;
		}
	}

	boolean isEndOfData() {
		return eof;
	}

	/**
	 * WARNING: May only be invoked once per ArrayCachedInputStream object.
	 */
	byte[] getFooter() throws MBBFailureException {
		if(!eof) {
			throw new MBBFailureException(
				"Can not get footer while stream is not " +
				"EOF. (Implementation error?)"
			);
		}

		byte[] ret = new byte[cache.length - 1];
		for(int i = 0; cache[pos] != -1; i++) {
			ret[i] = (byte)cache[pos];
			nextPos();
		}
		return ret;
	}

	@Override
	public long skip(long n) throws IOException {
		long i;
		for(i = 0; i < n && !eof; i++) {
			read();
		}
		return i;
	}

	@Override
	public boolean markSupported() {
		return false;
	}

}
