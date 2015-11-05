package it.grid.storm.api.filesystem.test;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class PosixQuotaManagerLocalTest {

	private static final Logger log = LoggerFactory.getLogger(PosixQuotaManagerLocalTest.class);

	private static String BLOCKDEVICE = "/dev/sdb";
	private static String MOUNTPOINT = "/storage/test.vo";
	private static int GID = 1003;
	private static int BLOCKHARDLIMIT = 1000;

	private static String FAKE_BLOCKDEVICE = "/dev/fake";
	private static int FAKE_GID = 1000;

	@BeforeClass
	public static void setUpBeforeClass() {

		initCLibrary();
	}

	static void initCLibrary() {

		try {
			setFinalStatic(CLibrary.class.getDeclaredField("INSTANCE"),
					(CLibrary) Native.loadLibrary("c", CLibrary.class));
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

		String blockdevice = BLOCKDEVICE;
		int gid = GID;

		log.debug("{} test on block device {} with gid {} expecting {}", "testLocalSuccess", blockdevice, gid,
				"success");

		PosixQuotaInfo pqi = checkQuotactlSuccess(blockdevice, gid);

		assertTrue(pqi.getBlockHardLimit() == BLOCKHARDLIMIT);
		assertTrue(pqi.getValid() == PosixQuotaInfo.QIF_ALL);
	}

	@Test
	@Category(LocalTests.class)
	public void testLocalFailureEPERM() throws NoSuchFieldException, SecurityException, Exception {

		String blockdevice = BLOCKDEVICE;
		int gid = FAKE_GID;

		log.debug("{} test on block device {} with gid {} expecting {}", "testLocalFailureEPERM", blockdevice, gid,
				ErrNo.EPERM);

		checkQuotactlFailWith(blockdevice, gid, ErrNo.EPERM);
	}

	@Test
	@Category(LocalTests.class)
	public void testLocalFailureENOENT() throws NoSuchFieldException, SecurityException, Exception {

		String blockdevice = FAKE_BLOCKDEVICE;
		int gid = GID;

		log.debug("{} test on block device {} with gid {} expecting {}", "testLocalFailureENOENT", blockdevice, gid,
				ErrNo.ENOENT);

		checkQuotactlFailWith(blockdevice, gid, ErrNo.ENOENT);
	}

	@Test
	@Category(LocalTests.class)
	public void testExceedBlockHardLimit() throws NoSuchFieldException, SecurityException, Exception {

		String blockdevice = BLOCKDEVICE;
		int gid = GID;

		log.debug("{} test on block device {} with gid {} expecting {}", "testExceedBlockHardLimit", blockdevice, gid,
				"success");

		String filename = MOUNTPOINT + "/test.txt";
		File f = new File(filename);
		Path newFile = f.toPath();
		Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrw----");
		FileAttribute<Set<PosixFilePermission>> fileAttrs = PosixFilePermissions.asFileAttribute(perms);
		GroupPrincipal group = newFile.getFileSystem().getUserPrincipalLookupService()
				.lookupPrincipalByGroupName("test.vo");
		BufferedWriter bw = null;
		
		try {
			log.debug("Create filename '{}' ... ", filename);
			Files.createFile(Paths.get(filename), fileAttrs);
			log.debug("File '{}' created!", filename);
			Files.getFileAttributeView(newFile, PosixFileAttributeView.class).setGroup(group);
			log.debug("File '{}' group owner changed to '{}'!", filename, group);
			FileWriter fw = new FileWriter(f.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			log.debug("File is going to be written ...");
			for (long i = 0; i <= 1024 * 1024; i++) {
				bw.write('0');
			}
			bw.close();
			fail("Creation of a big file didn't fail!!");
		} catch (Exception e) {
			log.debug("Exception: {}", e.getMessage());
			assertTrue(e instanceof IOException);
			assertTrue(e.getMessage().contains("Disk quota exceeded"));
		} finally {
			f.delete();
		}
	}

}
