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

//		test.testLoading();

		String q = "select * where { ?s ?p ?o } limit 10";
		test.testQuery(q);

		
	}

}
