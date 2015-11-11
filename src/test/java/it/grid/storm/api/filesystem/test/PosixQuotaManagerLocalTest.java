package it.grid.storm.api.filesystem.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.LastErrorException;

import it.grid.storm.api.filesystem.quota.posix.ErrNo;
import it.grid.storm.api.filesystem.quota.posix.PosixQuotaManager;
import it.grid.storm.api.filesystem.quota.posix.PosixQuotaException;
import it.grid.storm.api.filesystem.quota.posix.PosixQuotaInfo;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Set;

public class PosixQuotaManagerLocalTest {

	private static final Logger log = LoggerFactory.getLogger(PosixQuotaManagerLocalTest.class);

	private static String BLOCKDEVICE = "/dev/sdb";
	private static String MOUNTPOINT = "/storage/test.vo";
	private static int GID = 1003;
	private static String GROUP = "test.vo";
	private static int BLOCKHARDLIMIT = 10000;
	private static int BLOCKSIZE = 4096;
	
	private static String FAKE_BLOCKDEVICE = "/dev/fake";
	private static int FAKE_GID = 1000;

	private static PosixQuotaManager pqm = new PosixQuotaManager();
	
	private PosixQuotaInfo checkQuotactlSuccess(String blockDevice, int gid) {

		PosixQuotaInfo pqi = null;
		try {

			pqi = pqm.getGroupQuota(blockDevice, gid);

		} catch (PosixQuotaException pqe) {

			log.error("PosixQuotaException: {} - {}", pqe.getMessage(), pqe.getCause());
			fail("Got PosixQuotaException: " + pqe.getMessage());
		}
		return pqi;
	}

	private void checkQuotactlFailWith(String blockDevice, int gid, int errNo) {

		try {

			pqm.getGroupQuota(blockDevice, gid);
			fail("Error " + errNo + " not recognized!");
			
		} catch (PosixQuotaException pqe) {

			log.debug("PosixQuotaException: {}", pqe.getMessage());
			assertTrue(pqe.getCause() instanceof LastErrorException);
			assertTrue(((LastErrorException) pqe.getCause()).getErrorCode() == errNo);
			return;
		}
		
	}

	@Test
	@Category(LocalTests.class)
	public void testLocalSuccess() throws NoSuchFieldException, SecurityException, Exception {

		log.debug("TEST: {}", "testLocalSuccess");

		PosixQuotaInfo pqi = checkQuotactlSuccess(BLOCKDEVICE, GID);
		log.debug("Quota info: {}", pqi);

		assertTrue(pqi.getBlockHardLimit() == BLOCKHARDLIMIT);
		assertTrue(pqi.getValid() == PosixQuotaInfo.QIF_ALL);
	}

	@Test
	@Category(LocalTests.class)
	public void testLocalFailureEPERM() throws NoSuchFieldException, SecurityException, Exception {

		log.debug("TEST: {}", "testLocalFailureEPERM");

		checkQuotactlFailWith(BLOCKDEVICE, FAKE_GID, ErrNo.EPERM);
	}

	@Test
	@Category(LocalTests.class)
	public void testLocalFailureENOENT() throws NoSuchFieldException, SecurityException, Exception {

		log.debug("TEST: {}", "testLocalFailureENOENT");

		checkQuotactlFailWith(FAKE_BLOCKDEVICE, GID, ErrNo.ENOENT);
	}

	
	private void createFile(String filename, int size) throws Exception {
		
		File f = new File(filename);
		Path newFile = f.toPath();
		Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrw----");
		FileAttribute<Set<PosixFilePermission>> fileAttrs = PosixFilePermissions.asFileAttribute(perms);
		GroupPrincipal group = newFile.getFileSystem().getUserPrincipalLookupService()
				.lookupPrincipalByGroupName(GROUP);
		
		log.debug("Create filename '{}' ... ", filename);
		Files.createFile(Paths.get(filename), fileAttrs);
		log.debug("File '{}' created!", filename);
		Files.getFileAttributeView(newFile, PosixFileAttributeView.class).setGroup(group);
		log.debug("File '{}' group owner changed to '{}'!", filename, group);
		
		byte dataToWrite[] = new byte[size];
		Arrays.fill(dataToWrite, (byte) 0);
		FileOutputStream out = new FileOutputStream(filename);
		log.debug("File is going to be written ...");
		try {
			out.write(dataToWrite);
			out.close();
		} catch (Exception e) {
			log.debug("Got exception: {}", e.getMessage());
			out.close();
			throw e;
		}
	}
	
	private void deleteFile(String filename) {
		
		File f = new File(filename);
		if (f.delete()) {
			
			log.debug("File was successfully deleted.");
		
		} else {
	  
			log.error("File was not deleted.");	
		
		}		
	}
	
	@Test
	@Category(LocalTests.class)
	public void testExceedBlockHardLimit() throws NoSuchFieldException, SecurityException, Exception {

		log.debug("TEST: {}", "testExceedBlockHardLimit");
		
		PosixQuotaInfo pqi_start = checkQuotactlSuccess(BLOCKDEVICE, GID);
		log.debug("Quota info: {}", pqi_start);
		
		String filename = MOUNTPOINT + "/testExceedBlockHardLimit.txt";
		try {
			
			createFile(filename, BLOCKHARDLIMIT * 1024);
			fail("Creation of a big file didn't fail!!");
		
		} catch (Exception e) {
		
			assertTrue(e instanceof IOException);
			assertTrue(e.getMessage().contains("Disk quota exceeded"));
		
		} finally {
			
			deleteFile(filename);	
		}
		
		PosixQuotaInfo pqi_end = checkQuotactlSuccess(BLOCKDEVICE, GID);
		log.debug("Quota info: {}", pqi_end);
		assertTrue(pqi_start.getBlockUsage() == pqi_end.getBlockUsage());
		assertTrue(pqi_start.getINodesUsage() == pqi_end.getINodesUsage());
	}
	
	@Test
	@Category(LocalTests.class)
	public void testUpdatedBlockUsage() throws NoSuchFieldException, SecurityException, Exception {

		log.debug("TEST: {}", "testUpdatedBlockUsage");

		PosixQuotaInfo pqi_start = checkQuotactlSuccess(BLOCKDEVICE, GID);
		PosixQuotaInfo pqi_end = null;
		
		log.debug("Quota info before file creation: {}", pqi_start);
		
		String filename = MOUNTPOINT + "/testUpdatedBlockUsage.txt";
		try {
			
			createFile(filename, 1024);
			pqi_end = checkQuotactlSuccess(BLOCKDEVICE, GID);
			log.debug("Quota info after file creation: {}", pqi_end);
			
			//block usage is in kB
			assertTrue(pqi_start.getBlockUsage() == (pqi_end.getBlockUsage() -BLOCKSIZE));
			assertTrue(pqi_start.getINodesUsage() == (pqi_end.getINodesUsage() -1));
			
		} catch (Exception e) {
		
			log.error("Exception: {}", e.getMessage());
			fail("Creation of a 1kB file shouldn't fail!!");
		
		} finally {
			
			deleteFile(filename);
		}
		
		pqi_end = checkQuotactlSuccess(BLOCKDEVICE, GID);
		log.debug("Quota info after file delete: {}", pqi_end);
		assertTrue(pqi_start.getBlockUsage() == pqi_end.getBlockUsage());
		assertTrue(pqi_start.getINodesUsage() == pqi_end.getINodesUsage());
	}

}
