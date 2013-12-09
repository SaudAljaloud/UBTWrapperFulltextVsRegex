package de.l3s.ubt.wrapper.sesame2_7;

import edu.lehigh.swat.bench.ubt.api.Repository;
import edu.lehigh.swat.bench.ubt.api.RepositoryFactory;

public class NativeStoreFactory extends RepositoryFactory {

	public NativeStoreFactory() {
	}

	@Override
	public Repository create() {
		return new NativeStoreRepository("spoc posc ospc");
	}

}
