package spiderweb.utils;

import java.io.OutputStream;

public class BinaryEd {
	private BinaryEd() {}
	
	/**
	 * 1バイトバイナリ変換.
	 * @param buf
	 * @param b
	 * @throws Exception
	 */
	public static final void setByte(OutputStream buf, int b) throws Exception {
		buf.write((b & 0xff));
	}

	/**
	 * 2バイトバイナリ変換.
	 * @param buf
	 * @param b
	 * @throws Exception
	 */
	public static final void setShort(OutputStream buf, int b) throws Exception {
		buf.write(new byte[] { (byte) ((b & 0xff00) >> 8), (byte) (b & 0xff) });
	}

	/**
	 * 4バイトバイナリ変換.
	 * @param buf
	 * @param b
	 * @throws Exception
	 */
	public static final void setInt(OutputStream buf, int b) throws Exception {
		// 4バイトの場合は、先頭2ビットをビット長とする.
		int bit = nlzs(b);
		int src = (bit >> 3) + ((bit & 1) | ((bit >> 1) & 1) | ((bit >> 2) & 1));
		bit = ((bit += 2) >> 3) + ((bit & 1) | ((bit >> 1) & 1) | ((bit >> 2) & 1));

		// 先頭2ビット条件が混同できる場合.
		if (bit == src) {
			switch (bit) {
			case 1:
				buf.write(new byte[] { (byte) (b & 0xff) });
				return;
			case 2:
				buf.write(new byte[] { (byte) (0x40 | ((b & 0xff00) >> 8)), (byte) (b & 0xff) });
				return;
			case 3:
				buf.write(new byte[] { (byte) (0x80 | ((b & 0xff0000) >> 16)), (byte) ((b & 0xff00) >> 8),
						(byte) (b & 0xff) });
				return;
			case 4:
				buf.write(new byte[] { (byte) (0xc0 | ((b & 0xff000000) >> 24)), (byte) ((b & 0xff0000) >> 16),
						(byte) ((b & 0xff00) >> 8), (byte) (b & 0xff) });
				return;
			}
		}
		// 先頭2ビット条件が混同できない場合.
		switch (src) {
		case 0:
		case 1:
			buf.write(new byte[] { (byte) 0, (byte) (b & 0xff) });
			return;
		case 2:
			buf.write(new byte[] { (byte) 0x40, (byte) ((b & 0xff00) >> 8), (byte) (b & 0xff) });
			return;
		case 3:
			buf.write(new byte[] { (byte) 0x80, (byte) ((b & 0xff0000) >> 16), (byte) ((b & 0xff00) >> 8),
					(byte) (b & 0xff) });
			return;
		case 4:
			buf.write(new byte[] { (byte) 0xc0, (byte) ((b & 0xff000000) >> 24), (byte) ((b & 0xff0000) >> 16),
					(byte) ((b & 0xff00) >> 8), (byte) (b & 0xff) });
			return;
		}
	}

	/**
	 * 8バイトバイナリ変換.
	 * @param buf
	 * @param b
	 * @throws Exception
	 */
	public static final void setLong(OutputStream buf, long b) throws Exception {
		// 8バイトの場合は、先頭3ビットをビット長とする.
		int bit = nlzs(b);
		int src = (bit >> 3) + ((bit & 1) | ((bit >> 1) & 1) | ((bit >> 2) & 1));
		bit = ((bit += 3) >> 3) + ((bit & 1) | ((bit >> 1) & 1) | ((bit >> 2) & 1));

		// 先頭3ビット条件が混同できる場合.
		if (bit == src) {
			switch (bit) {
			case 1:
				buf.write(new byte[] { (byte) (b & 0xffL) });
				return;
			case 2:
				buf.write(new byte[] { (byte) (0x20 | ((b & 0xff00L) >> 8L)), (byte) (b & 0xffL) });
				return;
			case 3:
				buf.write(new byte[] { (byte) (0x40 | ((b & 0xff0000L) >> 16L)), (byte) ((b & 0xff00L) >> 8L),
						(byte) (b & 0xffL) });
				return;
			case 4:
				buf.write(new byte[] { (byte) (0x60 | ((b & 0xff000000L) >> 24L)), (byte) ((b & 0xff0000L) >> 16L),
						(byte) ((b & 0xff00L) >> 8L), (byte) (b & 0xffL) });
				return;
			case 5:
				buf.write(new byte[] { (byte) (0x80 | ((b & 0xff00000000L) >> 32L)), (byte) ((b & 0xff000000L) >> 24L),
						(byte) ((b & 0xff0000L) >> 16L), (byte) ((b & 0xff00L) >> 8L), (byte) (b & 0xffL) });
				return;
			case 6:
				buf.write(new byte[] { (byte) (0xA0 | ((b & 0xff0000000000L) >> 40L)),
						(byte) ((b & 0xff00000000L) >> 32L), (byte) ((b & 0xff000000L) >> 24L),
						(byte) ((b & 0xff0000L) >> 16L), (byte) ((b & 0xff00L) >> 8L), (byte) (b & 0xffL) });
				return;
			case 7:
				buf.write(new byte[] { (byte) (0xC0 | ((b & 0xff000000000000L) >> 48L)),
						(byte) ((b & 0xff0000000000L) >> 40L), (byte) ((b & 0xff00000000L) >> 32L),
						(byte) ((b & 0xff000000L) >> 24L), (byte) ((b & 0xff0000L) >> 16L),
						(byte) ((b & 0xff00L) >> 8L), (byte) (b & 0xffL) });
				return;
			case 8:
				buf.write(new byte[] { (byte) (0xE0 | ((b & 0xff00000000000000L) >> 56L)),
						(byte) ((b & 0xff000000000000L) >> 48L), (byte) ((b & 0xff0000000000L) >> 40L),
						(byte) ((b & 0xff00000000L) >> 32L), (byte) ((b & 0xff000000L) >> 24L),
						(byte) ((b & 0xff0000L) >> 16L), (byte) ((b & 0xff00L) >> 8L), (byte) (b & 0xffL) });
				return;
			}
		}
		// 先頭3ビット条件が混同できない場合.
		switch (src) {
		case 0:
		case 1:
			buf.write(new byte[] { (byte) 0, (byte) (b & 0xffL) });
			return;
		case 2:
			buf.write(new byte[] { (byte) 0x20, (byte) ((b & 0xff00L) >> 8L), (byte) (b & 0xffL) });
			return;
		case 3:
			buf.write(new byte[] { (byte) 0x40, (byte) ((b & 0xff0000L) >> 16L), (byte) ((b & 0xff00L) >> 8L),
					(byte) (b & 0xffL) });
			return;
		case 4:
			buf.write(new byte[] { (byte) 0x60, (byte) ((b & 0xff000000L) >> 24L), (byte) ((b & 0xff0000L) >> 16L),
					(byte) ((b & 0xff00L) >> 8L), (byte) (b & 0xffL) });
			return;
		case 5:
			buf.write(new byte[] { (byte) 0x80, (byte) ((b & 0xff00000000L) >> 32L), (byte) ((b & 0xff000000L) >> 24L),
					(byte) ((b & 0xff0000L) >> 16L), (byte) ((b & 0xff00L) >> 8L), (byte) (b & 0xffL) });
			return;
		case 6:
			buf.write(new byte[] { (byte) 0xA0, (byte) ((b & 0xff0000000000L) >> 40L),
					(byte) ((b & 0xff00000000L) >> 32L), (byte) ((b & 0xff000000L) >> 24L),
					(byte) ((b & 0xff0000L) >> 16L), (byte) ((b & 0xff00L) >> 8L), (byte) (b & 0xffL) });
			return;
		case 7:
			buf.write(new byte[] { (byte) 0xC0, (byte) ((b & 0xff000000000000L) >> 48L),
					(byte) ((b & 0xff0000000000L) >> 40L), (byte) ((b & 0xff00000000L) >> 32L),
					(byte) ((b & 0xff000000L) >> 24L), (byte) ((b & 0xff0000L) >> 16L), (byte) ((b & 0xff00L) >> 8L),
					(byte) (b & 0xffL) });
			return;
		case 8:
			buf.write(new byte[] { (byte) 0xE0, (byte) ((b & 0xff00000000000000L) >> 56L),
					(byte) ((b & 0xff000000000000L) >> 48L), (byte) ((b & 0xff0000000000L) >> 40L),
					(byte) ((b & 0xff00000000L) >> 32L), (byte) ((b & 0xff000000L) >> 24L),
					(byte) ((b & 0xff0000L) >> 16L), (byte) ((b & 0xff00L) >> 8L), (byte) (b & 0xffL) });
			return;
		}
	}
	
	/**
	 * 1バイトバイナリ変換.
	 * @param out
	 * @param off
	 * @param b
	 * @retrn int
	 */
	public static final int setByte(byte[] out, int off, int b) {
		out[off ++] = (byte)(b & 0xff);
		return off;
	}

	/**
	 * 2バイトバイナリ変換.
	 * @param out
	 * @param off
	 * @param b
	 * @retrn int
	 */
	public static final int setShort(byte[] out, int off, int b) {
		out[off ++] = (byte) ((b & 0xff00) >> 8);
		out[off ++] = (byte) (b & 0xff);
		return off;
	}

	/**
	 * 4バイトバイナリ変換.
	 * @param out
	 * @param off
	 * @param b
	 * @retrn int
	 */
	public static final int setInt(byte[] out, int off, int b) {
		// 4バイトの場合は、先頭2ビットをビット長とする.
		int bit = nlzs(b);
		int src = (bit >> 3) + ((bit & 1) | ((bit >> 1) & 1) | ((bit >> 2) & 1));
		bit = ((bit += 2) >> 3) + ((bit & 1) | ((bit >> 1) & 1) | ((bit >> 2) & 1));

		// 先頭2ビット条件が混同できる場合.
		if (bit == src) {
			switch (bit) {
			case 1:
				out[off ++] = (byte) (b & 0xff);
				return off;
			case 2:
				out[off ++] = (byte) (0x40 | ((b & 0xff00) >> 8));
				out[off ++] = (byte) (b & 0xff);
				return off;
			case 3:
				out[off ++] = (byte) (0x80 | ((b & 0xff0000) >> 16));
				out[off ++] = (byte) ((b & 0xff00) >> 8);
				out[off ++] = (byte) (b & 0xff);
				return off;
			case 4:
				out[off ++] = (byte) (0xc0 | ((b & 0xff000000) >> 24));
				out[off ++] = (byte) ((b & 0xff0000) >> 16);
				out[off ++] = (byte) ((b & 0xff00) >> 8);
				out[off ++] = (byte) (b & 0xff);
				return off;
			}
		}
		// 先頭2ビット条件が混同できない場合.
		switch (src) {
		case 0:
		case 1:
			out[off ++] = (byte) 0;
			out[off ++] = (byte) (b & 0xff);
			return off;
		case 2:
			out[off ++] = (byte) 0x40;
			out[off ++] = (byte) ((b & 0xff00) >> 8);
			out[off ++] = (byte) (b & 0xff);
			return off;
		case 3:
			out[off ++] = (byte) 0x80;
			out[off ++] = (byte) ((b & 0xff0000) >> 16);
			out[off ++] = (byte) ((b & 0xff00) >> 8);
			out[off ++] = (byte) (b & 0xff);
			return off;
		case 4:
			out[off ++] = (byte) 0xc0;
			out[off ++] = (byte) ((b & 0xff000000) >> 24);
			out[off ++] = (byte) ((b & 0xff0000) >> 16);
			out[off ++] = (byte) ((b & 0xff00) >> 8);
			out[off ++] = (byte) (b & 0xff);
			return off;
		}
		return off;
	}

	/**
	 * 8バイトバイナリ変換.
	 * @param out
	 * @param off
	 * @param b
	 * @retrn int
	 */
	public static final int setLong(byte[] out, int off, long b) {
		// 8バイトの場合は、先頭3ビットをビット長とする.
		int bit = nlzs(b);
		int src = (bit >> 3) + ((bit & 1) | ((bit >> 1) & 1) | ((bit >> 2) & 1));
		bit = ((bit += 3) >> 3) + ((bit & 1) | ((bit >> 1) & 1) | ((bit >> 2) & 1));

		// 先頭3ビット条件が混同できる場合.
		if (bit == src) {
			switch (bit) {
			case 1:
				out[off ++] = (byte) (b & 0xffL);
				return off;
			case 2:
				out[off ++] = (byte) (0x20 | ((b & 0xff00L) >> 8L));
				out[off ++] = (byte) (b & 0xffL);
				return off;
			case 3:
				out[off ++] = (byte) (0x40 | ((b & 0xff0000L) >> 16L));
				out[off ++] = (byte) ((b & 0xff00L) >> 8L);
				out[off ++] = (byte) (b & 0xffL);
				return off;
			case 4:
				out[off ++] = (byte) (0x60 | ((b & 0xff000000L) >> 24L));
				out[off ++] = (byte) ((b & 0xff0000L) >> 16L);
				out[off ++] = (byte) ((b & 0xff00L) >> 8L);
				out[off ++] = (byte) (b & 0xffL);
				return off;
			case 5:
				out[off ++] = (byte) (0x80 | ((b & 0xff00000000L) >> 32L));
				out[off ++] = (byte) ((b & 0xff000000L) >> 24L);
				out[off ++] = (byte) ((b & 0xff0000L) >> 16L);
				out[off ++] = (byte) ((b & 0xff00L) >> 8L);
				out[off ++] = (byte) (b & 0xffL);
				return off;
			case 6:
				out[off ++] = (byte) (0xA0 | ((b & 0xff0000000000L) >> 40L));
				out[off ++] = (byte) ((b & 0xff00000000L) >> 32L);
				out[off ++] = (byte) ((b & 0xff000000L) >> 24L);
				out[off ++] = (byte) ((b & 0xff0000L) >> 16L);
				out[off ++] = (byte) ((b & 0xff00L) >> 8L);
				out[off ++] = (byte) (b & 0xffL);
				return off;
			case 7:
				out[off ++] = (byte) (0xC0 | ((b & 0xff000000000000L) >> 48L));
				out[off ++] = (byte) ((b & 0xff0000000000L) >> 40L);
				out[off ++] = (byte) ((b & 0xff00000000L) >> 32L);
				out[off ++] = (byte) ((b & 0xff000000L) >> 24L);
				out[off ++] = (byte) ((b & 0xff0000L) >> 16L);
				out[off ++] = (byte) ((b & 0xff00L) >> 8L);
				out[off ++] = (byte) (b & 0xffL);
				return off;
			case 8:
				out[off ++] = (byte) (0xE0 | ((b & 0xff00000000000000L) >> 56L));
				out[off ++] = (byte) ((b & 0xff000000000000L) >> 48L);
				out[off ++] = (byte) ((b & 0xff0000000000L) >> 40L);
				out[off ++] = (byte) ((b & 0xff00000000L) >> 32L);
				out[off ++] = (byte) ((b & 0xff000000L) >> 24L);
				out[off ++] = (byte) ((b & 0xff0000L) >> 16L);
				out[off ++] = (byte) ((b & 0xff00L) >> 8L);
				out[off ++] = (byte) (b & 0xffL);
				return off;
			}
		}
		// 先頭3ビット条件が混同できない場合.
		switch (src) {
		case 0:
		case 1:
			out[off ++] = (byte) 0;
			out[off ++] = (byte) (b & 0xffL);
			return off;
		case 2:
			out[off ++] = (byte) 0x20;
			out[off ++] = (byte) ((b & 0xff00L) >> 8L);
			out[off ++] = (byte) (b & 0xffL);
			return off;
		case 3:
			out[off ++] = (byte) 0x40;
			out[off ++] = (byte) ((b & 0xff0000L) >> 16L);
			out[off ++] = (byte) ((b & 0xff00L) >> 8L);
			out[off ++] = (byte) (b & 0xffL);
			return off;
		case 4:
			out[off ++] = (byte) 0x60;
			out[off ++] = (byte) ((b & 0xff000000L) >> 24L);
			out[off ++] = (byte) ((b & 0xff0000L) >> 16L);
			out[off ++] = (byte) ((b & 0xff00L) >> 8L);
			out[off ++] = (byte) (b & 0xffL);
			return off;
		case 5:
			out[off ++] = (byte) 0x80;
			out[off ++] = (byte) ((b & 0xff00000000L) >> 32L);
			out[off ++] = (byte) ((b & 0xff000000L) >> 24L);
			out[off ++] = (byte) ((b & 0xff0000L) >> 16L);
			out[off ++] = (byte) ((b & 0xff00L) >> 8L);
			out[off ++] = (byte) (b & 0xffL);
			return off;
		case 6:
			out[off ++] = (byte) 0xA0;
			out[off ++] = (byte) ((b & 0xff0000000000L) >> 40L);
			out[off ++] = (byte) ((b & 0xff00000000L) >> 32L);
			out[off ++] = (byte) ((b & 0xff000000L) >> 24L);
			out[off ++] = (byte) ((b & 0xff0000L) >> 16L);
			out[off ++] = (byte) ((b & 0xff00L) >> 8L);
			out[off ++] = (byte) (b & 0xffL);
			return off;
		case 7:
			out[off ++] = (byte) 0xC0;
			out[off ++] = (byte) ((b & 0xff000000000000L) >> 48L);
			out[off ++] = (byte) ((b & 0xff0000000000L) >> 40L);
			out[off ++] = (byte) ((b & 0xff00000000L) >> 32L);
			out[off ++] = (byte) ((b & 0xff000000L) >> 24L);
			out[off ++] = (byte) ((b & 0xff0000L) >> 16L);
			out[off ++] = (byte) ((b & 0xff00L) >> 8L);
			out[off ++] = (byte) (b & 0xffL);
			return off;
		case 8:
			out[off ++] = (byte) 0xE0;
			out[off ++] = (byte) ((b & 0xff00000000000000L) >> 56L);
			out[off ++] = (byte) ((b & 0xff000000000000L) >> 48L);
			out[off ++] = (byte) ((b & 0xff0000000000L) >> 40L);
			out[off ++] = (byte) ((b & 0xff00000000L) >> 32L);
			out[off ++] = (byte) ((b & 0xff000000L) >> 24L);
			out[off ++] = (byte) ((b & 0xff0000L) >> 16L);
			out[off ++] = (byte) ((b & 0xff00L) >> 8L);
			out[off ++] = (byte) (b & 0xffL);
			return off;
		}
		return off;
	}

	/**
	 * 1バイト数値変換.
	 * @param b
	 * @param off
	 * @return
	 */
	public static final int getByte(byte[] b, int[] off) {
		return b[off[0]++] & 0xff;
	}
	
	/**
	 * 2バイト数値変換.
	 * @param b
	 * @param off
	 * @return
	 */
	public static final int getShort(byte[] b, int[] off) {
		return ((b[off[0]++] & 0xff) << 8) | (b[off[0]++] & 0xff);
	}
	
	/**
	 * 4バイト数値変換.
	 * @param b
	 * @param off
	 * @return
	 */
	public static final int getInt(byte[] b, int[] off) {
		int o = off[0];
		if ((b[o] & 0x3f) == 0) {
			// ヘッダ2ビットが単体１バイト定義の場合.
			switch (((b[o] & 0xc0) >> 6) + 1) {
			case 1:
				off[0] += 2;
				return (b[o + 1] & 0xff);
			case 2:
				off[0] += 3;
				return ((b[o + 1] & 0xff) << 8) | (b[o + 2] & 0xff);
			case 3:
				off[0] += 4;
				return ((b[o + 1] & 0xff) << 16) | ((b[o + 2] & 0xff) << 8) | (b[o + 3] & 0xff);
			case 4:
				off[0] += 5;
				return ((b[o + 1] & 0xff) << 24) | ((b[o + 2] & 0xff) << 16) | ((b[o + 3] & 0xff) << 8)
						| (b[o + 4] & 0xff);
			}
			throw new IllegalArgumentException("Invalid byte 4 int condition: " + off[0]);
		}
		// ヘッダ2ビットが混在定義の場合.
		switch (((b[o] & 0xc0) >> 6) + 1) {
		case 1:
			off[0] += 1;
			return (b[o] & 0x3f);
		case 2:
			off[0] += 2;
			return ((b[o] & 0x3f) << 8) | (b[o + 1] & 0xff);
		case 3:
			off[0] += 3;
			return ((b[o] & 0x3f) << 16) | ((b[o + 1] & 0xff) << 8) | (b[o + 2] & 0xff);
		case 4:
			off[0] += 4;
			return ((b[o] & 0x3f) << 24) | ((b[o + 1] & 0xff) << 16) | ((b[o + 2] & 0xff) << 8) | (b[o + 3] & 0xff);
		}
		throw new IllegalArgumentException("Invalid byte 4 int condition: " + off[0]);
	}

	/**
	 * 8バイト数値変換.
	 * @param b
	 * @param off
	 * @return
	 */
	public static final long getLong(byte[] b, int[] off) {
		int o = off[0];
		if ((b[o] & 0x1f) == 0) {
			// ヘッダ3ビットが単体１バイト定義の場合.
			switch (((b[o] & 0xe0) >> 5) + 1) {
			case 1:
				off[0] += 2;
				return (long) (b[o + 1] & 0xff);
			case 2:
				off[0] += 3;
				return (long) (((b[o + 1] & 0xff) << 8) | (b[o + 2] & 0xff));
			case 3:
				off[0] += 4;
				return (long) (((b[o + 1] & 0xff) << 16) | ((b[o + 2] & 0xff) << 8) | (b[o + 3] & 0xff));
			case 4:
				off[0] += 5;
				return (long) (((b[o + 1] & 0xff) << 24) | ((b[o + 2] & 0xff) << 16) | ((b[o + 3] & 0xff) << 8)
						| (b[o + 4] & 0xff));
			case 5:
				off[0] += 6;
				return (long) (((b[o + 1] & 0xffL) << 32L) | ((b[o + 2] & 0xffL) << 24L) | ((b[o + 3] & 0xffL) << 16L)
						| ((b[o + 4] & 0xffL) << 8L) | (b[o + 5] & 0xffL));
			case 6:
				off[0] += 7;
				return (long) (((b[o + 1] & 0xffL) << 40L) | ((b[o + 2] & 0xffL) << 32L) | ((b[o + 3] & 0xffL) << 24L)
						| ((b[o + 4] & 0xffL) << 16L) | ((b[o + 5] & 0xffL) << 8L) | (b[o + 6] & 0xffL));
			case 7:
				off[0] += 8;
				return (long) (((b[o + 1] & 0xffL) << 48L) | ((b[o + 2] & 0xffL) << 40L) | ((b[o + 3] & 0xffL) << 32L)
						| ((b[o + 4] & 0xffL) << 24L) | ((b[o + 5] & 0xffL) << 16L) | ((b[o + 6] & 0xffL) << 8L)
						| (b[o + 7] & 0xffL));
			case 8:
				off[0] += 9;
				return (long) (((b[o + 1] & 0xffL) << 56L) | ((b[o + 2] & 0xffL) << 48L) | ((b[o + 3] & 0xffL) << 40L)
						| ((b[o + 4] & 0xffL) << 32L) | ((b[o + 5] & 0xffL) << 24L) | ((b[o + 6] & 0xffL) << 16L)
						| ((b[o + 7] & 0xffL) << 8L) | (b[o + 8] & 0xffL));
			}
			throw new IllegalArgumentException("Invalid byte 8 long condition: " + off[0]);
		}
		// ヘッダ3ビットが混在定義の場合.
		switch (((b[o] & 0xe0) >> 5) + 1) {
		case 1:
			off[0] += 1;
			return (long) (b[o] & 0x1f);
		case 2:
			off[0] += 2;
			return (long) (((b[o] & 0x1f) << 8) | (b[o + 1] & 0xff));
		case 3:
			off[0] += 3;
			return (long) (((b[o] & 0x1f) << 16) | ((b[o + 1] & 0xff) << 8) | (b[o + 2] & 0xff));
		case 4:
			off[0] += 4;
			return (long) (((b[o] & 0x1f) << 24) | ((b[o + 1] & 0xff) << 16) | ((b[o + 2] & 0xff) << 8)
					| (b[o + 3] & 0xff));
		case 5:
			off[0] += 5;
			return (long) (((b[o] & 0x1fL) << 32L) | ((b[o + 1] & 0xffL) << 24L) | ((b[o + 2] & 0xffL) << 16L)
					| ((b[o + 3] & 0xffL) << 8L) | (b[o + 4] & 0xffL));
		case 6:
			off[0] += 6;
			return (long) (((b[o] & 0x1fL) << 40L) | ((b[o + 1] & 0xffL) << 32L) | ((b[o + 2] & 0xffL) << 24L)
					| ((b[o + 3] & 0xffL) << 16L) | ((b[o + 4] & 0xffL) << 8L) | (b[o + 5] & 0xffL));
		case 7:
			off[0] += 7;
			return (long) (((b[o] & 0x1fL) << 48L) | ((b[o + 1] & 0xffL) << 40L) | ((b[o + 2] & 0xffL) << 32L)
					| ((b[o + 3] & 0xffL) << 24L) | ((b[o + 4] & 0xffL) << 16L) | ((b[o + 5] & 0xffL) << 8L)
					| (b[o + 6] & 0xffL));
		case 8:
			off[0] += 8;
			return (long) (((b[o] & 0x1fL) << 56L) | ((b[o + 1] & 0xffL) << 48L) | ((b[o + 2] & 0xffL) << 40L)
					| ((b[o + 3] & 0xffL) << 32L) | ((b[o + 4] & 0xffL) << 24L) | ((b[o + 5] & 0xffL) << 16L)
					| ((b[o + 6] & 0xffL) << 8L) | (b[o + 7] & 0xffL));
		}
		throw new IllegalArgumentException("Invalid byte 8 long condition: " + off[0]);
	}
	
	/**
	 * 有効最大ビット長を取得.
	 * 
	 * @param x 対象の数値を設定します.
	 * @return int 左ゼロビット数が返却されます.
	 */
	private static final int nlzs(int x) {
		if (x == 0) {
			return 0;
		}
		x |= (x >> 1);
		x |= (x >> 2);
		x |= (x >> 4);
		x |= (x >> 8);
		x |= (x >> 16);
		x = (x & 0x55555555) + (x >> 1 & 0x55555555);
		x = (x & 0x33333333) + (x >> 2 & 0x33333333);
		x = (x & 0x0f0f0f0f) + (x >> 4 & 0x0f0f0f0f);
		x = (x & 0x00ff00ff) + (x >> 8 & 0x00ff00ff);
		return (x & 0x0000ffff) + (x >> 16 & 0x0000ffff);
	}

	/**
	 * 有効最大ビット長を取得.
	 * 
	 * @param x 対象の数値を設定します.
	 * @return int 左ゼロビット数が返却されます.
	 */
	private static final int nlzs(long x) {
		int xx = (int) ((x & 0xffffffff00000000L) >> 32L);
		if (nlzs(xx) == 0) {
			return nlzs((int) (x & 0x00000000ffffffff));
		}
		return nlzs(xx) + 32;
	}

}
