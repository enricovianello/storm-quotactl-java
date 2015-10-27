package it.grid.storm.api.filesystem.quota.posix;

public class PosixQuotaException extends Exception {

	private static final long serialVersionUID = 1L;

	public PosixQuotaException(String message) {
		super(message);
	}
	
	public PosixQuotaException(String message, Throwable cause) {
		super(message, cause);
	}

}
