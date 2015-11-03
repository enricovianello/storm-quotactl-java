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

	@BeforeClass
	public static void setUpBeforeClass() {

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
			setFinalStatic(CLibrary.class.getDeclaredField("INSTANCE"), mockedCLib);
		} catch (Throwable t) {
			t.printStackTrace();
			fail(t.getMessage());
		}
		return new PosixQuotaManager();
	}

	private PosixQuotaInfo checkQuotactlSuccess(PosixQuotaManager pqm, String blockDevice, int gid) {

		PosixQuotaInfo pqi = null;
		try {

			pqi = pqm.getGroupQuota(blockDevice, gid);

		} catch (PosixQuotaException pqe) {

			log.error("PosixQuotaException: {} - {}", pqe.getMessage(), pqe.getCause());
			fail("It shouldn't fail!");
		}
		return pqi;
	}

	private void checkQuotactlFailWith(PosixQuotaManager pqm, String blockDevice, int gid, int errNo) {

		try {

			pqm.getGroupQuota(blockDevice, gid);

		} catch (PosixQuotaException pqe) {

			log.info(pqe.getMessage());
			assertTrue(pqe.getCause() instanceof LastErrorException);
			log.debug("Error code returned: {}", ((LastErrorException) pqe.getCause()).getErrorCode());
			assertTrue(((LastErrorException) pqe.getCause()).getErrorCode() == errNo);
			return;
		}

		fail("Error " + errNo + " not recognized!");
	}

	@Test
	@Category(LocalTests.class)
	public void testLocalSuccess() throws NoSuchFieldException, SecurityException, Exception {

		String blockdevice = "/dev/sdb";
		int gid = 1002;

		log.debug("{} test on block device {} with gid {} expecting {}", 
				"testLocalSuccess", blockdevice, gid, "success");
		
		PosixQuotaInfo pqi = checkQuotactlSuccess(new PosixQuotaManager(), blockdevice, gid);
		
		assertTrue(pqi.getBlockHardLimit() == 1000);
		assertTrue(pqi.getValid() == PosixQuotaInfo.QIF_ALL);
	}
	
	@Test
	@Category(LocalTests.class)
	public void testLocalFailureEPERM() throws NoSuchFieldException, SecurityException, Exception {

		String blockdevice = "/dev/sdb";
		int gid = 1000;

		log.debug("{} test on block device {} with gid {} expecting {}", 
				"testLocalFailureEPERM", blockdevice, gid, ErrNo.EPERM);
		
		checkQuotactlFailWith(new PosixQuotaManager(), blockdevice, gid, ErrNo.EPERM);
	}
	
	@Test
	@Category(LocalTests.class)
	public void testLocalFailureENODEV() throws NoSuchFieldException, SecurityException, Exception {

		String blockdevice = FAKE_BLOCKDEVICE;
		int gid = 1002;

		log.debug("{} test on block device {} with gid {} expecting {}", 
				"testLocalFailureENODEV", blockdevice, gid, ErrNo.ENODEV);
		
		checkQuotactlFailWith(new PosixQuotaManager(), blockdevice, gid, ErrNo.ENODEV);
	}

	@Test
	@Category(MockedTests.class)
	public void testEFAULT() throws NoSuchFieldException, SecurityException, Exception {

		log.debug("{} test on block device {} with gid {} expecting {}", 
				"testEFAULT", FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.EFAULT);
		
		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.EFAULT);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.EFAULT);
	}

	@Test
	@Category(MockedTests.class)
	public void testEINVAL() throws NoSuchFieldException, SecurityException, Exception {

		log.debug("{} test on block device {} with gid {} expecting {}", 
				"testEINVAL", FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.EINVAL);
		
		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.EINVAL);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.EINVAL);
	}

	@Test
	@Category(MockedTests.class)
	public void testENOENT() throws NoSuchFieldException, SecurityException, Exception {

		log.debug("{} test on block device {} with gid {} expecting {}", 
				"testENOENT", FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.ENOENT);
		
		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.ENOENT);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.ENOENT);
	}

	@Test
	@Category(MockedTests.class)
	public void testENOSYS() throws NoSuchFieldException, SecurityException, Exception {

		log.debug("{} test on block device {} with gid {} expecting {}", 
				"testENOSYS", FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.ENOSYS);
		
		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.ENOSYS);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.ENOSYS);
	}

	@Test
	@Category(MockedTests.class)
	public void testENOTBLK() throws NoSuchFieldException, SecurityException, Exception {

		log.debug("{} test on block device {} with gid {} expecting {}", 
				"testENOTBLK", FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.ENOTBLK);
		
		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.ENOTBLK);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.ENOTBLK);
	}

	@Test
	@Category(MockedTests.class)
	public void testEPERM() throws NoSuchFieldException, SecurityException, Exception {

		log.debug("{} test on block device {} with gid {} expecting {}", 
				"testEPERM", FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.EPERM);
		
		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.EPERM);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.EPERM);
	}

	@Test
	@Category(MockedTests.class)
	public void testESRCH() throws NoSuchFieldException, SecurityException, Exception {

		log.debug("{} test on block device {} with gid {} expecting {}", 
				"testESRCH", FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.ESRCH);
		
		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.ESRCH);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.ESRCH);
	}

	@Test
	@Category(MockedTests.class)
	public void testEIO() throws NoSuchFieldException, SecurityException, Exception {

		log.debug("{} test on block device {} with gid {} expecting {}", 
				"testEIO", FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.EIO);
		
		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.EIO);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.EIO);
	}

	@Test
	@Category(MockedTests.class)
	public void testEMFILE() throws NoSuchFieldException, SecurityException, Exception {

		log.debug("{} test on block device {} with gid {} expecting {}", 
				"testEMFILE", FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.EMFILE);
		
		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.EMFILE);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.EMFILE);
	}

	@Test
	@Category(MockedTests.class)
	public void testENODEV() throws NoSuchFieldException, SecurityException, Exception {

		log.debug("{} test on block device {} with gid {} expecting {}", 
				"testENODEV", FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.ENODEV);
		
		PosixQuotaManager pqm = getMockedQuotaManagerFailsWith(ErrNo.ENODEV);
		checkQuotactlFailWith(pqm, FAKE_BLOCKDEVICE, FAKE_GID, ErrNo.ENODEV);
	}

}
