package it.grid.storm.api.filesystem.quota;

public class QuotaInfo {
	
	private long blockUsage;
	private long blockHardLimit;
	private long blockSoftLimit;
	
	/**
	 * @param blockUsage
	 * @param blockHardLimit
	 * @param blockSoftLimit
	 */
	public QuotaInfo(long blockUsage, long blockHardLimit, long blockSoftLimit) {
		this.blockUsage = blockUsage;
		this.blockHardLimit = blockHardLimit;
		this.blockSoftLimit = blockSoftLimit;
	}
	
	/**
	 * @return the blockUsage
	 */
	public long getBlockUsage() {
		return blockUsage;
	}
	/**
	 * @return the blockHardLimit
	 */
	public long getBlockHardLimit() {
		return blockHardLimit;
	}
	/**
	 * @return the blockSoftLimit
	 */
	public long getBlockSoftLimit() {
		return blockSoftLimit;
	}
};
