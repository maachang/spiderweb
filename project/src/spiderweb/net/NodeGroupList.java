package spiderweb.net;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import spiderweb.utils.ConvIp4;
import spiderweb.utils.ResetIterator;

/**
 * ノードグループリスト.
 */
public class NodeGroupList implements ResetIterator {
	private String groupName = null;
	private ResetIterator[] list = null;
	private int targetNo = 0;
	
	public NodeGroupList(String name, List<List<String>> list) {
		_create(name, list);
	}
	
	private void _create(String name, List<List<String>> list) {
		int ipLen;
		List<String> n;
		final List<ResetIterator> its = new ArrayList<ResetIterator>();
		final List<String> machineNames = new ArrayList<String>();
		int len = list.size();
		for(int i = 0; i < len; i ++) {
			n = list.get(i);
			ipLen = n.size();
			if(ipLen > 2) { 
				ipLen = 2;
			}
			switch(ipLen) {
			case 0: break;
			case 1:
			case 2:
				if(ConvIp4.isIp(n.get(0))) {
					if(ipLen == 1) {
						its.add(new Ip4List(n.get(0)));
					} else {
						its.add(new Ip4List(n.get(0), n.get(1)));
					}
				} else {
					machineNames.add(n.get(0));
				}
				break;
			}
		}
		if(machineNames.size() > 0) {
			len = machineNames.size();
			String[] names = new String[len];
			for(int i = 0; i < len; i ++) {
				names[i] = machineNames.get(i);
			}
			its.add(new MachineNameList(names));
		}
		len = its.size();
		ResetIterator[] riList = new ResetIterator[len];
		for(int i = 0; i < len; i ++) {
			riList[i] = its.get(i);
		}
		this.groupName = name;
		this.list = riList;
		this.targetNo = 0;
	}
	
	public String getNodeGroupName() {
		return groupName;
	}
	
	@Override
	public void reset() {
		this.targetNo = 0;
		int len = list.length;
		for(int i = 0; i < len; i ++) {
			list[i].reset();
		}
	}
	
	@Override
	public boolean hasNext() {
		int n = targetNo;
		while(!list[n].hasNext()) {
			if(n + 1 >= list.length) {
				return false;
			}
			n ++;
		}
		return true;
	}
	
	@Override
	public String next() {
		while(!list[targetNo].hasNext()) {
			if(targetNo +1 >= list.length) {
				throw new NoSuchElementException();
			}
			targetNo ++;
		}
		return list[targetNo].next();
	}
}
