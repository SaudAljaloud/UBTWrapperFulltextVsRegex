package de.l3s.ubt.wrapper.sesame2;

import edu.lehigh.swat.bench.ubt.api.Repository;
import edu.lehigh.swat.bench.ubt.api.RepositoryFactory;

public class LuceneSailNativeStoreFactory extends RepositoryFactory {

	public LuceneSailNativeStoreFactory() {
	}

	@Override
	public Repository create() {
		return new LuceneSailNativeStoreRepository("spoc posc ospc");
	}

}
