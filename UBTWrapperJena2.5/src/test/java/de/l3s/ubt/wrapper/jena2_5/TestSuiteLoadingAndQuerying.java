package de.l3s.ubt.wrapper.jena2_5;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TestLoading.class, TestQuerying.class })
public class TestSuiteLoadingAndQuerying {

	@BeforeClass
	public static void setUpClass() {
		System.out.println("Master setup");

	}

	@AfterClass
	public static void tearDownClass() {
		System.out.println("Master tearDown");
		ConfigMock kb = new ConfigMock();
		File directory = new File("jena-repositories/persistent-tdb-fulltext/"
				+ kb.databaseName);
		File directory2 = new File("jena-repositories/persistent-tdb-fulltext/"
				+ kb.databaseName + ".index/");
		File[] files = directory.listFiles();
		if (files != null) {
			for (File f : files) {
				f.deleteOnExit();
			}
		}
		directory.delete();

		File[] files2 = directory2.listFiles();
		if (files2 != null) {
			for (File f2 : files2) {
				f2.deleteOnExit();
			}
		}
		directory2.delete();

	}

}
