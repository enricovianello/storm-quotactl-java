package it.grid.storm.api.filesystem.quota.posix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;

import it.grid.storm.api.filesystem.quota.posix.CLibrary.T_dqblk;;

public class PosixQuotaManager {

	private static final Logger log = LoggerFactory.getLogger(PosixQuotaManager.class);

	private static CLibrary cLib = (CLibrary) Native.loadLibrary("c", CLibrary.class);

	private static int GETGROUPQUOTA_CMD = 0x80000701;

	private String getErrnoMsg(int errNo) {

		switch (errNo) {

		case CLibrary.EFAULT:
			return "addr or special is invalid.";
		case CLibrary.EINVAL:
			return "cmd or type is invalid.";
		case CLibrary.ENOENT:
			return "The file specified by special or addr does not exist.";
		case CLibrary.ENOSYS:
			return "The kernel has not been compiled with the CONFIG_QUOTA option.";
		case CLibrary.ENOTBLK:
			return "special is not a block device.";
		case CLibrary.EPERM:
			return "The caller lacked the required privilege (CAP_SYS_ADMIN) for the specified operation.";
		case CLibrary.ESRCH:
			return "No disk quota is found for the indicated user. Quotas have not been turned on for this filesystem.";
		case CLibrary.EIO:
			return "Cannot read or write the quota file.";
		case CLibrary.EMFILE:
			return "Too many open files: cannot open quota file.";
		case CLibrary.ENODEV:
			return "special cannot be found in the mount table.";
		default:
			return "Unrecognized error " + errNo + " !";
		}
	}

	public PosixQuotaInfo getGroupQuota(String blockDevice, int gid) throws PosixQuotaException {

		log.debug("PosixQuotaManager.getGroupQuota({},{})", blockDevice, gid);
		T_dqblk dablk = new T_dqblk();
		int res;
		try {
			
			log.debug("quotactl({},{},{}) ...", Integer.toHexString(GETGROUPQUOTA_CMD), blockDevice, gid);
			res = cLib.quotactl(GETGROUPQUOTA_CMD, blockDevice, gid, dablk);
			log.debug("quotactl exited with {} and returned {}", res, dablk.toString());
		
		} catch (LastErrorException e) {

			int errNo = e.getErrorCode();
			String errMsg = getErrnoMsg(e.getErrorCode());
			String message = String.format("Unable to load quota information for device %s and gid %d: [%d] %s",
					blockDevice, gid, errNo, errMsg);
			log.error(message);
			throw new PosixQuotaException(message, e);
		}
		
		return new PosixQuotaInfo(dablk);
	}

}
