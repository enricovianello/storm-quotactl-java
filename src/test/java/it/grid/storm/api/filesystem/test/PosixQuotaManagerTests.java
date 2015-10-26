package it.grid.storm.api.filesystem.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.Test;
import org.mockito.Mockito;

import com.sun.jna.LastErrorException;
import com.sun.jna.Structure;

import it.grid.storm.api.filesystem.quota.QuotaException;
import it.grid.storm.api.filesystem.quota.posix.CLibrary;
import it.grid.storm.api.filesystem.quota.posix.PosixInputData;
import it.grid.storm.api.filesystem.quota.posix.PosixQuotaManager;


public class PosixQuotaManagerTests {

	static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);        
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
	
	@Test
	public void testEFAULT() throws NoSuchFieldException, SecurityException, Exception {
		
		System.out.println("testEFAULT");
		CLibrary mockedCLib = mock(CLibrary.class);
		String emsg = String.format("[%d] addr or special is invalid.", CLibrary.EFAULT);
		Mockito.when(mockedCLib.quotactl(any(Integer.class), any(String.class), any(Integer.class), any(Structure.class))).thenThrow(new LastErrorException(emsg));
		setFinalStatic(PosixQuotaManager.class.getDeclaredField("cLib"), mockedCLib);
		PosixQuotaManager pqm = new PosixQuotaManager();
		try {
			pqm.getQuotaInfo(new PosixInputData("/path/to/blockdevice", 500));
		} catch (QuotaException e) {
			System.out.println("OK");
			return;
		}
		fail("EFAULT error not recognized!");
	}

}
