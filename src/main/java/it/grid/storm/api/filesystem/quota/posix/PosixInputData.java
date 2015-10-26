package it.grid.storm.api.filesystem.quota.posix;

import com.google.common.base.Preconditions;

import it.grid.storm.api.filesystem.quota.QuotaInputData;

public class PosixInputData implements QuotaInputData {

	private String blockDevice;
	private int gid;
	
	public PosixInputData(String blockDevice, int gid) {
		
		Preconditions.checkNotNull(blockDevice, "Invalid null blockDevice");
		Preconditions.checkArgument(gid > 0, "Invalid gid value: %d", gid);
		
		this.blockDevice = blockDevice;
		this.gid = gid;
	}

	/**
	 * @return the blockDevice
	 */
	public String getBlockDevice() {
		
		return blockDevice;
	}

	/**
	 * @return the gid
	 */
	public int getGid() {
		
		return gid;
	}

	@Override
	public String toString() {
		return "PosixInputData [blockDevice=" + blockDevice + ", gid=" + gid + "]";
	}
	
}
