package spiderweb;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import spiderweb.utils.Base64;
import spiderweb.utils.Json;

/**
 * spiderweb-config.
 */
public class SpiderWebConfig {
/**
 * config サンプル.
 * 
 * {
 *   "white": [
 *     ["192.168.1.0", "192.168.1.255]
 *   ],
 *   "yellow": [
 *     ["172.16.1.100", "172.16.1.120"],
 *     ["172.16.1.200", "172.16.1.225"],
 *     ["172.16.1.254"],
 *     ["test3"]
 *   ],
 *   "green": [
 *     ["10.0.0.1", "10.0.0.100"],
 *     ["test1"],
 *     ["test2"]
 *   ]
 * }
 * 
 * [white]ノードグループのIPアドレスの範囲
 *  192.168.1.(0 - 255)
 * [yellow]ノードグループのIPアドレスの範囲と端末名.
 *   172.16.1.(100 - 120)
 *   172.16.1.(200 - 225)
 *   172.16.1.254
 *   [test3]
 * [green]ノードグループのIPアドレスの範囲と端末名
 *   10.0.0.(1 - 100)
 *   [test1] 
 *   [test2]
 */
	
	private Map<String, List<List<String>>> conf = null;
	private String fileName = null;
	private long fileTime = -1L;
	private String checksum = "";
	private String[] nodeGroupNames = null;
	
	/**
	 * コンストラクタ.
	 * @param name
	 */
	public SpiderWebConfig(String name) {
		_reload(name);
	}
	
	/**
	 * コンストラクタ.
	 * @param c
	 */
	public SpiderWebConfig(Map<String, List<List<String>>> c) {
		conf = c;
		nodeGroupNames = _getNames(c);
		fileName = null;
		fileTime = -1L;
	}
	
	// ファイル内容を文字列で取得.
	private static final String _readFile(String[] o, String name) {
		int len;
		byte[] buf = new byte[1024];
		InputStream in = null;
		try {
			final MessageDigest dg = MessageDigest.getInstance("SHA-1");
			in = new FileInputStream(name);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			while((len = in.read(buf)) != -1) {
				dg.update(buf, 0, len);
				out.write(buf, 0, len);
			}
			buf = null;
			in.close();
			in = null;
			out.close();
			o[0] = Base64.encode(dg.digest());
			return new String(out.toByteArray(), "UTF8");
		} catch(Exception e) {
			throw new SpiderWebException(e);
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch(Exception e) {}
			}
		}
	}
	
	// コンフィグ情報を再読み込み.
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void _reload(String name) {
		if(name == null) {
			return;
		}
		String[] out = new String[1];
		Map o = (Map)Json.decode(_readFile(out, name));
		long tm = new File(name).lastModified();
		
		// nodeNameを取得.
		String[] names = _getNames(o);
		
		conf = (Map)o;
		checksum = out[0];
		fileTime = tm;
		fileName = name;
		nodeGroupNames = names;
	}
	
	// ノード名一覧を取得.
	private final String[] _getNames(Map<String, List<List<String>>> o) {
		// nodeNameを取得.
		Iterator<String> it = o.keySet().iterator();
		String[] ret = new String[o.size()];
		int cnt = 0;
		while(it.hasNext()) {
			ret[cnt++] = it.next();
		}
		it = null;
		return ret;
	}
	
	/**
	 * コンフィグファイルの再読み込み.
	 */
	public void reload() {
		_reload(fileName);
	}
	
	/**
	 * コンフィグファイルが更新されているかチェック.
	 * @return
	 */
	public boolean isUpdate() {
		if(fileName == null) {
			return false;
		}
		return fileTime != new File(fileName).lastModified();
	}
	
	/**
	 * ノードグループ名群を取得.
	 * @return
	 */
	public String[] getNodeGroupNames() {
		return nodeGroupNames;
	}
	
	/**
	 * ノードグループ内容を取得.
	 * @param name
	 * @return
	 */
	public List<List<String>> getNodeGroup(String name) {
		return conf.get(name);
	}
	
	/**
	 * チェックサム情報を取得.
	 * @return
	 */
	public String getChecksum() {
		return checksum;
	}
	
	/**
	 * コンフィグファイル名を取得.
	 * @return
	 */
	public String fileName() {
		return fileName;
	}
}
