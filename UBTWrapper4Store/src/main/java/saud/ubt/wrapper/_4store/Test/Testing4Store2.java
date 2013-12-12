package saud.ubt.wrapper._4store.Test;

import saud.ubt.test.AbstractTest;

public class Testing4Store2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ConfigFile kb = new ConfigFile();
		AbstractTest test = new AbstractTest();
		test.setClassName(kb.className);
		test.setDataSource(kb.dataSource);
		test.setDatabaseName(kb.databaseName);
		test.setOntologyName(kb.ontologyName);
		
		
//		test.testLoading();
		
		String q = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n" + 
				"PREFIX text: <http://4store.org/fulltext#>\n" + 
				"SELECT ?X\n" + 
				"WHERE {\n" + 
				"  ?X text:token \"engineer\" .\n" + 
				"  ?X ub:publicationText ?lit .\n" + 
				"}";
		test.testQuery(q);
		
		
		
	}

}
