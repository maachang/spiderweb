package spiderweb.net;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * UDPによる、spiderweb生存I/Oのカスタムデータ受信処理.
 */
public interface ExistenceReceiveCall {
	/**
	 * 受信データ解析処理.
	 * @param type
	 * @param packet
	 * @param bin
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 */
	public Object get(int type, DatagramPacket packet, byte[] bin, int off, int len)
		throws IOException;
}
