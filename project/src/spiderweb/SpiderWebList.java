package spiderweb;

import java.io.IOException;

import spiderweb.net.ConnectElement;

/**
 * spiderweb用マシン接続リスト.
 */
public interface SpiderWebList {
	
	/**
	 * ノードグループ名を取得.
	 * @return
	 */
	public String getNodeGroupName();
	
	/**
	 * 指定アドレスの存在確認最終時間を取得.
	 * @param addr
	 * @return
	 */
	public Long getUpdateTime(String addr);
	
	/**
	 * 指定アドレスのステータス情報を取得.
	 * @param addr
	 * @return
	 */
	public Integer getStatus(String addr);
	
	/**
	 * 指定アドレスのCPU利用率を取得.
	 * @param addr
	 * @return
	 */
	public Integer getCpuLoad(String addr);
	
	/**
	 * 指定アドレスの要素を取得.
	 * @param addr
	 * @return
	 */
	public ConnectElement getElement(String addr);
	
	/**
	 * 現状のコネクションアドレス一覧を取得.
	 * @return
	 * @throws IOException
	 */
	public String[] getConnectAddress() throws IOException;
	
	/**
	 * 現状のコネクションアドレス一覧の項番を指定して取得.
	 * @param no
	 * @return
	 * @throws IOException
	 */
	public String get(int no) throws IOException;
	
	/**
	 * 現状のコネクションアドレス一覧数を取得.
	 * @return
	 * @throws IOException
	 */
	public int size() throws IOException;
	
	/**
	 * コネクションアドレスのチェックサムを取得.
	 * @return
	 * @throws IOException
	 */
	public long getConnectChecksum() throws IOException;
	
	/**
	 * 空かチェック.
	 * @return
	 */
	public boolean isEmpty();
}
