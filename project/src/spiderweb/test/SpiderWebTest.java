package spiderweb.test;

import java.util.Iterator;

import spiderweb.SpiderWeb;
import spiderweb.SpiderWebConstants;
import spiderweb.SpiderWebList;

/**
 * spiderwebのテスト実装.
 */
public class SpiderWebTest {
	private static final long NEXT_TIME = 5000L;
	public static final void main(String[] args) throws Exception {
		SpiderWebConstants.DEBUG_FLAG = true;
		
		long nextTime = -1L;
		SpiderWeb spiderweb = new SpiderWeb();
		
		while(true) {
			if(System.currentTimeMillis() < nextTime) {
				Thread.sleep(1000);
				continue;
			}
			long nowTime = System.currentTimeMillis();
			nextTime = nowTime + NEXT_TIME;
			
			if(spiderweb.isEmpty()) {
				System.out.println("");
				continue;
			}
			System.out.println("");
			System.out.println("******************************");
			System.out.println("thisMachineCpu:" + spiderweb.getCpuLoad() + "%");
			Iterator<String> it = spiderweb.nodeGroups();
			while(it.hasNext()) {
				String node = it.next();
				System.out.println("-----------------------------");
				System.out.println("nodeGroup: " + node);
				SpiderWebList list = spiderweb.get(node);
				System.out.println("crc64: " + list.getConnectChecksum());
				String[] conns = list.getConnectAddress();
				int len = conns.length;
				for(int i = 0; i < len; i ++) {
					Long time = list.getUpdateTime(conns[i]);
					int cpuLoad = list.getCpuLoad(conns[i]);
					int status = list.getStatus(conns[i]);
					if(time != null) {
						System.out.println(" [" + conns[i] + "]: Updated:" + ((nowTime - time)/1000L) + " second cpu:" + cpuLoad + "% status:" + status);
					}
				}
			}
			System.out.println("******************************");
			
		}
	}
}
