package spiderweb.net;

import spiderweb.SpiderWebElement;

/**
 * コネクション要素.
 */
public class ConnectElement {
	// この情報の最終更新時間.
	private long time;
	// この情報の現在のステータス情報.
	private int status;
	// この情報の現在のCPU負荷情報.
	private int cpuLoad;
	// この情報のマシンNo.
	private int machineNo;
	// この情報のマシンNoが確定した時の時間.
	private long machineNoTime;
	
	public ConnectElement(SpiderWebElement em) {
		set(em);
	}
	
	public ConnectElement update() {
		this.time = System.currentTimeMillis();
		return this;
	}
	
	public ConnectElement set(SpiderWebElement em) {
		return set(em.getStatus(), em.getCpuLoad(), em.getMachineNo(), em.getMachineNoTime());
	}
	
	public ConnectElement set(int status, int cpuLoad, int machineNo, long machineNoTime) {
		this.status = status;
		this.cpuLoad = cpuLoad;
		this.machineNo = machineNo;
		this.machineNoTime = machineNoTime;
		return this;
	}
	
	public ConnectElement setStatus(int status) {
		this.status = status;
		return this;
	}
	
	public int getStatus() {
		return this.status;
	}
	
	public ConnectElement setCpuLoad(int cpuLoad) {
		this.cpuLoad = cpuLoad;
		return this;
	}
	
	public int getCpuLoad() {
		return cpuLoad;
	}
	
	public ConnectElement setMachineNo(int machineNo, long machineNoTime) {
		this.machineNo = machineNo;
		this.machineNoTime = machineNoTime;
		return this;
	}
	
	public int getMachineNo() {
		return machineNo;
	}
	
	public long getMachineNoTime() {
		return machineNoTime;
	}
	
	public ConnectElement setTime(long time) {
		this.time = time;
		return this;
	}
	
	public long getTime() {
		return time;
	}
}
