package it.grid.storm.api.filesystem.test;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;

import it.grid.storm.api.filesystem.quota.posix.CLibrary;
import it.grid.storm.api.filesystem.quota.posix.ErrNo;
import it.grid.storm.api.filesystem.quota.posix.PosixQuotaManager;
import it.grid.storm.api.filesystem.quota.posix.PosixQuotaException;
import it.grid.storm.api.filesystem.quota.posix.PosixQuotaInfo;

public class PosixQuotaManagerLocalTest {

	private static final Logger log = LoggerFactory.getLogger(PosixQuotaManagerLocalTest.class);

	private static String FAKE_BLOCKDEVICE = "/dev/fake";
	private static int FAKE_GID = 1000;

	@BeforeClass
	public static void setUpBeforeClass() {
		
		initCLibrary();
	}
	
    static void initCLibrary() {
		
		try {
			setFinalStatic(CLibrary.class.getDeclaredField("INSTANCE"), (CLibrary) Native.loadLibrary("c", CLibrary.class));
		} catch (Throwable t) {
			t.printStackTrace();
			fail(t.getMessage());
		}
	}
    
    static void setFinalStatic(Field field, Object newValue) throws Exception {

		field.setAccessible(true);
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		field.set(null, newValue);
	}

	private PosixQuotaInfo checkQuotactlSuccess(String blockDevice, int gid) {

		PosixQuotaManager pqm = new PosixQuotaManager();
		PosixQuotaInfo pqi = null;
		try {

			pqi = pqm.getGroupQuota(blockDevice, gid);

		} catch (PosixQuotaException pqe) {

			log.error("PosixQuotaException: {} - {}", pqe.getMessage(), pqe.getCause());
			fail("It shouldn't fail!");
		}
		return pqi;
	}

	private void checkQuotactlFailWith(String blockDevice, int gid, int errNo) {

		PosixQuotaManager pqm = new PosixQuotaManager();
		
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
		
		PosixQuotaInfo pqi = checkQuotactlSuccess(blockdevice, gid);
		
		assertTrue(pqi.getBlockHardLimit() == 1000);
		assertTrue(pqi.getValid() == PosixQuotaInfo.QIF_ALL);
	}
	
	@Test
	@Category(LocalTests.class)
	public void testLocalFailureEPERM() throws NoSuchFieldException, SecurityException, Exception {

		String blockdevice = "/dev/sdb";
		int gid = FAKE_GID;

		log.debug("{} test on block device {} with gid {} expecting {}", 
				"testLocalFailureEPERM", blockdevice, gid, ErrNo.EPERM);
		
		checkQuotactlFailWith(blockdevice, gid, ErrNo.EPERM);
	}
	
	@Test
	@Category(LocalTests.class)
	public void testLocalFailureENOENT() throws NoSuchFieldException, SecurityException, Exception {

		String blockdevice = FAKE_BLOCKDEVICE;
		int gid = 1002;

		log.debug("{} test on block device {} with gid {} expecting {}", 
				"testLocalFailureENOENT", blockdevice, gid, ErrNo.ENOENT);
		
		checkQuotactlFailWith(blockdevice, gid, ErrNo.ENOENT);
	}

}
