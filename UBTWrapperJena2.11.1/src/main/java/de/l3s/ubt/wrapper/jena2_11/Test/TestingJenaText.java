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
package de.l3s.ubt.wrapper.jena2_11.Test;

import org.apache.log4j.BasicConfigurator;

import edu.lehigh.swat.bench.ubt.RepositoryCreator;
import edu.lehigh.swat.bench.ubt.api.Query;
import edu.lehigh.swat.bench.ubt.api.QueryResult;
import edu.lehigh.swat.bench.ubt.api.Repository;

public class TestingJenaText extends RepositoryCreator {

	
	public Repository repository_ = null;
	String className = "de.l3s.ubt.wrapper.jena2_11.TDBLuceneFactory";
	String dataSource = "../../LUBM/LUBM-fulltext-1/";
	String databaseName = "LUBM-fulltext-1";
	String ontologyName  = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		BasicConfigurator.configure();
		TestingJenaText test = new TestingJenaText();
		String q = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n" + 
				"PREFIX text: <http://jena.apache.org/text#>\n" + 
				"SELECT ?X\n" + 
				"WHERE {\n" + 
				"  ?X text:query \"engineer~0.8\" .\n" + 
				"  ?X ub:publicationText ?lit .\n" + 
				"}";
		
		
		long result = test.testQuery(q);
		System.out.println("Number of Results: " + result);
		
		
//		test.testLoading();
		
		
	}

	public long testQuery(String queryString) {
		
		Query query = new Query();
		query.setString(queryString);
		QueryResult result;

		repository_ = createRepository(className);
		if (repository_ == null) {
			System.err.println(className + ": class not found!");
			System.exit(-1);
		}
		repository_.setOntology(ontologyName);
		repository_.open(databaseName);
		result = repository_.issueQuery(query);
		long resultNum = 0;
		while (result.next() != false) {
			resultNum++;
		}

		repository_.close();
		return resultNum;
	}
	
	public void testLoading() {

		Boolean laodResult = false;
		try {
			repository_ = createRepository(className);
			if (repository_ == null) {
				System.err.println(className + ": class not found!");
				System.exit(-1);
			}

			repository_.setOntology(ontologyName);
			repository_.open(databaseName);
			if (!repository_.load(dataSource)) {
				repository_.close();

			}
			laodResult = true;
		} catch (Exception e) {
			laodResult = false;
		}

		if (laodResult){
			System.out.println("Loading is DONE!!");
		}else{
			System.out.println("Error while loading");
		}
		
	}


}
