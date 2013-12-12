package saud.ubt.test;

import edu.lehigh.swat.bench.ubt.api.Query;
import edu.lehigh.swat.bench.ubt.api.QueryResult;
import edu.lehigh.swat.bench.ubt.api.Repository;
import edu.lehigh.swat.bench.ubt.RepositoryCreator;

;

public class AbstractTest extends RepositoryCreator {

	/**
	 * @param args
	 */

	private Repository repository_ = null;
	private String className = null;
	private String dataSource = null;
	private String databaseName = null;
	private String ontologyName = null;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getOntologyName() {
		return ontologyName;
	}

	public void setOntologyName(String ontologyName) {
		this.ontologyName = ontologyName;
	}

	public void testQuery(String queryString) {

		Query query = new Query();
		query.setString(queryString);
		QueryResult result;

		repository_ = createRepository(getClassName());
		if (repository_ == null) {
			System.err.println(getClassName() + ": class not found!");
			System.exit(-1);
		}
		repository_.setOntology(getOntologyName());
		repository_.open(getDatabaseName());
		result = repository_.issueQuery(query);
		long resultNum = 0;
		while (result.next() != false) {
			resultNum++;
		}

		repository_.close();
		System.out.println("==================");
		System.out.println("The Result is: " + resultNum);
		System.out.println("==================");
	}

	public void testLoading() {

		Boolean laodResult = false;
		try {
			repository_ = createRepository(getClassName());
			if (repository_ == null) {
				System.err.println(getClassName() + ": class not found!");
				System.exit(-1);
			}

			repository_.setOntology(getClassName());
			repository_.open(getDatabaseName());
			laodResult = repository_.load(getDataSource());
			repository_.close();

			
		} catch (Exception e) {
			laodResult = false;
			e.printStackTrace();
		}

		if (laodResult) {
			System.out.println("Loading is DONE!!");
		} else {
			System.out.println("Error while loading");
		}

	}

}
