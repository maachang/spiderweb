package spiderweb;

import spiderweb.utils.BinaryEd;

/**
 * spiderweb要素.
 */
public class SpiderWebElement {
	// 現在のステータス情報.
	private int status;
	// この情報の現在のCPU負荷情報.
	private int cpuLoad;
	// この情報のマシンNo.
	private int machineNo;
	// この情報のマシンNoが確定した時の時間.
	private long machineNoTime;
	
	/**
	 * コンストラクタ.
	 */
	public SpiderWebElement() {
		clear();
	}
	
	/**
	 * コンストラクタ.
	 * @param status ステータス情報.
	 * @param cpuLoad CPU負荷情報.
	 * @param machineNo マシンNo.
	 * @param machineNoTime マシンNoが確定した時の時間.
	 */
	public SpiderWebElement(int status, int cpuLoad, int machineNo, long machineNoTime) {
		this.status = status;
		this.cpuLoad = cpuLoad;
		this.machineNo = machineNo;
		this.machineNoTime = machineNoTime;
	}
	
	/**
	 * 情報クリア.
	 * @return
	 */
	public SpiderWebElement clear() {
		this.status = -1;
		this.cpuLoad = -1;
		this.machineNo = -1;
		this.machineNoTime = -1L;
		return this;
	}
	
	/**
	 * 情報設定.
	 * @param status ステータス情報.
	 * @param cpuLoad CPU負荷情報.
	 * @param machineNo マシンNo.
	 * @param machineNoTime マシンNoが確定した時の時間.
	 * @return
	 */
	public SpiderWebElement set(int status, int cpuLoad, int machineNo, long machineNoTime) {
		this.status = status;
		this.cpuLoad = cpuLoad;
		this.machineNo = machineNo;
		this.machineNoTime = machineNoTime;
		return this;
	}
	
	/**
	 * ステータス情報を取得.
	 * @return
	 */
	public int getStatus() {
		return status;
	}
	
	/**
	 * ステータス情報を設定.
	 * @param status
	 * @return
	 */
	public SpiderWebElement setStatus(int status) {
		this.status = status;
		return this;
	}
	
	/**
	 * CPU負荷情報を取得.
	 * @return
	 */
	public int getCpuLoad() {
		return cpuLoad;
	}
	
	/**
	 * CPU負荷情報を設定.
	 * @param cpuLoad
	 * @retrun
	 */
	public SpiderWebElement setCpuLoad(int cpuLoad) {
		this.cpuLoad = cpuLoad;
		return this;
	}
	
	/**
	 * マシンNoを取得.
	 * @return
	 */
	public int getMachineNo() {
		return machineNo;
	}
	
	/**
	 * マシンNoを設定.
	 * @param machineNo
	 * @return
	 */
	public SpiderWebElement setMachineNo(int machineNo) {
		this.machineNo = machineNo;
		return this;
	}
	
	/**
	 * マシンNo確定時間を取得.
	 * @return
	 */
	public long getMachineNoTime() {
		return machineNoTime;
	}
	
	/**
	 * マシンNo確定時間を設定.
	 * @param machineNoTime
	 * @return
	 */
	public SpiderWebElement setMachineNoTime(long machineNoTime) {
		this.machineNoTime = machineNoTime;
		return this;
	}
	
	/**
	 * SpiderWebElementをバイナリに変換.
	 * @param out 出力先のバイナリを設定します.
	 * @param off オフセット値を設定します.
	 * @param e SpiderWebElementを設定します.
	 * @return int 書き込まれたバイナリ長が返却されます.
	 */
	public static final int encodeBinary(byte[] out, int off, SpiderWebElement e) {
		int o = off;
		o = BinaryEd.setInt(out, o, e.status);
		o = BinaryEd.setByte(out, o, e.cpuLoad);
		o = BinaryEd.setInt(out, o, e.machineNo);
		o = BinaryEd.setLong(out, o, e.machineNoTime);
		return o - off;
	}
	
	/**
	 * バイナリからSpiderWebElementをデコード.
	 * @param out デコード先のSpiderWebElementを設定します.
	 * @param bin 対象のバイナリを設定します.
	 * @param off オフセット値を設定します.
	 * @return int 読み込まれたバイナリ長が返却されます.
	 */
	public static final int decodeBinary(SpiderWebElement out, byte[] bin, int[] off) {
		int o = off[0];
		out.status = BinaryEd.getInt(bin, off);
		out.cpuLoad = BinaryEd.getByte(bin, off);
		out.machineNo = BinaryEd.getInt(bin, off);
		out.machineNoTime = BinaryEd.getLong(bin, off);
		return off[0] - o;
	}
}
