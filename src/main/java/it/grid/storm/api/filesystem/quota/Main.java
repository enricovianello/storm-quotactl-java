package it.grid.storm.api.filesystem.quota;

import it.grid.storm.api.filesystem.quota.posix.PosixInputData;
import it.grid.storm.api.filesystem.quota.posix.PosixQuotaManager;

public class Main {

	public static void main (String [] args) {
		
		int gid = 502;
		String blockDevice = "/dev/disk/by-uuid/1d798f26-8ace-413f-9530-2d1d1d4fdbb5";
		
		QuotaInfo info = null;
		try {
			info = (new PosixQuotaManager()).getQuotaInfo(new PosixInputData(blockDevice, gid));
		} catch (QuotaException e) {
			System.err.printf("Error: %s", e.getMessage());
			e.printStackTrace();
			return;
		}

		System.out.printf("block-device: %s\n", blockDevice);
		System.out.printf("block-hard-limit: %d\n", info.getBlockHardLimit());
		System.out.printf("block-soft-limit: %d\n", info.getBlockSoftLimit());
		System.out.printf("block-usage: %d\n", info.getBlockUsage());
		
	}

}
