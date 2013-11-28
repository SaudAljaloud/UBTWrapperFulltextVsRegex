package test.saud.ubt.wrapper.BigData;

import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.junit.Test;

import saud.ubt.wrapper.BigData.Test.TestBigDataFullText;

public class TestLoading extends TestCase {

	@Test
	public void test() {
		Boolean result = false;
		Boolean expectedResult = true;
		TestBigDataFullText t1 = new TestBigDataFullText();
		t1.provideFulltext();
		t1.setOntology("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl");
		t1.open("TEST");
		result = t1.loadOneFile("../../LUBM/LUBM-fulltext-1/University0_0.owl");
		t1.close();
		assertEquals(result, expectedResult);
		
		
	}
	
}
