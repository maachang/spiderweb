package spiderweb;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import spiderweb.net.ConnectElement;
import spiderweb.net.ConnectList;
import spiderweb.net.ExistenceIO;
import spiderweb.net.ExistenceReceiveCall;
import spiderweb.net.NodeGroupList;
import spiderweb.net.SendBuffer;
import spiderweb.net.ThisMachineAddress;
import spiderweb.utils.AtomicNumber;
import spiderweb.utils.AtomicNumber64;
import spiderweb.utils.ConvIp4;
import spiderweb.utils.OsCpuLoad;

/**
 * spiderweb.
 */
public class SpiderWeb {
	private static final int TIMEOUT = 250;
	public static final int TYPE_RECEIVE = 0; // 受信処理用スレッド.
	public static final int TYPE_CONNECT = 1; // 起動時にNodeGroupListに従い、コネクション情報を送信.
	public static final int TYPE_SYNC= 2; // ConnectListに対して、接続情報の同期を取る.
	
	// nodeグループ別のコネクション情報を保持する
	private Map<String, ConnectList> connectNodeGroup = new ConcurrentHashMap<String, ConnectList>();
	
	// 受信スレッド.
	private ExecuteThread recvThread = null;
	
	// コネクション・同期スレッド.
	private ExecuteThread connectThread = null;
	
	// カスタム送信用、UDP送信処理.
	private ExistenceIO customSend = null;
	private SendBuffer customSendBuffer = null;
	
	// このマシンのステータス情報.
	private final AtomicNumber machineStatus = new AtomicNumber();
	
	// このマシンのCPU情報(平均).
	private final AtomicNumber machineCpuLoad = new AtomicNumber();
	
	// このマシンの番号.
	private final AtomicNumber machineNo = new AtomicNumber();
	
	// このマシンの番号が決定した日時.
	private final AtomicNumber64 machineNoTime = new AtomicNumber64();
	
	private final SpiderWebElement machineElement() {
		return new SpiderWebElement(machineStatus.get(), machineCpuLoad.get(), machineNo.get(), machineNoTime.get());
	}
	
	// 実行スレッド.
	@SuppressWarnings("unused")
	private static final class ExecuteThread extends Thread {
		private boolean startFlag = false;
		private volatile boolean stopFlag = false;
		protected volatile int type = -1;
		protected SpiderWebConfig config = null;
		protected Map<String, ConnectList> connectNodeGroup = null;
		protected long expireTime = -1L;
		protected long syncTime = -1L;
		protected long nextTime = -1L;
		protected ExistenceIO existenceIO = null;
		protected SendBuffer sendBuf = null;
		protected AtomicNumber machineStatus = null;
		protected AtomicNumber machineCpuLoad = null;
		protected AtomicNumber machineNo = null;
		protected AtomicNumber64 machineNoTime = null;
		
		// 受信スレッドを作成.
		public ExecuteThread(SpiderWebConfig cg, Map<String, ConnectList> c,
				AtomicNumber ms, AtomicNumber cpu, AtomicNumber mNo, AtomicNumber64 mTime,
				long etm, int pt, InetAddress bindAddr)
			throws IOException {
			this(TYPE_RECEIVE, cg, c, ms, cpu, mNo, mTime, etm, -1L, pt, bindAddr);
		}
		
		// コネクションスレッドを作成.
		public ExecuteThread(SpiderWebConfig cg, Map<String, ConnectList> c,
			AtomicNumber ms, AtomicNumber cpu, AtomicNumber mNo, AtomicNumber64 mTime,
			long etm, long stm, int pt, InetAddress bindAddr)
			throws IOException {
			this(TYPE_CONNECT, cg, c, ms, cpu, mNo, mTime, etm, stm, pt, bindAddr);
		}
		
		// 実行スレッド.
		public ExecuteThread(int t, SpiderWebConfig cg, Map<String, ConnectList> c,
			AtomicNumber ms, AtomicNumber cpu, AtomicNumber mNo, AtomicNumber64 mTime,
			long etm, long stm, int pt, InetAddress bindAddr)
			throws IOException {
			SendBuffer b = null;
			ExistenceIO e = new ExistenceIO();
			if(pt <= 0) {
				pt = SpiderWebConstants.PORT;
			}
			switch(t) {
			case TYPE_RECEIVE:
				b = new SendBuffer(SendBuffer.TYPE_SEND);
				e.bind(pt, bindAddr);
				break;
			case TYPE_CONNECT:
				b = new SendBuffer(SendBuffer.TYPE_ALL);
				e.setPort(pt, bindAddr);
				break;
			}
			if(etm <= 0L) {
				etm = SpiderWebConstants.EXPIRE_CONNECT_TIME;
			}
			if(stm <= 0L) {
				stm = SpiderWebConstants.SYNC_TIME;
			}
			type = t;
			config = cg;
			existenceIO = e;
			connectNodeGroup = c;
			machineStatus = ms;
			machineCpuLoad = cpu;
			machineNo = mNo;
			machineNoTime = mTime;
			sendBuf = b;
			expireTime = etm;
			syncTime = stm;
		}
		
		private final SpiderWebElement machineElement() {
			return new SpiderWebElement(machineStatus.get(), machineCpuLoad.get(), machineNo.get(), machineNoTime.get());
		}
		
		public final void startThread() {
			this.startFlag = true;
			this.stopFlag = false;
			this.setDaemon(true);
			this.start();
		}
		
		public final void stopThread() {
			this.stopFlag = true;
		}
		
		public final boolean isStartThread() {
			return this.startFlag;
		}
		
		public final boolean isStopThread() {
			return this.stopFlag;
		}
		
		public final int getType() {
			return this.type;
		}
		
		public final void setExistenceReceiveCall(ExistenceReceiveCall call) {
			existenceIO.setExistenceReceiveCall(call);
		}
		
		public final void run() {
			try {
				while(!stopFlag) {
					try {
						while(!stopFlag) {
							switch(type) {
							case TYPE_RECEIVE:
								executeReceive();
								break;
							case TYPE_CONNECT:
								executeConnect();
								break;
							case TYPE_SYNC:
								executeSync();
								break;
							}
						}
					} catch(Throwable t) {
						if(t instanceof ThreadDeath) {
							throw (ThreadDeath)t;
						}
						if(SpiderWebConstants.DEBUG_FLAG) {
							t.printStackTrace();
							System.out.println();
						}
					}
				}
			} finally {
				this.startFlag = false;
			}
		}
		
		// 受信監視.
		private final void executeReceive() throws IOException {
			int cpu;
			final InetAddress[] addr = new InetAddress[1];
			final int[] port = new int[1];
			final int[] type = new int[1];
			final String[] nodeGroupName = new String[1];
			final SpiderWebElement em = new SpiderWebElement();
			final Object[] data = new Object[1];
			while(!stopFlag) {
				// CPU平均を取得.
				cpu = OsCpuLoad.get();
				machineCpuLoad.set((machineCpuLoad.get() + cpu) >> 1);
				
				// 受信処理.
				if(!existenceIO.receive(addr, port, type, nodeGroupName, em, data)) {
					continue;
				}
				switch(type[0]) {
				case ExistenceIO.TYPE_CONNECT:
					executeReceiveConnect(addr[0], em, (String[])data[0]);
					break;
				case ExistenceIO.TYPE_IPLIST:
					executeReceiveIpList(addr[0], nodeGroupName[0], em, (int[])data[0]);
					break;
				case ExistenceIO.TYPE_MACHINE_LIST:
					executeReceiveMachineList(addr[0], nodeGroupName[0], em, (String[])data[0]);
					break;
				case ExistenceIO.TYPE_CHECKSUM:
					executeReceiveChecksum(addr[0], nodeGroupName[0], em, (Long)data[0]);
					break;
				case ExistenceIO.TYPE_SUCCESS:
					executeReceiveSuccess(addr[0], nodeGroupName[0], em);
					break;
				}
			}
		}
		
		// コネクション処理.
		// 1度コネクション処理が終わった場合は、同期処理に移行.
		private final void executeConnect() throws IOException {
			String nodeGroupName, addr;
			NodeGroupList nodeGroupList;
			Entry<String, NodeGroupList> e;
			final Map<String, NodeGroupList> nodeGroupMap = loadNodeGroupMap(config);
			final Iterator<Entry<String, NodeGroupList>> it = nodeGroupMap.entrySet().iterator();
			while(!stopFlag && it.hasNext()) {
				e = it.next();
				nodeGroupName = e.getKey();
				nodeGroupList = e.getValue();
				nodeGroupList.reset();
				existenceIO.connectData(sendBuf, machineElement(), nodeGroupName);
				while(!stopFlag && nodeGroupList.hasNext()) {
					addr = nodeGroupList.next();
					// コネクション処理だけは、自マシンにも送信する.
					existenceIO.connect(sendBuf, addr);
					// タイミングを入れる.
					sleepTime(5L);
				}
			}
			// コネクション処理が全部終わった場合は、同期処理に変更する.
			type = TYPE_SYNC;
			nextTime = System.currentTimeMillis() + syncTime;
		}
		
		// 同期処理.
		private final void executeSync() throws IOException {
			// 再実行時間に達してない場合は処理しない.
			if(System.currentTimeMillis() <= nextTime) {
				// configが更新されている場合は、connect処理を実行.
				if(config.isUpdate()) {
					type = TYPE_CONNECT;
				} else {
					sleepTime(TIMEOUT);
				}
				return;
			}
			// 次の実行時間をセット.
			nextTime = System.currentTimeMillis() + syncTime;
			
			// connectNodeGroupのchecksumを送信.
			int i, len;
			String nodeGroupName;
			long checksum;
			ConnectList conn;
			String[] addrs;
			Entry<String, ConnectList> e;
			Iterator<Entry<String, ConnectList>> it = connectNodeGroup.entrySet().iterator();
			while(!stopFlag && it.hasNext()) {
				e = it.next();
				nodeGroupName = e.getKey();
				conn = e.getValue();
				conn.removeExpire();
				addrs = conn.getConnectAddress();
				len = addrs.length;
				checksum = conn.getConnectChecksum();
				existenceIO.sendChecksumData(sendBuf, nodeGroupName, machineElement(), checksum);
				for(i = 0; i < len; i ++) {
					// 自マシンには送信しない.
					if(!ThisMachineAddress.eq(addrs[i])) {
						existenceIO.send(sendBuf, addrs[i]);
						// タイミングを入れる.
						sleepTime(5L);
					} else {
						// 自マシンの場合は、時間を更新.
						updateNodeGroupAddress(nodeGroupName, InetAddress.getByName(addrs[i]),machineElement());
					}
				}
			}
			
			// configが更新されている場合は、connect処理を実行.
			if(config.isUpdate()) {
				type = TYPE_CONNECT;
			}
		}
		
		// 他のマシンからのコネクション情報を受信.
		private final void executeReceiveConnect(InetAddress addr, SpiderWebElement em, String[] nodeGroupNames)
			throws IOException {
			// 現在のnodeGroupNamesのコネクションリストを取得して、
			// 現在接続中の「IPリスト」や「マシン名」を返却.
			final int len = nodeGroupNames.length;
			for(int i = 0; !stopFlag && i < len; i ++) {
				// ノードグループアドレスの更新.
				updateNodeGroupAddress(nodeGroupNames[i], addr, em);
				// 現在保持しているノードグループの接続管理一覧を送信する.
				if(!sendConnectList(addr, nodeGroupNames[i])) {
					// 接続管理一覧の件数が0件の場合は、success送信する.
					// ただし自マシンには送信しない.
					if(!ThisMachineAddress.eq(addr)) {
						existenceIO.sendSuccess(sendBuf, nodeGroupNames[i], machineElement())
							.send(sendBuf, addr);
					}
				}
			}
		}
		
		// IPリストを受信.
		private final void executeReceiveIpList(InetAddress addr, String nodeGroupName, SpiderWebElement em, int[] data)
			throws IOException {
			ConnectList conn = connectNodeGroup.get(nodeGroupName);
			if(conn == null) {
				conn = new ConnectList(nodeGroupName, expireTime);
				connectNodeGroup.put(nodeGroupName, conn);
			} else {
				// 期限の過ぎた接続情報を削除.
				conn.removeExpire();
			}
			// ローカルIPの場合は、IPアドレスで保持.
			// グローバルIPの場合は、マシン名で保持.
			String s;
			final int len = data.length;
			for(int i = 0; !stopFlag && i < len; i ++) {
				if(ConvIp4.isLocalIp(s = ConvIp4.ipToString(data[i]))) {
					conn.putToNoUpdate(s, em);
				} else {
					conn.putToNoUpdate(InetAddress.getByName(s).getHostName(), em);
				}
			}
			// 送信先の情報が存在しない場合は、その接続先の情報も登録.
			if(!conn.isInetAddressByUpdate(addr, em)) {
				if(ConvIp4.isLocalIp(addr.getHostAddress())) {
					conn.put(addr.getHostAddress(), em);
				} else {
					conn.put(addr.getHostName(), em);
				}
			}
		}
		
		// マシン名リストを受信.
		private void executeReceiveMachineList(InetAddress addr, String nodeGroupName, SpiderWebElement em, String[] data)
			throws IOException {
			ConnectList conn = connectNodeGroup.get(nodeGroupName);
			if(conn == null) {
				conn = new ConnectList(nodeGroupName, expireTime);
				connectNodeGroup.put(nodeGroupName, conn);
			} else {
				// 期限の過ぎた接続情報を削除.
				conn.removeExpire();
			}
			// ローカルIPの場合は、IPアドレスで保持.
			// グローバルIPの場合は、マシン名で保持.
			InetAddress ia;
			final int len = data.length;
			for(int i = 0; !stopFlag && i < len; i ++) {
				ia = InetAddress.getByName(data[i]);
				if(ConvIp4.isLocalIp(ia.getHostAddress())) {
					conn.putToNoUpdate(ia.getHostAddress(), em);
				} else {
					conn.putToNoUpdate(data[i], em);
				}
			}
			// 送信先の情報が存在しない場合は、その接続先の情報も登録.
			if(!conn.isInetAddressByUpdate(addr, em)) {
				if(ConvIp4.isLocalIp(addr.getHostAddress())) {
					conn.put(addr.getHostAddress(), em);
				} else {
					conn.put(addr.getHostName(), em);
				}
			}
		}
		
		// ノードの接続先同期チェックサムをチェック.
		// 一致しない場合は、接続元の接続情報群を問い合わせする.
		private void executeReceiveChecksum(InetAddress addr, String nodeGroupName, SpiderWebElement em, long data)
			throws IOException {
			// ノードグループアドレスの更新.
			updateNodeGroupAddress(nodeGroupName, addr, em);
			// コネクション情報が存在しない場合は、接続元の接続情報群を問合せ.
			ConnectList conn = connectNodeGroup.get(nodeGroupName);
			if(conn == null) {
				// 自マシンには送信しない.
				if(!ThisMachineAddress.eq(addr)) {
					// 受信元に、コネクションデータの送信.
					existenceIO.connectData(sendBuf, machineElement(), nodeGroupName)
						.connect(sendBuf, addr);
				}
				return;
			} else {
				// 期限の過ぎた接続情報を削除.
				conn.removeExpire();
			}
			// チェックサムが一致しない場合は、ノードグループの接続管理一覧を送信.
			if(conn.getConnectChecksum() != data) {
				// ノードグループの接続管理一覧を送信.
				if(!sendConnectList(addr, nodeGroupName)) {
					// 送るデータが存在しない場合は、success送信処理する.
					// ただし自マシンには送信しない.
					if(!ThisMachineAddress.eq(addr)) {
						existenceIO.sendSuccess(sendBuf, nodeGroupName, machineElement())
							.send(sendBuf, addr);
					}
				}
			// チェックサムが一致する場合は、successを返信.
			// 自マシンには送信しない.
			} else if(!ThisMachineAddress.eq(addr)) {
				existenceIO.sendSuccess(sendBuf, nodeGroupName, machineElement())
					.send(sendBuf, addr);
			}
		}
		
		// 正常を示す情報を受信.
		private void executeReceiveSuccess(InetAddress addr, String nodeGroupName, SpiderWebElement em) throws IOException {
			updateNodeGroupAddress(nodeGroupName, addr, em);
		}
		
		// 指定Nodeの現在の接続情報を送信.
		private final boolean sendConnectList(InetAddress addr, String nodeGroupName)
			throws IOException {
			// この処理は自マシンには送信しない.
			if(ThisMachineAddress.eq(addr)) {
				return true;
			}
			final ConnectList conn = connectNodeGroup.get(nodeGroupName);
			if(conn == null || conn.isEmpty()) {
				// 対象のノードグループに対する接続管理一覧が存在しない場合は
				// falseを返却する.
				return false;
			}
			List<Integer> ipList = null;
			List<String> machineList = null;
			final String[] connAddrs = conn.getConnectAddress();
			final int len = connAddrs.length;
			// このマシンが保持する、指定Nodeグループの接続情報群の送信準備をする.
			for(int i = 0; !stopFlag && i < len; i ++) {
				if(ConvIp4.isIp(connAddrs[i])) {
					if(ipList == null) {
						ipList = new ArrayList<Integer>();
					}
					ipList.add(ConvIp4.ipToInt(connAddrs[i]));
				} else {
					if(machineList == null) {
						machineList = new ArrayList<String>();
					}
					machineList.add(connAddrs[i]);
				}
			}
			// このマシンが保持する、指定NodeグループのローカルIPアドレス群の送信.
			if(ipList != null) {
				existenceIO.sendIPListData(sendBuf, nodeGroupName, machineElement(), ipList)
					.send(sendBuf, addr);
			}
			// このマシンが保持する、指定Nodeのマシン名群の送信.
			if(machineList != null) {
				existenceIO.sendMachineListData(sendBuf, nodeGroupName, machineElement(), machineList)
					.send(sendBuf, addr);
			}
			return true;
		}
		
		// 指定ノードグループのアドレスのコネクトリストを更新.
		private void updateNodeGroupAddress(String nodeGroupName, InetAddress addr, SpiderWebElement em)
			throws IOException {
			ConnectList conn = connectNodeGroup.get(nodeGroupName);
			if(conn == null) {
				conn = new ConnectList(nodeGroupName, expireTime);
				connectNodeGroup.put(nodeGroupName, conn);
			} else {
				// 期限の過ぎた接続情報を削除.
				conn.removeExpire();
			}
			// 送信先の情報が存在しない場合は、その接続先の情報も登録.
			if(!conn.isInetAddressByUpdate(addr, em)) {
				if(ConvIp4.isLocalIp(addr.getHostAddress())) {
					conn.put(addr.getHostAddress(), em);
				} else {
					conn.put(addr.getHostName(), em);
				}
			}
		}
		
		//  スリープ実行.
		private static final void sleepTime(long time) {
			try { Thread.sleep(time); } catch(Exception e) {}
		}
		
		// コンフィグ情報を読み込んで、ノードグループリストを作成.
		private static final Map<String, NodeGroupList> loadNodeGroupMap(SpiderWebConfig cg) {
			Map<String, NodeGroupList> ret = new HashMap<String, NodeGroupList>();
			final String[] list = cg.getNodeGroupNames();
			final int len = list.length;
			for(int i = 0; i < len; i ++) {
				ret.put(list[i], new NodeGroupList(list[i], cg.getNodeGroup(list[i])));
			}
			return ret;
		}
	}
	
	// 接続リスト.
	private static class SpiderWebListImpl implements SpiderWebList {
		private final ConnectList list;
		private final String nodeGroupName;
		SpiderWebListImpl(ConnectList list) {
			this.list = list;
			this.nodeGroupName = null;
		}
		SpiderWebListImpl(String nodeGroupName) {
			this.nodeGroupName = nodeGroupName;
			this.list = null;
		}
		@Override
		public String[] getConnectAddress() throws IOException {
			if(list == null) {
				return new String[0];
			}
			return list.getConnectAddress();
		}

		@Override
		public long getConnectChecksum() throws IOException {
			if(list == null) {
				return 0;
			}
			return list.getConnectChecksum();
		}
		
		@Override
		public Long getUpdateTime(String addr) {
			if(list == null) {
				return null;
			}
			return list.getUpdateTime(addr);
		}
		
		@Override
		public Integer getStatus(String addr) {
			if(list == null) {
				return null;
			}
			return list.getStatus(addr);
		}
		
		@Override
		public Integer getCpuLoad(String addr) {
			if(list == null) {
				return null;
			}
			return list.getCpuLoad(addr);
		}
		
		@Override
		public ConnectElement getElement(String addr) {
			if(list == null) {
				return null;
			}
			return list.getElement(addr);
		}
		
		@Override
		public String getNodeGroupName() {
			if(list == null) {
				return nodeGroupName;
			}
			return list.getNodeGroupName();
		}
		
		@Override
		public boolean isEmpty() {
			if(list == null) {
				return true;
			}
			return list.isEmpty();
		}
		
		@Override
		public String get(int no) throws IOException {
			if(list == null) {
				return null;
			}
			return list.get(no);
		}
		
		@Override
		public int size() throws IOException {
			if(list == null) {
				return 0;
			}
			return list.size();
		}
	}
	
	// 文字列からlong変換.
	private static final long parseLong(String s) {
		try {
			return Long.parseLong(s);
		} catch(Exception e) {
			return -1L;
		}
	}
	
	// 文字列からint変換.
	private static final int parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch(Exception e) {
			return -1;
		}
	}
	
	/**
	 * コンストラクタ.
	 * @throws IOException
	 */
	public SpiderWeb() throws IOException {
		this(SpiderWebConstants.CONF_FOLDER + SpiderWebConstants.CONF_NAME, null);
	}
	
	/**
	 * コンストラクタ.
	 * @param opt
	 * @throws IOException
	 */
	public SpiderWeb(Map<String, Object> opt) throws IOException {
		this(new SpiderWebConfig(SpiderWebConstants.CONF_FOLDER + SpiderWebConstants.CONF_NAME), opt);
	}
	
	/**
	 * コンストラクタ.
	 * @param configName
	 * @throws IOException
	 */
	public SpiderWeb(String configName) throws IOException {
		this(new SpiderWebConfig(configName), null);
	}
	
	/**
	 * コンストラクタ.
	 * @param config
	 * @throws IOException
	 */
	public SpiderWeb(SpiderWebConfig config) throws IOException {
		this(config, null);
	}
	
	/**
	 * コンストラクタ.
	 * @param configName
	 * @param opt
	 * @throws IOException
	 */
	public SpiderWeb(String configName, Map<String, Object> opt) throws IOException {
		this(new SpiderWebConfig(configName), opt);
	}
	
	/**
	 * コンストラクタ.
	 * @param config
	 * @param opt
	 * @throws IOException
	 */
	public SpiderWeb(SpiderWebConfig config, Map<String, Object> opt) throws IOException {
		if(opt == null) {
			opt = new HashMap<String, Object>();
		}
		final long expire = parseLong(""+opt.get("expire")); // 一定時間接続されていない場合の削除時間(ミリ秒単位).
		final long sync = parseLong(""+opt.get("sync")); // 全ノードの同期を行う時間(ミリ秒単位).
		final int port = parseInt(""+opt.get("port")); // バインドポート.
		
		// バインドアドレス.
		final String addr = opt.get("addr") == null ? null : "" + opt.get("addr");
		
		// カスタム受信実行処理.
		final ExistenceReceiveCall call = opt.get("call") == null ? null : (ExistenceReceiveCall)opt.get("call");
		
		// 受信スレッド.
		final ExecuteThread r = new ExecuteThread(config, connectNodeGroup,
				machineStatus, machineCpuLoad, machineNo, machineNoTime,
				expire, port, addr == null ? null : InetAddress.getByName(addr));
		
		// コネクションスレッド.
		final ExecuteThread c = new ExecuteThread(config, connectNodeGroup,
				machineStatus, machineCpuLoad, machineNo, machineNoTime,
				expire, sync, port, addr == null ? null : InetAddress.getByName(addr));
		
		// 受信スレッドにカスタム受信処理をセット.
		// カスタム受信が存在する場合は、カスタム送信も有効にする.
		r.setExistenceReceiveCall(call);
		if(call != null) {
			customSend = new ExistenceIO();
			customSend.setPort(port, addr == null ? null : InetAddress.getByName(addr));
			customSendBuffer = new SendBuffer(SendBuffer.TYPE_SEND);
		}
		
		this.recvThread = r;
		this.connectThread = c;
		if(!recvThread.isStartThread() && !connectThread.isStartThread()) {
			recvThread.startThread();
			connectThread.startThread();
		}
	}
	
	/**
	 * クローズ処理.
	 */
	public void close() {
		recvThread.stopThread();
		connectThread.stopThread();
		connectNodeGroup.clear();
		customSend = null;
		customSendBuffer = null;
	}
	
	/**
	 * 指定Nodeグループ名の接続一覧を取得.
	 * @param nodeGroupName
	 * @return
	 */
	public SpiderWebList get(String nodeGroupName) {
		if(connectNodeGroup.containsKey(nodeGroupName)) {
			return new SpiderWebListImpl(connectNodeGroup.get(nodeGroupName));
		}
		return new SpiderWebListImpl(nodeGroupName);
	}
	
	/**
	 * 指定Nodeグループ名が存在するかチェック.
	 * @param nodeGroupName
	 * @return
	 */
	public boolean isNodeGroup(String nodeGroupName) {
		return connectNodeGroup.containsKey(nodeGroupName);
	}
	
	/**
	 * 現在のNodeグループ名のIteratorを取得.
	 * @return
	 */
	public Iterator<String> nodeGroups() {
		return connectNodeGroup.keySet().iterator();
	}
	
	/**
	 * 情報が空か取得.
	 * @return
	 */
	public boolean isEmpty() {
		return connectNodeGroup.isEmpty();
	}
	
	/**
	 * 送信タイプを取得.
	 * 
	 * 送信タイプが[SpiderWeb.TYPE_CONNECT]の場合は、コネクション処理中です.
	 * 送信タイプが[SpiderWeb.TYPE_SYNC]の場合は、接続先と担保する同期処理実行中です.
	 * 通常[SpiderWeb.TYPE_SYNC]に通信タイプが移行している場合は、互いの端末で接続情報を管理されています.
	 * @return
	 */
	public int getMode() {
		return connectThread.getType();
	}
	
	/**
	 * このマシンのステータスを設定.
	 * @param status
	 */
	public void setStatus(int status) {
		this.machineStatus.set(status);
	}
	
	/**
	 * このマシンのステータスを取得.
	 * @return
	 */
	public int getStatus() {
		return this.machineStatus.get();
	}
	
	/**
	 * 現在のCPU平均を取得.
	 * @return int cpu負荷率の平均が返却されます.
	 */
	public int getCpuLoad() {
		return this.machineCpuLoad.get();
	}
	
	/**
	 * このマシンのアドレスかチェック.
	 * @param addr 対象のアドレスを設定します.
	 * @return boolean [true]の場合は、このマシンのアドレスです.
	 */
	public static final boolean isThisAddress(InetAddress addr) {
		return ThisMachineAddress.eq(addr);
	}
	
	/**
	 * このマシンのアドレスかチェック.
	 * @param addr 対象のアドレスを設定します.
	 * @return boolean [true]の場合は、このマシンのアドレスです.
	 */
	public static final boolean isThisAddress(String addr) {
		return ThisMachineAddress.eq(addr);
	}
	
	/**
	 * カスタムデータ送信.
	 * @param type
	 * @param nodeGroup
	 * @param b
	 * @throws IOException
	 */
	public void customSend(int type, String nodeGroup, byte[] b) throws IOException {
		if(customSend == null) {
			throw new IOException("Custom sending is not supported.");
		}
		customSend.sendApps(customSendBuffer, (byte)type, nodeGroup, machineElement(), b);
	}
	
	/**
	 * カスタムデータ送信.
	 * @param type
	 * @param nodeGroup
	 * @param s
	 * @throws IOException
	 */
	public void customSend(int type, String nodeGroup, String s) throws IOException {
		if(customSend == null) {
			throw new IOException("Custom sending is not supported.");
		}
		customSend.sendApps(customSendBuffer, (byte)type, nodeGroup, machineElement(), s);
	}
}
