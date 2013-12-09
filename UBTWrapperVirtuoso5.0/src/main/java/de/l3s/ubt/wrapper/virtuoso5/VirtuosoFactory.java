package de.l3s.ubt.wrapper.virtuoso5;

import edu.lehigh.swat.bench.ubt.api.Repository;
import edu.lehigh.swat.bench.ubt.api.RepositoryFactory;

public class VirtuosoFactory extends RepositoryFactory {

	@Override
	public Repository create() {
		return new VirtuosoRepository();
	}

}
