package de.l3s.ubt.wrapper.yars;

import edu.lehigh.swat.bench.ubt.api.Repository;
import edu.lehigh.swat.bench.ubt.api.RepositoryFactory;

public class YARSFactory extends RepositoryFactory {

	public YARSFactory() {
	}

	@Override
	public Repository create() {
		return new YARSRepository();
	}

}
