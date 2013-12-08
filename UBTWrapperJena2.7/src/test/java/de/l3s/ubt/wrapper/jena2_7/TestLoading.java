package de.l3s.ubt.wrapper.jena2_7;


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
