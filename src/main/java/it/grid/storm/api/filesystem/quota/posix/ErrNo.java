package it.grid.storm.api.filesystem.quota.posix;

/**
 * ErrNo interface maps some of the error codes contained into the standard C
 * library sys/errno.h
 * 
 * @author Enrico Vianello
 *
 */
public interface ErrNo {

	/* Error codes from errno.h */
	
	public static int EPERM = 1;
	public static int ENOENT = 2;
	public static int ESRCH = 3;
	public static int EIO = 5;
	public static int EFAULT = 14;
	public static int ENOTBLK = 15;
	public static int ENODEV = 19;
	public static int EINVAL = 22;
	public static int EMFILE = 23;
	public static int ENOSYS = 78;
	
}