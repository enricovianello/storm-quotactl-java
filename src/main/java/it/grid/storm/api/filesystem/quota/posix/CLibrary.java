package it.grid.storm.api.filesystem.quota.posix;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Structure;

public interface CLibrary extends Library {

	/* Error codes from errno.h */
	public static int EPERM = 1;
	public static int ENOENT = 2;
	public static int ESRCH = 3;
	public static int EFAULT = 14;
	public static int ENOTBLK = 15;
	public static int EINVAL = 22;
	public static int ENOSYS = 78;

	/* struct dqblk from quota.h */
	class T_dqblk extends Structure implements Structure.ByReference {

		public long dqb_bhardlimit;
		public long dqb_bsoftlimit;
		public long dqb_curspace;
		public long dqb_ihardlimit;
		public long dqb_isoftlimit;
		public long dqb_curinodes;
		public long dqb_btime;
		public long dqb_itime;
		public int dqb_valid;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList(new String[] { "dqb_bhardlimit", "dqb_bsoftlimit", "dqb_curspace", "dqb_ihardlimit",
					"dqb_isoftlimit", "dqb_curinodes", "dqb_btime", "dqb_itime", "dqb_valid" });
		}

		@Override
		public String toString() {
			return "T_dqblk [dqb_bhardlimit=" + dqb_bhardlimit + ", dqb_bsoftlimit=" + dqb_bsoftlimit
					+ ", dqb_curspace=" + dqb_curspace + ", dqb_ihardlimit=" + dqb_ihardlimit + ", dqb_isoftlimit="
					+ dqb_isoftlimit + ", dqb_curinodes=" + dqb_curinodes + ", dqb_btime=" + dqb_btime + ", dqb_itime="
					+ dqb_itime + ", dqb_valid=" + Integer.toBinaryString(dqb_valid) + "]";
		}	
		
	};

	/* quotactl method from quota.h */
	int quotactl(int cmd, String special, int id, Structure addr) throws LastErrorException;
}
