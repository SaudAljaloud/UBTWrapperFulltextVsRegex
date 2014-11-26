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
package saud.ubt.wrapper.StarDog;

import org.junit.Assert;
import org.junit.Test;

import edu.lehigh.swat.bench.ubt.RepositoryCreator;
import edu.lehigh.swat.bench.ubt.api.Query;
import edu.lehigh.swat.bench.ubt.api.QueryResult;
import edu.lehigh.swat.bench.ubt.api.Repository;

public class TestQuerying extends RepositoryCreator {
	public Repository repository_ = null;

	@Test
	public void testQuerying_BASIC_IR_QUERIES() {

		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n"
				+ "PREFIX arq: <http://jena.hpl.hp.com/ARQ/property#>\n"
				+ "SELECT ?X\n"
				+ "WHERE {\n"
				+ "  ?lit arq:textMatch \"engineer\" .\n"
				+ "  ?X ?p ?lit .\n"
				+ "}";
		long expectedResultSet = 13;

		long actualResult = doquerying(query);
		Assert.assertEquals(expectedResultSet, actualResult);

	}

	@Test
	public void testQuerying_SEMANTIC_IR_QUERIES() {

		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n"
				+ "PREFIX arq: <http://jena.hpl.hp.com/ARQ/property#>\n"
				+ "SELECT ?X\n"
				+ "WHERE {\n"
				+ "  ?lit arq:textMatch \"network\" .\n"
				+ "  ?X ub:publicationText ?lit .\n"
				+ "  ?X rdf:type ub:Publication .\n" + "}";
		long expectedResultSet = 388;
		long actualResult = doquerying(query);
		Assert.assertEquals(expectedResultSet, actualResult);

	}

	@Test
	public void testQuerying_ADVANCED_IR_QUERIES() {
		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n"
				+ "PREFIX arq: <http://jena.hpl.hp.com/ARQ/property#>\n"
				+ "SELECT ?X\n"
				+ "WHERE {\n"
				+ "  ?lit arq:textMatch \"-engineer +network\" .\n"
				+ "  ?X ub:publicationText ?lit .\n" + "}";
		long expectedResultSet = 375;
		long actualResult = doquerying(query);
		Assert.assertEquals(expectedResultSet, actualResult);

	}

	@Test
	public void testQuerying_REGEX() {
		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n"
				+ "SELECT ?X\n"
				+ "WHERE {\n"
				+ "  ?X ?p ?lit .\n"
				+ "	FILTER regex(?lit, \"engineering\")\n" + "}";

		long expectedResultSet = 131;
		long actualResult = doquerying(query);
		Assert.assertEquals(expectedResultSet, actualResult);

	}

	private long doquerying(String queryString) {

		Query query = new Query();
		query.setString(queryString);

		ConfigMock kb = new ConfigMock();

		QueryResult result;
		repository_ = createRepository(kb.className);
		if (repository_ == null) {
			System.err.println(kb.className + ": class not found!");
			System.exit(-1);
		}
		repository_.setOntology(kb.ontologyName);
		repository_.open(kb.databaseName);
		result = repository_.issueQuery(query);
		long resultNum = 0;
		while (result.next() != false) {
			resultNum++;
		}

		repository_.close();
		return resultNum;
	}

	

}
