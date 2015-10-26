package it.grid.storm.api.filesystem.quota.posix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.Structure;

import it.grid.storm.api.filesystem.quota.QuotaException;
import it.grid.storm.api.filesystem.quota.QuotaInfo;
import it.grid.storm.api.filesystem.quota.QuotaInputData;
import it.grid.storm.api.filesystem.quota.QuotaManager;
import it.grid.storm.api.filesystem.quota.posix.CLibrary.T_dqblk;;

public class PosixQuotaManager implements QuotaManager {
	
	private static final Logger log = LoggerFactory.getLogger(PosixQuotaManager.class);
	
	private static CLibrary cLib = (CLibrary) Native.loadLibrary("c", CLibrary.class);
	
	private static int GETQUOTA_CMD = 0x80000701;
	private static int QIF_USAGE_BLIMITS = 3;
	
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
	
	public QuotaInfo getQuotaInfo(QuotaInputData inputData) throws QuotaException {
		
		log.debug("getQuotaInfo({})", inputData);
		
		Preconditions.checkNotNull(inputData, "Invalid null getQuotaInfo input data");
		Preconditions.checkArgument(inputData instanceof PosixInputData, "Invalid getQuotaInfo input data type");
		
		PosixInputData pid = (PosixInputData) inputData;
		T_dqblk dablk;
		try {
			dablk = (T_dqblk) quotactl(GETQUOTA_CMD, pid.getBlockDevice(), pid.getGid());
		} catch (LastErrorException e) {
			log.error("quotactl failed with errno = {} and message: {}", e.getErrorCode(), e.getMessage());
			throw new QuotaException(String.format("Unable to load quota information for device {} and gid {}", pid.getBlockDevice(), pid.getGid()), e);
		}
		/* check if returned value are valid */
		if ((dablk.dqb_valid & QIF_USAGE_BLIMITS) != QIF_USAGE_BLIMITS) {
			throw new QuotaException("getQuotaInfo: invalid blimits and usage values returned by quotactl!");
		}
		log.debug("getQuotaInfo: blimits and usage values are valid");
		return new QuotaInfo(dablk.dqb_curspace, dablk.dqb_bhardlimit, dablk.dqb_bsoftlimit);		
	}

}
