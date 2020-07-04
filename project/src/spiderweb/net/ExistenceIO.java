package spiderweb.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.List;

import spiderweb.SpiderWebConstants;
import spiderweb.SpiderWebElement;

/**
 * UDPによる、spiderweb生存I/O.
 * spiderwebの通信プロトコル関連の実装がまとめられています.
 */
public class ExistenceIO {
	private static final int TIMEOUT = 250;
	private static final byte[] ZERO_BIN = new byte[0];
	
	// spiderweb通信ヘッダ.
	private static final byte[] HEAD = new byte[] {
		(byte)'h', (byte)0x08, (byte)'h', (byte)0x0a, (byte)'k', (byte)0x0e
	};
	
	// spiderweb通信ヘッダとノード名長、データ長のオフセット値.
	private static final int OFFSET = HEAD.length + 8;
	
	public static final byte TYPE_BINARY = 0;			// バイナリ通信.
	public static final byte TYPE_STRING = 1;			// 文字通信.
	public static final byte TYPE_CONNECT = 10;			// [NodeGroup]コネクションパケット.
	public static final byte TYPE_IPLIST = 11;			// [NodeGroup]IPアドレス一覧.
	public static final byte TYPE_MACHINE_LIST = 12;	// [NodeGroup]マシン名一覧.
	public static final byte TYPE_CHECKSUM = 13;		// [NodeGroup]チェックサム値.
	public static final byte TYPE_SUCCESS = 20;			// [NodeGroup]正常返信.
	public static final byte TYPE_ERROR = 29;			// [NodeGroup]異常返信.
	
	public static final byte TYPE_APPS = 30;			// アプリ実行タイプ.
	
	private boolean notReuseAddress = false;
	private DatagramSocket receiveUdp = null;
	private byte[] recvBuffer = null;
	private int port = -1;
	private InetAddress bindAddr = null;
	private ExistenceReceiveCall call = null;
	
	/**
	 * コンストラクタ.
	 */
	public ExistenceIO() {
		this.receiveUdp = null;
		this.port = -1;
	}
	
	/**
	 * UDP受信用のバインド設定を行います.
	 * @param port
	 * @return
	 * @throws IOException
	 */
	public ExistenceIO bind(int port) throws IOException {
		return bind(port, (InetAddress)null);
	}
	
	/**
	 * UDP受信用のバインド設定を行います.
	 * @param port
	 * @param addr
	 * @return
	 * @throws IOException
	 */
	public ExistenceIO bind(int port, String addr) throws IOException {
		return bind(port, addr == null ? null : InetAddress.getByName(addr));
	}
	
	/**
	 * UDP受信用のバインド設定を行います.
	 * @param port
	 * @param addr
	 * @return
	 * @throws IOException
	 */
	public ExistenceIO bind(int port, InetAddress addr) throws IOException {
		_bind(port, addr);
		this.recvBuffer = new byte[65535];
		return this.setPort(port, addr);
	}
	
	// バインド処理.
	private final void _bind(int port, InetAddress addr) throws IOException {
		DatagramSocket udp = null;
		try {
			udp = new DatagramSocket(null);
			if(!notReuseAddress) {
				try {
					// このオプションで「無効な引数です」とエラーが出る場合もある.
					udp.setReuseAddress(true);
				} catch(Exception e) {
					notReuseAddress = true;
					udp.close();
					udp = null;
					udp = new DatagramSocket(null);
				}
			}
			udp.setSoTimeout(TIMEOUT);
			udp.setBroadcast(false);
			if(addr == null) {
				udp.bind(new InetSocketAddress("0.0.0.0", port));
			} else {
				udp.bind(new InetSocketAddress(addr, port));
			}
			this.receiveUdp = udp;
			udp = null;
		} finally {
			if(udp != null) {
				try {
					udp.close();
				} catch(Exception e) {}
			}
		}
	}
	
	/**
	 * UDP受信用のバインドクローズ.
	 */
	public void close() {
		if(receiveUdp != null) {
			receiveUdp.close();
			receiveUdp = null;
		}
		recvBuffer = null;
	}
	
	/**
	 * カスタム受信処理を設定.
	 * @param call
	 * @return
	 */
	public ExistenceIO setExistenceReceiveCall(ExistenceReceiveCall call) {
		this.call = call;
		return this;
	}
	
	/**
	 * カスタム受信処理を取得.
	 * @return
	 */
	public ExistenceReceiveCall getExistenceReceiveCall() {
		return this.call;
	}
	
	/**
	 * ポート番号を設定.
	 * (bindしない場合のデフォルトポート番号を設定します)
	 * @param port
	 * @param addr
	 * @return
	 */
	public ExistenceIO setPort(int port, InetAddress addr) {
		this.port = port;
		this.bindAddr = addr;
		return this;
	}
	
	/**
	 * ポート番号を取得.
	 * @return
	 */
	public int getPort() {
		return this.port;
	}
	
	/**
	 * バインドアドレスを取得.
	 * @return
	 */
	public InetAddress getBindAddress() {
		return bindAddr;
	}
	
	/**
	 * TYPE_CONNECTのデータ設定.
	 * @param sendBuf
	 * @param status
	 * @param cpuLoad
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public ExistenceIO connectData(SendBuffer sendBuf, SpiderWebElement em, String data)
		throws IOException {
		return _sendData(sendBuf, TYPE_CONNECT, "", em, data.getBytes("UTF8"));
	}
	
	/**
	 * TYPE_CONNECTのデータ設定.
	 * @param sendBuf
	 * @param status
	 * @param cpuLoad
	 * @param nodeGroupNames
	 * @return
	 * @throws IOException
	 */
	public ExistenceIO connectData(SendBuffer sendBuf, SpiderWebElement em, String[] nodeGroupNames)
		throws IOException {
		int len = nodeGroupNames.length;
		StringBuilder buf = new StringBuilder();
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				buf.append(",");
			}
			buf.append(nodeGroupNames[i]);
		}
		String data = buf.toString();
		buf = null;
		return connectData(sendBuf, em, data);
	}
	
	/**
	 * バイナリ送信データの設定.
	 * @param sendBuf
	 * @param nodeGroupName
	 * @param status
	 * @param cpuLoad
	 * @param b
	 * @return
	 * @throws IOException
	 */
	public ExistenceIO sendData(SendBuffer sendBuf, String nodeGroupName, SpiderWebElement em, byte[] b)
		throws IOException {
		return _sendData(sendBuf, TYPE_BINARY, nodeGroupName, em, b);
	}
	
	/**
	 * 文字列送信データの設定.
	 * @param sendBuf
	 * @param nodeGroupName
	 * @param status
	 * @param cpuLoad
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public ExistenceIO sendData(SendBuffer sendBuf, String nodeGroupName, SpiderWebElement em, String data)
		throws IOException {
		return _sendData(sendBuf, TYPE_STRING, nodeGroupName, em, data.getBytes("UTF8"));
	}
	
	/**
	 * IPリストデータの設定.
	 * @param sendBuf
	 * @param nodeGroupName
	 * @param status
	 * @param cpuLoad
	 * @param ipList
	 * @return
	 * @throws IOException
	 */
	public ExistenceIO sendIPListData(SendBuffer sendBuf, String nodeGroupName, SpiderWebElement em, List<Integer> ipList)
		throws IOException {
		int ip;
		int off;
		byte[] nodeGroupBin = nodeGroupName.getBytes("UTF8");
		int nodeLen = nodeGroupBin.length;
		int len = ipList.size();
		final byte[] sendBuffer = sendBuf.sendBuffer;
		System.arraycopy(HEAD, 0, sendBuffer, 0, HEAD.length);
		off = HEAD.length;
		sendBuffer[off++] = 0;
		sendBuffer[off++] = TYPE_IPLIST;
		sendBuffer[off++] = (byte)(nodeLen & 0x000000ff);
		sendBuffer[off++] = (byte)((nodeLen & 0x0000ff00) >> 8);
		System.arraycopy(nodeGroupBin, 0, sendBuffer, off, nodeLen);
		off += nodeLen;
		nodeGroupBin = null;
		final int emLen = SpiderWebElement.encodeBinary(sendBuffer, off, em);
		off += emLen;
		sendBuffer[off++] = (byte)(len & 0x000000ff);
		sendBuffer[off++] = (byte)((len & 0x0000ff00) >> 8);
		for(int i = 0; i < len; i ++) {
			ip = ipList.get(i);
			sendBuffer[off++] = (byte)(ip & 0x000000ff);
			sendBuffer[off++] = (byte)((ip & 0x0000ff00) >> 8);
			sendBuffer[off++] = (byte)((ip & 0x00ff0000) >> 16);
			sendBuffer[off++] = (byte)((ip & 0xff000000) >> 24);
		}
		sendBuf.sendBufferLength = off;
		sendBuffer[HEAD.length] = checkSendCode(sendBuffer, off);
		return this;
	}
	
	/**
	 * マシン名リストデータの設定.
	 * @param sendBuf
	 * @param nodeGroupName
	 * @param status
	 * @param cpuLoad
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public ExistenceIO sendMachineListData(SendBuffer sendBuf, String nodeGroupName, SpiderWebElement em, String data)
		throws IOException {
		return _sendData(sendBuf, TYPE_MACHINE_LIST, nodeGroupName, em, data.getBytes("UTF8"));
	}
	
	/**
	 * マシン名リストデータの設定.
	 * @param sendBuf
	 * @param nodeGroupName
	 * @param status
	 * @param cpuLoad
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public ExistenceIO sendMachineListData(SendBuffer sendBuf, String nodeGroupName, SpiderWebElement em, List<String> data)
		throws IOException {
		StringBuilder buf = new StringBuilder();
		int len = data.size();
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				buf.append(",");
			}
			buf.append(data.get(i));
		}
		String list = buf.toString();
		buf = null;
		return sendMachineListData(sendBuf, nodeGroupName, em, list);
	}
	
	/**
	 * チェックサムデータの設定.
	 * @param sendBuf
	 * @param nodeGroupName
	 * @param status
	 * @param cpuLoad
	 * @param code
	 * @return
	 * @throws IOException
	 */
	public ExistenceIO sendChecksumData(SendBuffer sendBuf, String nodeGroupName, SpiderWebElement em, long code)
		throws IOException {
		return _sendData(sendBuf, TYPE_CHECKSUM, nodeGroupName, em, new byte[] {
				(byte)(code & 0x00000000000000ffL),
				(byte)((code & 0x000000000000ff00L) >> 8L),
				(byte)((code & 0x0000000000ff0000L) >> 16L),
				(byte)((code & 0x00000000ff000000L) >> 24L),
				(byte)((code & 0x000000ff00000000L) >> 32L),
				(byte)((code & 0x0000ff0000000000L) >> 40L),
				(byte)((code & 0x00ff000000000000L) >> 48L),
				(byte)((code & 0xff00000000000000L) >> 56L)
		});
	}
	
	/**
	 * 正常データの設定.
	 * @param sendBuf
	 * @param nodeGroupName
	 * @param status
	 * @param cpuLoad
	 * @return
	 * @throws IOException
	 */
	public ExistenceIO sendSuccess(SendBuffer sendBuf, String nodeGroupName, SpiderWebElement em)
		throws IOException {
		return _sendData(sendBuf, TYPE_SUCCESS, nodeGroupName, em, ZERO_BIN);
	}
	
	/**
	 * 正常データの設定.
	 * @param sendBuf
	 * @param nodeGroupName
	 * @param status
	 * @param cpuLoad
	 * @param message
	 * @return
	 * @throws IOException
	 */
	public ExistenceIO sendSuccess(SendBuffer sendBuf, String nodeGroupName, SpiderWebElement em, String message)
		throws IOException {
		return _sendData(sendBuf, TYPE_SUCCESS, nodeGroupName, em, message.getBytes("UTF8"));
	}
	
	/**
	 * 異常データの設定.
	 * @param sendBuf
	 * @param nodeGroupName
	 * @param status
	 * @param cpuLoad
	 * @return
	 * @throws IOException
	 */
	public ExistenceIO sendError(SendBuffer sendBuf, String nodeGroupName, SpiderWebElement em)
		throws IOException {
		return _sendData(sendBuf, TYPE_ERROR, nodeGroupName, em, ZERO_BIN);
	}
	
	/**
	 * 異常データの設定.
	 * @param sendBuf
	 * @param nodeGroupName
	 * @param status
	 * @param cpuLoad
	 * @param message
	 * @return
	 * @throws IOException
	 */
	public ExistenceIO sendError(SendBuffer sendBuf, String nodeGroupName, SpiderWebElement em, String message)
		throws IOException {
		return _sendData(sendBuf, TYPE_ERROR, nodeGroupName, em, message.getBytes("UTF8"));
	}
	
	/**
	 * アプリ送信.
	 * @param sendBuf
	 * @param type
	 * @param nodeGroupName
	 * @param status
	 * @param cpuLoad
	 * @param b
	 * @return
	 * @throws IOException
	 */
	public ExistenceIO sendApps(SendBuffer sendBuf, byte type, String nodeGroupName, SpiderWebElement em, byte[] b)
		throws IOException {
		if(type <= TYPE_APPS) {
			throw new IOException("It is necessary to set the type more than the custom type ("+TYPE_APPS+").");
		}
		return _sendData(sendBuf, type, nodeGroupName, em, b);
	}
	
	/**
	 * アプリ送信.
	 * @param sendBuf
	 * @param type
	 * @param nodeGroupName
	 * @param status
	 * @param cpuLoad
	 * @param s
	 * @return
	 * @throws IOException
	 */
	public ExistenceIO sendApps(SendBuffer sendBuf, byte type, String nodeGroupName, SpiderWebElement em, String s)
		throws IOException {
		if(type <= TYPE_APPS) {
			throw new IOException("It is necessary to set the type more than the custom type ("+TYPE_APPS+").");
		}
		return _sendData(sendBuf, type, nodeGroupName, em, s.getBytes("UTF8"));
	}
	
	// 汎用送信パケットデータの作成.
	private ExistenceIO _sendData(SendBuffer sendBuf, byte type, String nodeGroupName, SpiderWebElement em, byte[] b)
		throws IOException {
		int off;
		int bLen = (b == null) ? 0 : b.length;
		byte[] nodeGroupBin = (nodeGroupName == null) ? ZERO_BIN : nodeGroupName.getBytes("UTF8");
		int nodeLen = nodeGroupBin.length;
		byte[] buf;
		if(type == TYPE_CONNECT && sendBuf.connectBuffer != null) {
			buf = sendBuf.connectBuffer;
		} else {
			buf = sendBuf.sendBuffer;
		}
		System.arraycopy(HEAD, 0, buf, 0, HEAD.length);
		off = HEAD.length;
		buf[off++] = 0;
		buf[off++] = type;
		buf[off++] = (byte)(nodeLen & 0x000000ff);
		buf[off++] = (byte)((nodeLen & 0x0000ff00) >> 8);
		System.arraycopy(nodeGroupBin, 0, buf, off, nodeLen);
		off += nodeLen;
		nodeGroupBin = null;
		final int emLen = SpiderWebElement.encodeBinary(buf, off, em);
		off += emLen;
		buf[off++] = (byte)(bLen & 0x000000ff);
		buf[off++] = (byte)((bLen & 0x0000ff00) >> 8);
		if(b != null) {
			System.arraycopy(b, 0, buf, off, bLen);
		}
		if(type == TYPE_CONNECT && sendBuf.connectBuffer != null) {
			sendBuf.connectBufferLength = off + bLen;
		} else {
			sendBuf.sendBufferLength = off + bLen;
		}
		buf[HEAD.length] = checkSendCode(buf, off + bLen);
		return this;
	}
	
	// チェックコードを生成.
	private static final byte checkSendCode(byte[] b, int len) {
		int ret = 99;
		for(int i = 0; i < len ; i++) {
			ret ^= b[i];
		}
		return (byte)ret;
	}
	
	/**
	 * コネクションデータの送信.
	 * @param sendBuf
	 * @param addr
	 * @throws IOException
	 */
	public void connect(SendBuffer sendBuf, Object addr) throws IOException {
		if(sendBuf.connectBuffer != null) {
			_send(addr, this.port, sendBuf.connectBuffer, sendBuf.connectBufferLength);
		} else {
			_send(addr, this.port, sendBuf.sendBuffer, sendBuf.sendBufferLength);
		}
	}
	
	/**
	 * コネクションデータの送信.
	 * @param sendBuf
	 * @param addr
	 * @param port
	 * @throws IOException
	 */
	public void connect(SendBuffer sendBuf, Object addr, int port) throws IOException {
		if(sendBuf.connectBuffer != null) {
			_send(addr, port, sendBuf.connectBuffer, sendBuf.connectBufferLength);
		} else {
			_send(addr, port, sendBuf.sendBuffer, sendBuf.sendBufferLength);
		}
	}
	
	/**
	 * 送信バッファデータの送信.
	 * @param sendBuf
	 * @param addr
	 * @throws IOException
	 */
	public void send(SendBuffer sendBuf, Object addr) throws IOException {
		_send(addr, this.port, sendBuf.sendBuffer, sendBuf.sendBufferLength);
	}
	
	/**
	 * 送信バッファデータの送信.
	 * @param sendBuf
	 * @param addr
	 * @param port
	 * @throws IOException
	 */
	public void send(SendBuffer sendBuf, Object addr, int port) throws IOException {
		_send(addr, port, sendBuf.sendBuffer, sendBuf.sendBufferLength);
	}
	
	// 汎用送信処理.
	private final void _send(Object addr, int port, byte[] b, int len)
		throws IOException {
		InetAddress inetAddr = (addr instanceof InetAddress) ? (InetAddress)addr : InetAddress.getByName(""+addr);
		DatagramSocket s = null;
		try {
			s = new DatagramSocket(null);
			if(!notReuseAddress) {
				try {
					// このオプションで「無効な引数です」とエラーが出る場合もある.
					s.setReuseAddress(true);
				} catch(Exception e) {
					notReuseAddress = true;
					s.close(); s = null;
					s = new DatagramSocket(null);
				}
			}
			s.setBroadcast(false);
			if(this.bindAddr == null) {
				s.bind(new InetSocketAddress("0.0.0.0", 0));
			} else {
				s.bind(new InetSocketAddress(this.bindAddr, 0));
			}
			s.send(new DatagramPacket(b, 0, len, inetAddr, port));
			s.close(); s = null;
		} finally {
			if(s != null) {
				try {
					s.close();
				} catch(Exception e) {}
			}
		}
	}
	
	/**
	 * 受信処理.
	 * @param recvAddr
	 * @param recvPort
	 * @param type
	 * @param nodeGroupName
	 * @param data
	 * @return
	 */
	public boolean receive(InetAddress[] recvAddr, int[] recvPort, int[] type,
		String[] nodeGroupName, SpiderWebElement em, Object[] data) {
		final byte[] b = recvBuffer;
		final DatagramPacket packet = new DatagramPacket(b, b.length);
		try {
			receiveUdp.receive(packet);
			if(packet.getLength() < OFFSET ||
				b[0] != HEAD[0] || b[1] != HEAD[1] || b[2] != HEAD[2] ||
				b[3] != HEAD[3] || b[4] != HEAD[4] || b[5] != HEAD[5]) {
				return false;
			}
			byte checkCode = b[6]; b[6] = 0;
			if(checkSendCode(b, packet.getLength()) != checkCode) {
				return false;
			}
			int t = b[7] & 0x000000ff;
			int groupLen = (b[8] & 0x000000ff) | ((b[9] & 0x000000ff) << 8);
			int off = 10;
			String groupName = new String(b, off, groupLen, "UTF8");
			off += groupLen;
			int[] eOff = new int[] {off};
			SpiderWebElement.decodeBinary(em, b, eOff);
			off = eOff[0];
			int len = (b[off] & 0x000000ff) | ((b[off+1] & 0x000000ff) << 8);
			off += 2;
			Object d = null;
			if(t > TYPE_APPS) {
				if(call == null) {
					return false;
				}
				d = call.get(t, packet, b, off, len);
			} else {
				switch(t) {
				case TYPE_BINARY: d = recvBinary(packet, b, off, len); break;
				case TYPE_STRING: d = recvString(packet, b, off, len); break;
				case TYPE_CONNECT : d = recvConnect(packet, b, off, len); break;
				case TYPE_IPLIST: d = recvIpList(packet, b, off, len); break;
				case TYPE_MACHINE_LIST: d = recvMachineList(packet, b, off, len); break;
				case TYPE_CHECKSUM: d = recvChecksum(packet, b, off, len); break;
				case TYPE_SUCCESS: d = recvString(packet, b, off, len); break;
				case TYPE_ERROR: d = recvString(packet, b, off, len); break;
				default : return false;
				}
			}
			recvAddr[0] = packet.getAddress();
			recvPort[0] = packet.getPort();
			type[0] = t;
			nodeGroupName[0] = groupName;
			data[0] = d;
			return true;
		} catch(SocketTimeoutException st) {
			// タイムアウトは無視.
		} catch(IOException io) {
			if(SpiderWebConstants.DEBUG_FLAG) {
				io.printStackTrace();
				System.out.println();
			}
		}
		return false;
	}
	
	// [TYPE_BINARY]の受信Bodyデータを取得.
	private static final Object recvBinary(DatagramPacket packet, byte[] bin, int off, int len)
		throws IOException {
		final byte[] ret = new byte[len];
		System.arraycopy(bin, off, ret, 0, len);
		return ret;
	}
	
	// [TYPE_STRING]の受信Bodyデータを取得.
	private static final Object recvString(DatagramPacket packet, byte[] bin, int off, int len)
		throws IOException {
		return new String(bin, off, len, "UTF8");
	}
	
	// [TYPE_CONNECT]の受信Bodyデータを取得.
	private static final Object recvConnect(DatagramPacket packet, byte[] bin, int off, int len)
		throws IOException {
		return new String(bin, off, len, "UTF8").split(",");
	}
	
	// [TYPE_IPLIST]の受信Bodyデータを取得.
	private static final Object recvIpList(DatagramPacket packet, byte[] bin, int off, int len)
		throws IOException {
		int[] ret = new int[len];
		for(int i = 0; i < len; i ++) {
			ret[i] = (bin[off++] & 0x000000ff) |
					((bin[off++] & 0x000000ff) << 8) |
					((bin[off++] & 0x000000ff) << 16) |
					((bin[off++] & 0x000000ff) << 24);
		}
		return ret;
	}
	
	// [TYPE_MACHINE_LIST]の受信Bodyデータを取得.
	private static final Object recvMachineList(DatagramPacket packet, byte[] bin, int off, int len)
		throws IOException {
		return new String(bin, off, len, "UTF8").split(",");
	}
	
	// [TYPE_CHECKSUM]の受信Bodyデータを取得.
	private static final Object recvChecksum(DatagramPacket packet, byte[] bin, int off, int len)
		throws IOException {
		if(len != 8) {
			throw new IOException("Not the checksum data length.");
		}
		return (bin[off] & 0x00000000000000ffL) |
				((bin[off+1] & 0x00000000000000ffL) << 8L) |
				((bin[off+2] & 0x00000000000000ffL) << 16L) |
				((bin[off+3] & 0x00000000000000ffL) << 24L) |
				((bin[off+4] & 0x00000000000000ffL) << 32L) |
				((bin[off+5] & 0x00000000000000ffL) << 40L) |
				((bin[off+6] & 0x00000000000000ffL) << 48L) |
				((bin[off+7] & 0x00000000000000ffL) << 56L);
	}
}
