package it.grid.storm.api.filesystem.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.Test;
import org.mockito.Mockito;

import com.sun.jna.LastErrorException;
import com.sun.jna.Structure;

import it.grid.storm.api.filesystem.quota.posix.CLibrary;
import it.grid.storm.api.filesystem.quota.posix.PosixQuotaManager;
import it.grid.storm.api.filesystem.quota.posix.PosixQuotaException;

public class PosixQuotaManagerTest {

	static void setFinalStatic(Field field, Object newValue) throws Exception {
		
		field.setAccessible(true);
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		field.set(null, newValue);
	}

	private String getQuotactlErrorMsg(int errNo) {

		switch (errNo) {

		case CLibrary.EFAULT:
			return String.format("[%d] addr or special is invalid.", CLibrary.EFAULT);
		case CLibrary.EINVAL:
			return String.format("[%d] cmd or type is invalid.", CLibrary.EINVAL);
		case CLibrary.ENOENT:
			return String.format("[%d] The file specified by special or addr does not exist.", CLibrary.ENOENT);
		case CLibrary.ENOSYS:
			return String.format("[%d] The kernel has not been compiled with the CONFIG_QUOTA option.",
					CLibrary.ENOSYS);
		case CLibrary.ENOTBLK:
			return String.format("[%d] special is not a block device.", CLibrary.ENOTBLK);
		case CLibrary.EPERM:
			return String.format(
					"[%d] The caller lacked the required privilege (CAP_SYS_ADMIN) for the specified operation.",
					CLibrary.EPERM);
		case CLibrary.ESRCH:
			return String.format(
					"[%d] No disk quota is found for the indicated user. Quotas have not been turned on for this filesystem.",
					CLibrary.ESRCH);
		default:
			return "";
		}
	}

	private void checkQuotactlFailWith(int errNo) {

		CLibrary mockedCLib = mock(CLibrary.class);
		LastErrorException e = new LastErrorException(getQuotactlErrorMsg(errNo));
		Mockito.when(
				mockedCLib.quotactl(any(Integer.class), any(String.class), any(Integer.class), any(Structure.class)))
				.thenThrow(e);
		try {
			setFinalStatic(PosixQuotaManager.class.getDeclaredField("cLib"), mockedCLib);
		} catch (Throwable t) {
			t.printStackTrace();
			fail(t.getMessage());
		}
		PosixQuotaManager pqm = new PosixQuotaManager();

		try {

			pqm.getQuotaInfo("/path/to/blockdevice", 500);

		} catch (PosixQuotaException pqe) {

			assertTrue(pqe.getCause() instanceof LastErrorException);
			assertTrue(((LastErrorException) pqe.getCause()).getErrorCode() == errNo);
			return;
		}

		fail("Error " + errNo + " not recognized!");
	}

	@Test
	public void testEFAULT() throws NoSuchFieldException, SecurityException, Exception {

		checkQuotactlFailWith(CLibrary.EFAULT);
	}

	@Test
	public void testEINVAL() throws NoSuchFieldException, SecurityException, Exception {

		checkQuotactlFailWith(CLibrary.EINVAL);
	}

	@Test
	public void testENOENT() throws NoSuchFieldException, SecurityException, Exception {

		checkQuotactlFailWith(CLibrary.ENOENT);
	}

	@Test
	public void testENOSYS() throws NoSuchFieldException, SecurityException, Exception {

		checkQuotactlFailWith(CLibrary.ENOSYS);
	}

	@Test
	public void testENOTBLK() throws NoSuchFieldException, SecurityException, Exception {

		checkQuotactlFailWith(CLibrary.ENOTBLK);
	}

	@Test
	public void testEPERM() throws NoSuchFieldException, SecurityException, Exception {

		checkQuotactlFailWith(CLibrary.EPERM);
	}

	@Test
	public void testESRCH() throws NoSuchFieldException, SecurityException, Exception {

		checkQuotactlFailWith(CLibrary.ESRCH);
	}

}
