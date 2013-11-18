package de.l3s.ubt.wrapper.jena2;

import edu.lehigh.swat.bench.ubt.api.Repository;

public class SDBH2LuceneFactory extends JDBCFactory {

	@Override
	public Repository create() {
		JenaRepository repo = new SDBH2Repository();
		repo.provideFulltext();
		return repo;
	}

}
