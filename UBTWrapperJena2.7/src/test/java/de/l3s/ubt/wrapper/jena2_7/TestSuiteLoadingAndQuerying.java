package de.l3s.ubt.wrapper.jena2_7;

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

	    @AfterClass public static void tearDownClass() { 
	        System.out.println("Master tearDown");
	    }


}
