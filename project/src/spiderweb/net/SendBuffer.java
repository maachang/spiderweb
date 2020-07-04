package spiderweb.net;

/**
 * 送信用バッファ.
 */
public class SendBuffer {
	public static final int TYPE_CONNECT = 1; // コネクション用バッファのみ作成.
	public static final int TYPE_SEND = 2; // 送信データ用バッファのみ作成.
	public static final int TYPE_ALL = 3; // コネクション、送信データ用バッファ両方を作成.
	
	public int connectBufferLength = 0;
	public byte[] connectBuffer = null;
	public int sendBufferLength = 0;
	public byte[] sendBuffer = null;
	
	/**
	 * コンストラクタ.
	 * @param type
	 */
	public SendBuffer(int type) {
		if((type & TYPE_CONNECT) != 0) {
			connectBuffer = new byte[65535];
		}
		if((type & TYPE_SEND) != 0) {
			sendBuffer = new byte[65535];
		}
	}
}
