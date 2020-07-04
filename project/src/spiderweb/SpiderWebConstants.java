package spiderweb;

/**
 * spiderweb 定義.
 */
public class SpiderWebConstants {
	/** デバッグモード. **/
	public static boolean DEBUG_FLAG = false;
	
	/** 基本コンフィグファイル名. **/
	public static final String CONF_NAME = "spiderweb.conf";
	
	/** 基本コンフィグフォルダ名. **/
	public static final String CONF_FOLDER = "./conf/";
	
	/** 基本受信ポート(UDP). **/
	public static final int PORT = 4321;
	
	/** 接続情報保持時間(1.5分). **/
	public static final long EXPIRE_CONNECT_TIME = 3L * 30L * 1000L;
	
	/** 同期間隔(30秒). **/
	public static final long SYNC_TIME = 30L * 1000L;

	/** デフォルトのマシンNo最大値 **/
	public static final int DEF_MACHINE_NO = 65535;
}
