package de.l3s.ubt.wrapper.sesame2_7.Test;

import org.apache.log4j.BasicConfigurator;

import saud.ubt.test.AbstractTest;

public class TestingSesame2_7_Sec {

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
				"PREFIX ls: <http://www.openrdf.org/contrib/lucenesail#>\n" + 
				"SELECT ?X\n" + 
				"WHERE {\n" + 
				"  ?X ls:matches [\n" + 
				"    rdf:type ls:LuceneQuery ;\n" + 
				"    ls:query \"engineer\"\n" + 
				"  ]\n" + 
				"}";
		test.testQuery(q);
		
//		==================
//				The Result is: 13
//				==================
		
	}

}
