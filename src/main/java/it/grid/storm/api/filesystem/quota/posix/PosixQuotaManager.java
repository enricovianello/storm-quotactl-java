package it.grid.storm.api.filesystem.quota.posix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.LastErrorException;

import it.grid.storm.api.filesystem.quota.posix.CLibrary.T_dqblk;;

/**
 * 
 * PosixQuotaManager allows to access the quota information on a Posix
 * filesystem. Quotas allow you to control disk usage by user or by group.
 * Quotas prevent individual users and groups from using a larger portion of a
 * filesystem than they are permitted, or from filling it up altogether.
 * <p>
 * The quotactl() standard C library is called to manipulate disk quotas.
 * 
 * @author Enrico Vianello
 * 
 */
public class PosixQuotaManager {

	private static final Logger log = LoggerFactory.getLogger(PosixQuotaManager.class);

	/**
	 * The result of the QCMD(subcmd, type) macro defined into sys/quota.h,
	 * where type value is GRPQUOTA and subcmd value is Q_GETQUOTA.
	 * <p>
	 * 
	 * <pre>
	 * {@code
	 * #define SUBCMDMASK  0x00ff
	 * #define SUBCMDSHIFT 8
	 * #define QCMD(cmd, type)  (((cmd) << SUBCMDSHIFT) | ((type) & SUBCMDMASK))
	 * 
	 * #define Q_GETQUOTA 0x0300
	 * #define GRPQUOTA  1
	 * }
	 * </pre>
	 */
	private static int GETGROUPQUOTA_CMD = 0x80000701;

	/**
	 * In case quotactl exits with a non-zero value, each error code has a
	 * correspondent message, returned by this method.
	 * 
	 * @param errNo
	 *            The error code
	 * @return The error message related to the errno specified.
	 */
	private String getErrnoMsg(int errNo) {

		switch (errNo) {

		case ErrNo.EFAULT:
			return "addr or special is invalid.";
		case ErrNo.EINVAL:
			return "cmd or type is invalid.";
		case ErrNo.ENOENT:
			return "The file specified by special or addr does not exist.";
		case ErrNo.ENOSYS:
			return "The kernel has not been compiled with the CONFIG_QUOTA option.";
		case ErrNo.ENOTBLK:
			return "special is not a block device.";
		case ErrNo.EPERM:
			return "The caller lacked the required privilege (CAP_SYS_ADMIN) for the specified operation.";
		case ErrNo.ESRCH:
			return "No disk quota is found for the indicated user. Quotas have not been turned on for this filesystem.";
		case ErrNo.EIO:
			return "Cannot read or write the quota file.";
		case ErrNo.EMFILE:
			return "Too many open files: cannot open quota file.";
		case ErrNo.ENODEV:
			return "special cannot be found in the mount table.";
		default:
			return "Unrecognized error " + errNo + " !";
		}
	}

	/**
	 * Call quotactl() to retrieve disk quota limits and current usage for a
	 * group id.
	 * 
	 * @param blockDevice
	 *            The pathname of the (mounted) block special device for the
	 *            filesystem being manipulated
	 * @param gid
	 *            The group id
	 * @return PosixQuotaInfo The disk quota limits and current usage for the
	 *         specified group id.
	 * @throws PosixQuotaException
	 *             Exception representing the problem occurred retrieving the
	 *             quota information.
	 */
	public PosixQuotaInfo getGroupQuota(String blockDevice, int gid) throws PosixQuotaException {

		log.debug("PosixQuotaManager.getGroupQuota({},{})", blockDevice, gid);
		T_dqblk dablk = new T_dqblk();
		int res;
		try {

			log.debug("quotactl({},{},{}) ...", Integer.toHexString(GETGROUPQUOTA_CMD), blockDevice, gid);
			res = CLibrary.INSTANCE.quotactl(GETGROUPQUOTA_CMD, blockDevice, gid, dablk);
			log.debug("quotactl exited with {} and returned {}", res, dablk.toString());

		} catch (LastErrorException e) {

			int errNo = e.getErrorCode();
			String errMsg = getErrnoMsg(e.getErrorCode());
			String message = String.format("Unable to load quota information for device %s and gid %d: [%d] %s",
					blockDevice, gid, errNo, errMsg);
			throw new PosixQuotaException(message, e);
		}

		return new PosixQuotaInfo(dablk);
	}

}
