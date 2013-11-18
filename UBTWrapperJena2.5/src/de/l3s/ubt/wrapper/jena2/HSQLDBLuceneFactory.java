package de.l3s.ubt.wrapper.jena2;

import edu.lehigh.swat.bench.ubt.api.Repository;

public class HSQLDBLuceneFactory extends JDBCFactory {

	@Override
	public Repository create() {
		JenaRepository repo = new HSQLDBRepository();
		repo.provideFulltext();
		return repo;
	}

}
