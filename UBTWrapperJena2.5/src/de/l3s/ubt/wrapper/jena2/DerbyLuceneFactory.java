package de.l3s.ubt.wrapper.jena2;

import edu.lehigh.swat.bench.ubt.api.Repository;

public class DerbyLuceneFactory extends JDBCFactory {

	@Override
	public Repository create() {
		JenaRepository repo = new DerbyRepository();
		repo.provideFulltext();
		return repo;
	}

}
