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


import org.junit.Assert;
import org.junit.Test;


import edu.lehigh.swat.bench.ubt.RepositoryCreator;


import edu.lehigh.swat.bench.ubt.api.Repository;
public class TestLoading extends RepositoryCreator {
	public Repository repository_ = null;


	/**
	 * @param args
	 */
	@Test
	public void testLoadingJena2_7() {

		ConfigMock kb = new ConfigMock();
		Boolean laodResult = false;
		try {
			repository_ = createRepository(kb.className);
			if (repository_ == null) {
				System.err.println(kb.className + ": class not found!");
				System.exit(-1);
			}

			repository_.setOntology(kb.ontologyName);
			repository_.open(kb.databaseName);
			if (!repository_.load(kb.dataSource)) {
				repository_.close();

			}
			laodResult = true;
		} catch (Exception e) {
			laodResult = false;
		}

		Assert.assertEquals(true, laodResult);
		
	}

}
