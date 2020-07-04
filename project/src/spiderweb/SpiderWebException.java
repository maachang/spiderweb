package spiderweb;

/**
 * base例外.
 */
public class SpiderWebException extends RuntimeException {
	private static final long serialVersionUID = -3666102550773922341L;
	protected int status;

	public SpiderWebException(int status) {
		super();
		this.status = status;
	}

	public SpiderWebException(int status, String message) {
		super(message);
		this.status = status;
	}

	public SpiderWebException(int status, Throwable e) {
		super(e);
		this.status = status;
	}

	public SpiderWebException(int status, String message, Throwable e) {
		super(message, e);
		this.status = status;
	}

	public SpiderWebException() {
		this(500);
	}

	public SpiderWebException(String m) {
		this(500, m);
	}

	public SpiderWebException(Throwable e) {
		this(500, e);
	}

	public SpiderWebException(String m, Throwable e) {
		this(500, m, e);
	}

	public int getStatus() {
		return status;
	}
}
