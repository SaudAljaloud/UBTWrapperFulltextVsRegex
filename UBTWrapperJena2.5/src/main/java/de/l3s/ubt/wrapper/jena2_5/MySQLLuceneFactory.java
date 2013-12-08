package de.l3s.ubt.wrapper.jena2_5;

import edu.lehigh.swat.bench.ubt.api.Repository;

public class MySQLLuceneFactory extends JDBCFactory {

	@Override
	public Repository create() {
		JenaRepository repo = new MySQLRepository();
		repo.provideFulltext();
		return repo;
	}

}
