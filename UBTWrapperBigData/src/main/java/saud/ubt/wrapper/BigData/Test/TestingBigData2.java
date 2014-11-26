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
package saud.ubt.wrapper.BigData.Test;

import org.apache.log4j.BasicConfigurator;

import saud.ubt.test.AbstractTest;

public class TestingBigData2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BasicConfigurator.configure();
		ConfigFile kb = new ConfigFile();
		AbstractTest test = new AbstractTest();
		test.setClassName(kb.className);
		test.setDataSource(kb.dataSource);
		test.setDatabaseName(kb.databaseName);
		test.setOntologyName(kb.ontologyName);
		
		
//		test.testLoading();
		
		String q = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n" + 
				"prefix bd: <http://www.bigdata.com/rdf/search#>\n" + 
				"SELECT ?X\n" + 
				"WHERE {\n" + 
				"  ?lit bd:search \"engineer\" .\n" + 
				"  ?X ?p ?lit .\n" + 
				"}";
		test.testQuery(q);
		
//		==================
//				The Result is: 13
//				==================
		
	}

}
