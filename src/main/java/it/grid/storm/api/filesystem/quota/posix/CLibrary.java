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
	public static int EIO = 5;
	public static int EFAULT = 14;
	public static int ENOTBLK = 15;
	public static int ENODEV = 19;
	public static int EINVAL = 22;
	public static int EMFILE = 23;
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + (int) (dqb_bhardlimit ^ (dqb_bhardlimit >>> 32));
			result = prime * result + (int) (dqb_bsoftlimit ^ (dqb_bsoftlimit >>> 32));
			result = prime * result + (int) (dqb_btime ^ (dqb_btime >>> 32));
			result = prime * result + (int) (dqb_curinodes ^ (dqb_curinodes >>> 32));
			result = prime * result + (int) (dqb_curspace ^ (dqb_curspace >>> 32));
			result = prime * result + (int) (dqb_ihardlimit ^ (dqb_ihardlimit >>> 32));
			result = prime * result + (int) (dqb_isoftlimit ^ (dqb_isoftlimit >>> 32));
			result = prime * result + (int) (dqb_itime ^ (dqb_itime >>> 32));
			result = prime * result + dqb_valid;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			T_dqblk other = (T_dqblk) obj;
			if (dqb_bhardlimit != other.dqb_bhardlimit)
				return false;
			if (dqb_bsoftlimit != other.dqb_bsoftlimit)
				return false;
			if (dqb_btime != other.dqb_btime)
				return false;
			if (dqb_curinodes != other.dqb_curinodes)
				return false;
			if (dqb_curspace != other.dqb_curspace)
				return false;
			if (dqb_ihardlimit != other.dqb_ihardlimit)
				return false;
			if (dqb_isoftlimit != other.dqb_isoftlimit)
				return false;
			if (dqb_itime != other.dqb_itime)
				return false;
			if (dqb_valid != other.dqb_valid)
				return false;
			return true;
		}	
		
	};

	/* quotactl method from quota.h */
	int quotactl(int cmd, String special, int id, Structure addr) throws LastErrorException;
}
