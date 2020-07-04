package spiderweb.utils;

/**
 * CRC64.
 */
public class CRC64 {
	private static final int LOOKUPTABLE_SIZE = 256;
	private static final long POLY64REV = 0xC96C5795D7870F42L;
	private static final long LOOKUPTABLE[] = new long[LOOKUPTABLE_SIZE];
	static {
		int b, i;
		long r;
		final int bLen = LOOKUPTABLE_SIZE;
		final int iLen = Long.BYTES;
		for (b = 0; b < bLen; ++b) {
			r = b;
			for (i = 0; i < iLen; ++i) {
				r = ((r & 1) == 1) ? (r >>> 1) ^ POLY64REV : r >>> 1;
			}
			LOOKUPTABLE[b] = r;
		}
	}
	
	private long crc = -1;
	
	public void update(final int b) {
		crc = LOOKUPTABLE[((b & 0xFF) ^ (int) crc) & 0xFF] ^ (crc >>> 8);
	}
	
	public void update(final byte[] buf) {
		update(buf, 0, buf.length);
	}

	public void update(final byte[] buf, final int off, final int len) {
		final int end = off + len;
		long c = crc;
		int o = off;
		while (o < end) {
			c = LOOKUPTABLE[(buf[o++] ^ (int)c) & 0xFF] ^ (c >>> 8);
		}
		crc = c;
	}

	public long getValue() {
		return ~crc;
	}

	public void reset() {
		crc = -1;
	}
}
