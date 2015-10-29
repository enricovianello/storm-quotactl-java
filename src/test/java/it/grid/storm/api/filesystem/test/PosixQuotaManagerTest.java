package it.grid.storm.api.filesystem.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.LastErrorException;
import com.sun.jna.Structure;

import it.grid.storm.api.filesystem.quota.posix.CLibrary;
import it.grid.storm.api.filesystem.quota.posix.PosixQuotaManager;
import it.grid.storm.api.filesystem.quota.posix.PosixQuotaException;
import it.grid.storm.api.filesystem.quota.posix.PosixQuotaInfo;

public class PosixQuotaManagerTest {
	
	private static final Logger log = LoggerFactory.getLogger(PosixQuotaManagerTest.class);
	
	private static boolean doLocalTests = false;
	private static String blockDevice = "/dev/sdb1";
	private static int gid = 1003;
	private static int expectedErrNo = 0;
	private static long expectedBlockHardLimit = 1000;

	@BeforeClass
	public static void setUpBeforeClass() {
		
		doLocalTests = Boolean.valueOf((String) System.getProperties().get("islocal"));
		blockDevice = (String) System.getProperties().get("blockdevice");
		gid = Integer.valueOf((String) System.getProperties().get("gid"));
		expectedErrNo = Integer.valueOf((String) System.getProperties().get("errno"));
		expectedBlockHardLimit = Long.valueOf((String) System.getProperties().get("bhardlimit"));
		
		log.debug("doLocalTests: {}", doLocalTests);
		log.debug("blockDevice: {}", blockDevice);
		log.debug("gid: {}", gid);
		log.debug("expectedErrNo: {}", expectedErrNo);
		log.debug("expectedBlockHardLimit: {}", expectedBlockHardLimit);
	}
	
	static void setFinalStatic(Field field, Object newValue) throws Exception {

		field.setAccessible(true);
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		field.set(null, newValue);
	}
	
	private PosixQuotaManager getMockedQuotaManagerFailsWith(int errNo) {
		
		CLibrary mockedCLib = mock(CLibrary.class);
		LastErrorException e = new LastErrorException("[" + errNo + "]");
		Mockito.when(
				mockedCLib.quotactl(any(Integer.class), any(String.class), any(Integer.class), any(Structure.class)))
				.thenThrow(e);
		try {
			setFinalStatic(PosixQuotaManager.class.getDeclaredField("cLib"), mockedCLib);
		} catch (Throwable t) {
			t.printStackTrace();
			fail(t.getMessage());
		}
		return new PosixQuotaManager();
	}
	
	private void checkQuotactlSuccess(PosixQuotaManager pqm, String blockDevice, int gid, PosixQuotaInfo expectedResult) {

		PosixQuotaInfo pqi;
		try {

			pqi = pqm.getGroupQuota(blockDevice, gid);

		} catch (PosixQuotaException pqe) {

			fail("It shouldn't fail!");
			return;
		}
		assertTrue(pqi.getBlockHardLimit() == expectedResult.getBlockHardLimit());
	}

	private void checkQuotactlFailWith(PosixQuotaManager pqm, String blockDevice, int gid, int errNo) {

		try {

			pqm.getGroupQuota(blockDevice, gid);

		} catch (PosixQuotaException pqe) {

			assertTrue(pqe.getCause() instanceof LastErrorException);
			assertTrue(((LastErrorException) pqe.getCause()).getErrorCode() == errNo);
			return;
		}

		fail("Error " + errNo + " not recognized!");
	}

	@Test
	public void testSuccess() throws NoSuchFieldException, SecurityException, Exception {

		if (!doLocalTests) {
			log.info("Local tests disabled");
			return;
		}
		
		if (expectedErrNo == 0) {
			
			CLibrary.T_dqblk dablk = new CLibrary.T_dqblk();
			dablk.dqb_bhardlimit = expectedBlockHardLimit;
			dablk.dqb_valid = PosixQuotaInfo.QIF_ALL;
			checkQuotactlSuccess(new PosixQuotaManager(), blockDevice, gid, new PosixQuotaInfo(dablk));
		
		} else {
		
			checkQuotactlFailWith(new PosixQuotaManager(), blockDevice, gid, expectedErrNo);
		}
	}
	
	@Test
	public void testEFAULT() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(CLibrary.EFAULT);
		checkQuotactlFailWith(pqm, blockDevice, gid, CLibrary.EFAULT);
	}

	@Test
	public void testEINVAL() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(CLibrary.EINVAL);
		checkQuotactlFailWith(pqm, blockDevice, gid, CLibrary.EINVAL);
	}

	@Test
	public void testENOENT() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(CLibrary.ENOENT);
		checkQuotactlFailWith(pqm, blockDevice, gid, CLibrary.ENOENT);
	}

	@Test
	public void testENOSYS() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(CLibrary.ENOSYS);
		checkQuotactlFailWith(pqm, blockDevice, gid, CLibrary.ENOSYS);
	}

	@Test
	public void testENOTBLK() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(CLibrary.ENOTBLK);
		checkQuotactlFailWith(pqm, blockDevice, gid, CLibrary.ENOTBLK);
	}

	@Test
	public void testEPERM() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(CLibrary.EPERM);
		checkQuotactlFailWith(pqm, blockDevice, gid, CLibrary.EPERM);
	}

	@Test
	public void testESRCH() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(CLibrary.ESRCH);
		checkQuotactlFailWith(pqm, blockDevice, gid, CLibrary.ESRCH);
	}
	
	@Test
	public void testEIO() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(CLibrary.EIO);
		checkQuotactlFailWith(pqm, blockDevice, gid, CLibrary.EIO);
	}
	
	@Test
	public void testEMFILE() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(CLibrary.EMFILE);
		checkQuotactlFailWith(pqm, blockDevice, gid, CLibrary.EMFILE);
	}

	@Test
	public void testENODEV() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(CLibrary.ENODEV);
		checkQuotactlFailWith(pqm, blockDevice, gid, CLibrary.ENODEV);
	}
	
}
