package de.l3s.ubt.wrapper.sesame2;

import edu.lehigh.swat.bench.ubt.api.Repository;
import edu.lehigh.swat.bench.ubt.api.RepositoryFactory;

public class RDFSNativeStoreFactory extends RepositoryFactory {

	public RDFSNativeStoreFactory() {
	}

	@Override
	public Repository create() {
		return new RDFSNativeStoreRepository("spoc posc ospc");
	}

}
