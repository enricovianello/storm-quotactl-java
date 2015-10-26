package it.grid.storm.api.filesystem.quota;

public class QuotaException extends Exception {

	private static final long serialVersionUID = 1L;

	public QuotaException(String message) {
		super(message);
	}
	
	public QuotaException(String message, Throwable cause) {
		super(message, cause);
	}

}
