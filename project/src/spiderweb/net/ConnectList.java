package spiderweb.net;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import spiderweb.SpiderWebElement;
import spiderweb.utils.CRC64;
import spiderweb.utils.ConvIp4;
import spiderweb.utils.Flag;

/**
 * 接続が確立しているリスト.
 */
public class ConnectList {
	private static final double REMOVE_LIST = 2d;
	private String groupName;
	private final Map<String, ConnectElement> list = new ConcurrentHashMap<String, ConnectElement>();
	private final AtomicReference<String[]> connetAddrs = new AtomicReference<String[]>();
	private final AtomicLong connectSync = new AtomicLong(0L);
	private long expireTime = -1L;
	private long removeTime = -1L;
	
	// createConnectAddrs再生性フラグ.
	private final Flag updateFlag = new Flag(true);
	
	/**
	 * コンストラクタ.
	 * @param name
	 * @param time
	 */
	public ConnectList(String name, long time) {
		this.groupName = name;
		this.expireTime = time;
		this.removeTime = (long)((double)time * REMOVE_LIST);
	}
	
	/**
	 * ノードグループ名を取得.
	 * @return
	 */
	public String getNodeGroupName() {
		return groupName;
	}
	
	/**
	 * expire時間を取得
	 * @return.
	 */
	public long getExpireTime() {
		return expireTime;
	}
	
	/**
	 * 新しいアドレスをセットして、更新時間を更新.
	 * @param addr
	 * @param em
	 */
	public void put(String addr, SpiderWebElement em) {
		list.put(addr, new ConnectElement(em));
		updateFlag.set(true);
	}
	
	/**
	 * 存在する情報は更新せず、存在しない場合だけ更新時間を更新.
	 * @param addr
	 * @param em
	 */
	public void putToNoUpdate(String addr, SpiderWebElement em) {
		if(!list.containsKey(addr)) {
			list.put(addr, new ConnectElement(em));
		}
		updateFlag.set(true);
	}
	
	/**
	 * 指定アドレス情報が存在する場合、更新時間を更新.
	 * @param addr
	 * @param em
	 * @return
	 */
	public boolean update(String addr, SpiderWebElement em) {
		if(list.containsKey(addr)) {
			list.get(addr).set(em).update();
			updateFlag.set(true);
			return true;
		}
		return false;
	}
	
	/**
	 * inetAddressの情報で既に一致する情報が存在するかチェック.
	 * IPアドレスか、ホスト名か存在する場合は[true]返却.
	 * @param addr
	 * @param em
	 * @return
	 */
	public boolean isInetAddressByUpdate(InetAddress addr, SpiderWebElement em) {
		String haddr = addr.getHostAddress();
		if(list.containsKey(haddr)) {
			list.get(haddr).set(em).update();
			updateFlag.set(true);
			return true;
		} else if(list.containsKey(haddr)) {
			list.get(addr.getHostName()).set(em).update();
			updateFlag.set(true);
			return true;
		}
		return false;
	}
	
	/**
	 * inetAddressの情報で既に一致する情報が存在するかチェック.
	 * IPアドレスか、ホスト名か存在する場合は[true]返却.
	 * @param addr
	 * @return
	 */
	public boolean isInetAddress(InetAddress addr) {
		return (list.containsKey(addr.getHostAddress()) || list.containsKey(addr.getHostName()));
	}
	
	/**
	 * 指定アドレスを削除.
	 * @param addr
	 */
	public void remove(String addr) {
		if(list.containsKey(addr)) {
			// 物理削除せず、論理削除のみとする.
			ConnectElement e = list.get(addr);
			e.setTime(System.currentTimeMillis() - expireTime);
			updateFlag.set(true);
		}
	}
	
	/**
	 * 一定時間が超えている情報を削除.
	 * @return
	 */
	public int removeExpire() {
		Long n;
		Entry<String, ConnectElement> e;
		int ret = 0;
		// 物理削除する.
		final long target = System.currentTimeMillis() - removeTime;
		final Iterator<Entry<String, ConnectElement>> it = list.entrySet().iterator();
		while(it.hasNext()) {
			e = it.next();
			if((n = e.getValue().getTime()) != null && n < target) {
				list.remove(e.getKey());
				updateFlag.set(true);
				ret ++;
			}
		}
		return ret;
	}
	
	// コネクションアドレスリスト、チェックサムを取得.
	private final String[] createConnectAddrs() throws IOException {
		updateFlag.set(false);
		// 接続先一覧をリスト化して、ソート.
		int len, cnt;
		long oldSync;
		String[] tmp;
		String[] old;
		String[] addrs;
		Entry<String, ConnectElement> e;
		Iterator<Entry<String, ConnectElement>> it;
		final byte[] b = new byte[4];
		final CRC64 crc = new CRC64();
		// 論理削除された条件は非表示.
		final long target = System.currentTimeMillis() - expireTime;
		while(true) {
			old = connetAddrs.get();
			oldSync = connectSync.get();
			len = list.size();
			it = list.entrySet().iterator();
			cnt = 0;
			addrs = new String[len];
			while(it.hasNext()) {
				if((e = it.next()).getValue().getTime() >= target) {
					addrs[cnt++] = e.getKey();
				}
			}
			it = null; e = null;
			if(len != cnt) {
				tmp = addrs;
				addrs = new String[cnt];
				System.arraycopy(tmp, 0, addrs, 0, cnt);
				tmp = null;
				len = cnt;
			}
			Arrays.sort(addrs);
			for(int i = 0; i < len ; i++) {
				if(ConvIp4.isIp(addrs[i])) {
					ConvIp4.ipToBin(b, addrs[i]);
					crc.update(b);
				} else {
					crc.update(addrs[i].getBytes("UTF8"));
				}
			}
			if(connetAddrs.compareAndSet(old, addrs) &&
				connectSync.compareAndSet(oldSync, crc.getValue())) {
				break;
			}
			crc.reset();
		}
		return addrs;
	}
	
	/**
	 * 指定アドレスの存在確認最終時間を取得.
	 * @param addr
	 * @return
	 */
	public Long getUpdateTime(String addr) {
		if(list.containsKey(addr)) {
			return list.get(addr).getTime();
		}
		return null;
	}
	
	/**
	 * 指定アドレスのステータス情報を取得.
	 * @param addr
	 * @return
	 */
	public Integer getStatus(String addr) {
		if(list.containsKey(addr)) {
			return list.get(addr).getStatus();
		}
		return null;
	}
	
	/**
	 * 指定アドレスのCPU利用率を取得.
	 * @param addr
	 * @return
	 */
	public Integer getCpuLoad(String addr) {
		if(list.containsKey(addr)) {
			return list.get(addr).getCpuLoad();
		}
		return null;
	}
	
	/**
	 * 指定アドレスのマシン番号を取得.
	 * @param addr
	 * @return
	 */
	public Integer getMachineNo(String addr) {
		if(list.containsKey(addr)) {
			return list.get(addr).getMachineNo();
		}
		return null;
	}
	
	/**
	 * 指定アドレスのマシン番号決定時間を取得.
	 * @param addr
	 * @return
	 */
	public Long getMachineNoTime(String addr) {
		if(list.containsKey(addr)) {
			return list.get(addr).getMachineNoTime();
		}
		return null;
	}
	
	/**
	 * 指定アドレスの要素を取得.
	 * @param addr
	 * @return
	 */
	public ConnectElement getElement(String addr) {
		if(list.containsKey(addr)) {
			return list.get(addr);
		}
		return null;
	}
	
	/**
	 * コネクションアドレスのチェックサムを取得.
	 * @return
	 * @throws IOException
	 */
	public long getConnectChecksum() throws IOException {
		if(updateFlag.get()) {
			createConnectAddrs();
		}
		return connectSync.get();
	}
	
	/**
	 * 現状のコネクションアドレス一覧を取得.
	 * @return
	 * @throws IOException
	 */
	public String[] getConnectAddress() throws IOException {
		if(updateFlag.get()) {
			return createConnectAddrs();
		}
		return connetAddrs.get();
	}
	
	/**
	 * 現状のコネクションアドレス一覧の項番を指定して取得.
	 * @param no
	 * @return
	 * @throws IOException
	 */
	public String get(int no) throws IOException {
		String[] n = getConnectAddress();
		if(no < 0 || no >= n.length) {
			return null;
		}
		return n[no];
	}
	
	/**
	 * 現状のコネクションアドレス一覧数を取得.
	 * @return
	 * @throws IOException
	 */
	public int size() throws IOException {
		return getConnectAddress().length;
	}
	
	/**
	 * 空かチェック.
	 * @return
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}
}
