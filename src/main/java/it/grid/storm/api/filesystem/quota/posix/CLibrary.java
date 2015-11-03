package it.grid.storm.api.filesystem.quota.posix;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

/**
 * CLibrary maps the standard C library to a Java interface through JNA.
 * <p>
 * Java Native Access (JNA) provides simplified access to native library methods
 * without requiring any additional JNI or native code. Function names are
 * mapped directly from their Java interface name to the symbol exported by the
 * native library.
 * 
 * @author vianello
 *
 */
public interface CLibrary extends Library {

	CLibrary INSTANCE = (CLibrary) Native.loadLibrary("c", CLibrary.class);

	/**
	 * T_dqblk is the correspondent of the dqblk structure defined in
	 * sys/quota.h and returned by reference as addr field after quotactl()
	 * call with Q_GETQUOTA.
	 *
	 * @author Enrico Vianello
	 *
	 */
	class T_dqblk extends Structure implements Structure.ByReference {

		/**
		 * The absolute limit on disk quota blocks allocation.
		 */
		public long dqb_bhardlimit;
		
		/**
		 * The preferred limit on disk quota blocks.
		 */
		public long dqb_bsoftlimit;
		/**
		 * The current quota block count.
		 */
		public long dqb_curspace;
		/**
		 * The maximum number of allocated inodes.
		 */
		public long dqb_ihardlimit;
		/**
		 * The preferred inode limit.
		 */
		public long dqb_isoftlimit;
		/**
		 * The current number of allocated inodes.
		 */
		public long dqb_curinodes;
		/**
		 * The time limit for excessive disk use.
		 */
		public long dqb_btime;
		/**
		 * The time limit for excessive files.
		 */
		public long dqb_itime;
		/**
		 * The bit mask of QIF_* constants
		 */
		public int dqb_valid;

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sun.jna.Structure#getFieldOrder()
		 */
		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList(new String[] { "dqb_bhardlimit", "dqb_bsoftlimit", "dqb_curspace", "dqb_ihardlimit",
					"dqb_isoftlimit", "dqb_curinodes", "dqb_btime", "dqb_itime", "dqb_valid" });
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sun.jna.Structure#toString()
		 */
		@Override
		public String toString() {
			return "T_dqblk [dqb_bhardlimit=" + dqb_bhardlimit + ", dqb_bsoftlimit=" + dqb_bsoftlimit
					+ ", dqb_curspace=" + dqb_curspace + ", dqb_ihardlimit=" + dqb_ihardlimit + ", dqb_isoftlimit="
					+ dqb_isoftlimit + ", dqb_curinodes=" + dqb_curinodes + ", dqb_btime=" + dqb_btime + ", dqb_itime="
					+ dqb_itime + ", dqb_valid=" + Integer.toBinaryString(dqb_valid) + "]";
		}

	};

	/**
	 * The standard C library quotactl() method from sys/quota.h which manipulates disk quotas. 
	 * <p>
	 * {@code int quotactl(int cmd, const char *special, int id, caddr_t addr); }
	 * <p>
	 * The cmd argument indicates a command to be applied to the user or group ID specified in id. To
	 * initialize the cmd argument, use the QCMD(subcmd, type) macro. The type
	 * value is either USRQUOTA, for user quotas, or GRPQUOTA, for group quotas.
	 * The subcmd value is described below.
	 * 
	 * The special argument is a pointer to a null-terminated string containing
	 * the pathname of the (mounted) block special device for the filesystem
	 * being manipulated.
	 * 
	 * The addr argument is the address of an optional, command-specific, data
	 * structure that is copied in or out of the system. The interpretation of
	 * addr is given with each command below.
	 * 
	 * The subcmd value is one of the following:
	 * <ul>
	 * <li><b>Q_QUOTAON</b> Turn on quotas for a filesystem.</li>
	 * <li><b>Q_QUOTAOFF</b> Turn off quotas for a filesystem.</li>
	 * <li><b>Q_GETQUOTA</b> Get disk quota limits and current usage for user or group id.</li>
	 * <li><b>Q_SETQUOTA</b> Set quota information for user or group id.</li>
	 * <li><b>Q_GETINFO</b> Get information (like grace times) about quotafile.</li>
	 * <li><b>Q_SETINFO</b> Set information about quotafile.</li>
	 * <li><b>Q_GETFMT</b> Get quota format used on the specified filesystem.</li>
	 * <li><b>Q_SYNC</b> Update the on-disk copy of quota usages for a filesystem.</li>
	 * <li><b>Q_GETSTATS</b> Get statistics and other generic information about the quota
	 * subsystem.</li>
	 * </ul>
	 * 
	 * @param cmd An unique identifier of the pair (subcmd,type)
	 * @param special The pathname of the (mounted) block special device for the filesystem being manipulated
	 * @param id User or group id
	 * @param addr An optional command-specific data structure
	 * @return The exit code
	 * @throws LastErrorException Exception representing a non-zero error code returned.
	 */
	int quotactl(int cmd, String special, int id, Structure addr) throws LastErrorException;
}
