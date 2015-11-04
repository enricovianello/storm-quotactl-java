package it.grid.storm.api.filesystem.quota.posix;

import com.sun.jna.LastErrorException;

/**
 * Exception raised in case of an error. In case of an error during quotactl(),
 * the cause object is an instance of {@link LastErrorException}
 * 
 * @author Enrico Vianello
 *
 */
public class PosixQuotaException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * PosixQuotaException constructor.
	 * 
	 * @param message
	 *            The error message
	 * @param cause
	 *            The cause of the error. In case of an error during quotactl(),
	 *            the cause object is an instance of {@link LastErrorException}
	 */
	public PosixQuotaException(String message, Throwable cause) {
		super(message, cause);
	}

}
