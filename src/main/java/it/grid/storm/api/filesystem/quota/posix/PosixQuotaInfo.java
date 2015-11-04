package it.grid.storm.api.filesystem.quota.posix;

/**
 * The quota information associated to a block device. It's a
 * {@link CLibrary.T_dqblk} object wrapper.
 * 
 * @author Enrico Vianello
 *
 */
public class PosixQuotaInfo {

	private final CLibrary.T_dqblk dablk;

	/*
	 * Flags in dqb_valid that indicate which fields in dqblk structure are
	 * valid.
	 */

	/**
	 * Flag enabled if block limits are valid
	 */
	public static final short QIF_BLIMITS = 1;

	/**
	 * Flag enabled if the current number of allocated blocks is valid.
	 */
	public static final short QIF_SPACE = 2;

	/**
	 * Flag enabled if inode limits are valid
	 */
	public static final short QIF_ILIMITS = 4;

	/**
	 * Flag enabled if the current number of allocated inode is valid.
	 */
	public static final short QIF_INODES = 8;

	/**
	 * Flag enabled if the time limit for excessive disk use is valid.
	 */
	public static final short QIF_BTIME = 16;

	/**
	 * Flag enabled if the time limit for excessive files is valid.
	 */
	public static final short QIF_ITIME = 32;

	/**
	 * Flag which indicates if all the limits are valid.
	 */
	public static final short QIF_LIMITS = (QIF_BLIMITS | QIF_ILIMITS);

	/**
	 * Flag which indicates if all the space usage are valid.
	 */
	public static final short QIF_USAGE = (QIF_SPACE | QIF_INODES);

	/**
	 * Flag which indicates if all the time limits are valid
	 */
	public static final short QIF_TIMES = (QIF_BTIME | QIF_ITIME);

	/**
	 * Flag which indicates if all the values are valid.
	 */
	public static final short QIF_ALL = (QIF_LIMITS | QIF_USAGE | QIF_TIMES);

	/**
	 * The constructor builds the object information by copying them from the
	 * {@link CLibrary.T_dqblk} object passed as argument.
	 * 
	 * @param dablk
	 *            The {@link CLibrary.T_dqblk} object returned by a standard C
	 *            library quotactl() call.
	 */
	public PosixQuotaInfo(CLibrary.T_dqblk dablk) {
		this.dablk = dablk;
	}

	/**
	 * Get the current quota block count.
	 * 
	 * @return The current quota block count.
	 */
	public long getBlockUsage() {
		return dablk.dqb_curspace;
	}

	/**
	 * Get the absolute limit on disk quota blocks allocation.
	 * <p>
	 * The blocks quota controls how much disk space the user can use, and is
	 * specified in disk blocks which are typically 1 kB in size.
	 * 
	 * @return the absolute limit on disk quota blocks allocation.
	 */
	public long getBlockHardLimit() {
		return dablk.dqb_bhardlimit;
	}

	/**
	 * Get the preferred limit on disk quota blocks.
	 * <p>
	 * The soft limit must be less than the hard limit. Once the user exceeds
	 * the soft limit, a quota timer begins. While the quota timer is ticking,
	 * the user is allowed to operate above the soft limit but cannot exceed the
	 * hard limit. Once the user goes below the soft limit, the timer is reset.
	 * However, if the user's usage remains above the soft limit when the timer
	 * expires, the soft limit is enforced as a hard limit.
	 * 
	 * @return the preferred limit on disk quota blocks.
	 */
	public long getBlockSoftLimit() {
		return dablk.dqb_bsoftlimit;
	}

	/**
	 * Get the maximum number of allocated inodes.
	 * <p>
	 * The files quota controls how many separate files the a user can create,
	 * and is necessary because Unix filesystems often have a limit on how many
	 * files can exist at one time. Without a files quota, a user could create
	 * millions of empty files until the filesystems limit was reached, and so
	 * prevent other users from creating any files at all.
	 * 
	 * @return the maximum number of allocated inodes.
	 */
	public long getINodesHardLimit() {
		return dablk.dqb_ihardlimit;
	}

	/**
	 * Get the preferred inode limit.
	 * <p>
	 * The soft limit must be less than the hard limit. Once the user exceeds
	 * the soft limit, a quota timer begins. While the quota timer is ticking,
	 * the user is allowed to operate above the soft limit but cannot exceed the
	 * hard limit. Once the user goes below the soft limit, the timer is reset.
	 * However, if the user's usage remains above the soft limit when the timer
	 * expires, the soft limit is enforced as a hard limit.
	 * 
	 * @return the preferred inode limit.
	 */
	public long getINodesSoftLimit() {
		return dablk.dqb_isoftlimit;
	}

	/**
	 * Get the current number of allocated inodes.
	 * 
	 * @return the current number of allocated inodes.
	 */
	public long getINodesUsage() {
		return dablk.dqb_curinodes;
	}

	/**
	 * Get the time limit for excessive disk use.
	 * 
	 * @return the time limit for excessive disk use.
	 */
	public long getBlockTimeLimit() {
		return dablk.dqb_btime;
	}

	/**
	 * Get the time limit for excessive files.
	 * 
	 * @return the time limit for excessive files.
	 */
	public long getINodesTimeLimit() {
		return dablk.dqb_itime;
	}

	/**
	 * Get the bit mask of QIF_* constants.
	 * <p>
	 * The returned value is a bit mask that is set to indicate which values
	 * contained into the object are valid. Unprivileged users may retrieve only
	 * their own quotas; a privileged user can retrieve the quotas of any user.
	 * <p>
	 * The available QIF_* constants are:
	 * <ul>
	 * <li>{@link QIF_BLIMITS}</li>
	 * <li>{@link QIF_SPACE}</li>
	 * <li>{@link QIF_ILIMITS}</li>
	 * <li>{@link QIF_INODES}</li>
	 * <li>{@link QIF_BTIME}</li>
	 * <li>{@link QIF_ITIME}</li>
	 * <li>{@link QIF_LIMITS}</li>
	 * <li>{@link QIF_USAGE}</li>
	 * <li>{@link QIF_TIMES}</li>
	 * <li>{@link QIF_ALL}</li>
	 * </ul>
	 *
	 * @return the bit mask of QIF_* constants.
	 */
	public int getValid() {
		return dablk.dqb_valid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PosixQuotaInfo [dablk=" + dablk + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dablk == null) ? 0 : dablk.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
