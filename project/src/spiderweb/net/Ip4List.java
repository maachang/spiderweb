package spiderweb.net;

import java.util.NoSuchElementException;

import spiderweb.utils.ConvIp4;
import spiderweb.utils.ResetIterator;

/**
 * 指定IPアドレス範囲を設定して、IPアドレスを取得.
 */
public class Ip4List implements ResetIterator {
	private long start = -1L;
	private long end = -1L;
	private long nowAddr = -1L;
	
	public Ip4List() {
	}
	
	public Ip4List(String addr) {
		this.start = ConvIp4.ipToInt(addr) & 0x00000000ffffffffL;
		this.end = this.start;
		this.nowAddr = this.start;
	}
	
	public Ip4List(String start, String end) {
		this.start = ConvIp4.ipToInt(start) & 0x00000000ffffffffL;
		this.end = ConvIp4.ipToInt(end) & 0x00000000ffffffffL;
		if(this.start > this.end) {
			long n = this.start;
			this.start = this.end;
			this.end = n;
		}
		this.nowAddr = this.start;
	}
	
	@Override
	public void reset() {
		this.nowAddr = this.start;
	}
	
	@Override
	public boolean hasNext() {
		long n = nowAddr;
		if(n == -1L) {
			return false;
		}
		if((n & 0x000000ffL) == 0 || (n & 0x000000ffL) == 0x000000ffL) {
			n ++;
		}
		if((n & 0x000000ffL) == 0 || (n & 0x000000ffL) == 0x000000ffL) {
			n ++;
		}
		return this.end >= n;
	}
	
	@Override
	public String next() {
		long n = nowAddr;
		if(n == -1L) {
			throw new NoSuchElementException();
		}
		if((n & 0x000000ff) == 0 || (n & 0x000000ff) == 0x000000ff) {
			n ++;
		}
		if((n & 0x000000ff) == 0 || (n & 0x000000ff) == 0x000000ff) {
			n ++;
		}
		if(this.end < n) {
			throw new NoSuchElementException();
		}
		nowAddr = n + 1;
		return ConvIp4.ipToString(n);
	}
}
