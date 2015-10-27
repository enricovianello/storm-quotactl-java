package it.grid.storm.api.filesystem.quota.posix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.Structure;

import it.grid.storm.api.filesystem.quota.posix.CLibrary.T_dqblk;;

public class PosixQuotaManager {
	
	private static final Logger log = LoggerFactory.getLogger(PosixQuotaManager.class);
	
	private static CLibrary cLib = (CLibrary) Native.loadLibrary("c", CLibrary.class);
	
	private static int GETQUOTA_CMD = 0x80000701;
	
	private Structure quotactl(int cmd, String blockDevice, int id) throws LastErrorException {
		
		Preconditions.checkNotNull(blockDevice, "Invalid blockDevice argument: null");
		
		log.debug("quotactl({},{},{}) ...", Integer.toHexString(cmd), blockDevice, id);
		T_dqblk dablk = new T_dqblk();
		int res = cLib.quotactl(cmd, blockDevice, id, dablk);
		log.debug("quotactl exited with {}", res);
		if (res >= 0) {
			log.debug("quotactl returned info: {}", dablk.toString());
		}
		return dablk;
	}
	
	public PosixQuotaInfo getQuotaInfo(String blockDevice, int gid) throws PosixQuotaException {
		
		log.debug("PosixQuotaManager.getQuotaInfo({},{})", blockDevice, gid);
		
		Preconditions.checkNotNull(blockDevice, "Invalid null blockDevice");
		Preconditions.checkArgument(gid > 0, "Invalid gid value: %d", gid);
		
		try {
			return new PosixQuotaInfo((T_dqblk) quotactl(GETQUOTA_CMD, blockDevice, gid));
		} catch (LastErrorException e) {
			log.error("quotactl failed with errno = {} and message: {}", e.getErrorCode(), e.getMessage());
			throw new PosixQuotaException(String.format("Unable to load quota information for device {} and gid {}", blockDevice, gid), e);
		}
	}

}
