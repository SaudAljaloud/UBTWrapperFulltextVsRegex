package saud.ubt.wrapper.StarDog.Test;

import org.apache.log4j.BasicConfigurator;

import saud.ubt.test.AbstractTest;

public class TestingStarDog2 {

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
		
		
		
		AbstractTest test2 = new AbstractTest();
		test2.setClassName(kb.className);
		test2.setDataSource(kb.dataSource);
		test2.setDatabaseName(kb.databaseName);
		test2.setOntologyName(kb.ontologyName);

//		test.testLoading();

		

		String q2 = "" + 
				"SELECT ?X\n" + 
				"WHERE {\n" + 
				"  ?X ?p ?lit .\n" + 
				"	FILTER regex(?lit, \"engin\")\n" + 
				"}";
		test2.testQuery(q2);
		
		String q = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n" + 
				"SELECT ?X\n" + 
				"WHERE {\n" + 
				"  ?X ?p ?lit .\n" + 
				"	FILTER regex(?lit, \"dfgdfg\")\n" + 
				"}";
		test.testQuery(q);
		
	
		
	}

}
