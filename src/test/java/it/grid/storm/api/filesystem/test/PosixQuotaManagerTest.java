package it.grid.storm.api.filesystem.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.LastErrorException;
import com.sun.jna.Structure;

import it.grid.storm.api.filesystem.quota.posix.CLibrary;
import it.grid.storm.api.filesystem.quota.posix.ErrNo;
import it.grid.storm.api.filesystem.quota.posix.PosixQuotaManager;
import it.grid.storm.api.filesystem.quota.posix.PosixQuotaException;
import it.grid.storm.api.filesystem.quota.posix.PosixQuotaInfo;

public class PosixQuotaManagerTest {
	
	private static final Logger log = LoggerFactory.getLogger(PosixQuotaManagerTest.class);
	
	private static String FAKE_BLOCKDEVICE = "/dev/fake";
	private static int FAKE_GID = 1000;
	
	private static boolean doLocalTests = false;

	@BeforeClass
	public static void setUpBeforeClass() {
		
		doLocalTests = true;
		//doLocalTests = Boolean.valueOf((String) System.getProperties().get("haslocal"));
		log.debug("doLocalTests: {}", doLocalTests);
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

			log.info(pqe.getMessage());
			assertTrue(pqe.getCause() instanceof LastErrorException);
			assertTrue(((LastErrorException) pqe.getCause()).getErrorCode() == errNo);
			return;
		}

		fail("Error " + errNo + " not recognized!");
	}

	@Test
	@Category(LocalTests.class)
	public void testSuccess() throws NoSuchFieldException, SecurityException, Exception {

		if (!doLocalTests) {
			log.info("Local tests disabled");
			return;
		}
		
		String blockdevice = "/dev/sdb";
		int gid = 1002;
		
		CLibrary.T_dqblk dablk = new CLibrary.T_dqblk();
		dablk.dqb_bhardlimit = 1000;
		dablk.dqb_valid = PosixQuotaInfo.QIF_ALL;
		checkQuotactlSuccess(new PosixQuotaManager(), blockdevice, gid, new PosixQuotaInfo(dablk));

	}
	
	@Test
	@Category(MockedTests.class)
	public void testEFAULT() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.EFAULT);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.EFAULT);
	}

	@Test
	@Category(MockedTests.class)
	public void testEINVAL() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.EINVAL);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.EINVAL);
	}

	@Test
	@Category(MockedTests.class)
	public void testENOENT() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.ENOENT);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.ENOENT);
	}

	@Test
	@Category(MockedTests.class)
	public void testENOSYS() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.ENOSYS);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.ENOSYS);
	}

	@Test
	@Category(MockedTests.class)
	public void testENOTBLK() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.ENOTBLK);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.ENOTBLK);
	}

	@Test
	@Category(MockedTests.class)
	public void testEPERM() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.EPERM);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.EPERM);
	}

	@Test
	@Category(MockedTests.class)
	public void testESRCH() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.ESRCH);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.ESRCH);
	}
	
	@Test
	@Category(MockedTests.class)
	public void testEIO() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.EIO);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.EIO);
	}
	
	@Test
	@Category(MockedTests.class)
	public void testEMFILE() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.EMFILE);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.EMFILE);
	}

	@Test
	@Category(MockedTests.class)
	public void testENODEV() throws NoSuchFieldException, SecurityException, Exception {

		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.ENODEV);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.ENODEV);
	}
	
}
