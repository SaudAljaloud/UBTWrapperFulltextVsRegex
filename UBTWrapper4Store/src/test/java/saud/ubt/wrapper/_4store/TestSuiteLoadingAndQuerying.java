/**

* Copyright 2012 Saud Aljaloud
* 
* This file is part of UBTWrapperFulltextVsRegex.
* 
* UBTWrapperFulltextVsRegex is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
* 
* UBTWrapperFulltextVsRegex is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License along with UBTWrapperFulltextVsRegex. If not, see http://www.gnu.org/licenses/.
 */
package saud.ubt.wrapper._4store;

import java.io.File;

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
