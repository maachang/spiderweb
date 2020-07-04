package spiderweb.net;

import java.util.NoSuchElementException;

import spiderweb.utils.ResetIterator;

/**
 * マシン名(IP解決できる名前)のリスト管理.
 */
public class MachineNameList implements ResetIterator {
	private String[] list = null;
	private int no = 0;
	
	public MachineNameList() {
	}
	
	public MachineNameList(String[] list) {
		this.list = list;
	}
	
	@Override
	public void reset() {
		no = 0;
	}
	
	@Override
	public boolean hasNext() {
		if(list == null) {
			return false;
		}
		return no + 1 <= list.length;
	}
	
	@Override
	public String next() {
		if(list == null) {
			throw new NoSuchElementException();
		}
		int n = no;
		if(no >= list.length) {
			throw new NoSuchElementException();
		}
		no ++;
		return list[n];
	}
}
