package spiderweb.utils;

import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

/**
 * 現在のOSのCPU負荷率を取得.
 */
public class OsCpuLoad {
	private static final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	private static final AtomicNumber before = new AtomicNumber(0);
	/**
	 * 現在のOSのCPU負荷率を取得.
	 * @return double 最大のCPU負荷の場合は１００が返却されます.
	 */
	public static final int get() {
		try {
			double d = osBean.getSystemCpuLoad();
			if(d == Double.NaN) {
				return before.get();
			}
			before.set((int)(d * 100));
			return before.get();
		} catch(Exception e) {
			return before.get();
		}
	}
}
