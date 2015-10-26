package it.grid.storm.api.filesystem.test;

import static org.junit.Assert.*;

import org.junit.Test;

import it.grid.storm.api.filesystem.quota.posix.PosixInputData;

/**
 * @author vianello
 *
 */
public class PosixInputDataTest {

	/**
	 * Test method for {@link it.grid.storm.api.filesystem.quota.posix.PosixInputData#PosixInputData(java.lang.String, int)}.
	 */
	@Test
	public void testPosixInputData() {
		
		String blockDevice = "/path/to/device";
		int gid = 500;
		PosixInputData pid = new PosixInputData(blockDevice, gid);
		assertNotNull(pid);
		assertEquals(blockDevice, pid.getBlockDevice());
		assertEquals(gid, pid.getGid());
	}

}
