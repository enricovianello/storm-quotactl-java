package it.grid.storm.api.filesystem.quota.posix;

public class PosixQuotaInfo {
	
	private final CLibrary.T_dqblk dablk;
	
	public static final short QIF_BLIMITS = 1;
	public static final short QIF_SPACE = 2;
	public static final short QIF_ILIMITS = 4;
	public static final short QIF_INODES = 8;
	public static final short QIF_BTIME = 16;
	public static final short QIF_ITIME = 32;
	public static final short QIF_LIMITS = (QIF_BLIMITS | QIF_ILIMITS);
	public static final short QIF_USAGE = (QIF_SPACE | QIF_INODES);
	public static final short QIF_TIMES = (QIF_BTIME | QIF_ITIME);
	public static final short QIF_ALL = (QIF_LIMITS | QIF_USAGE | QIF_TIMES);
	
	public PosixQuotaInfo(CLibrary.T_dqblk dablk) {
		this.dablk = dablk;
	}

	public long getBlockUsage() {
		return dablk.dqb_curspace;
	}

	public long getBlockHardLimit() {
		return dablk.dqb_bhardlimit;
	}
	
	public long getBlockSoftLimit() {
		return dablk.dqb_bsoftlimit;
	}
	
	public long getINodesHardLimit() {
		return dablk.dqb_ihardlimit;
	}
	
	public long getINodesSoftLimit() {
		return dablk.dqb_isoftlimit;
	}

	public long getINodesUsage() {
		return dablk.dqb_curinodes;
	}
	
	public long getBlockTimeLimit() {
		return dablk.dqb_btime;
	}

	public long getINodesTimeLimit() {
		return dablk.dqb_itime;
	}
	
	public int getValid() {
		return dablk.dqb_valid;
	}

	@Override
	public String toString() {
		return "PosixQuotaInfo [dablk=" + dablk + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dablk == null) ? 0 : dablk.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PosixQuotaInfo other = (PosixQuotaInfo) obj;
		if (dablk == null) {
			if (other.dablk != null)
				return false;
		} else if (!dablk.equals(other.dablk))
			return false;
		return true;
	}

	
	
};
