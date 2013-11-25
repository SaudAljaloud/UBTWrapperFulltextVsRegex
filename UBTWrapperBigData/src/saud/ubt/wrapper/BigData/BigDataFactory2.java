package saud.ubt.wrapper.BigData;

import edu.lehigh.swat.bench.ubt.api.Repository;
import edu.lehigh.swat.bench.ubt.api.RepositoryFactory;

public class BigDataFactory2 extends RepositoryFactory {

	

	@Override
	public Repository create() {
		return new BigDataRepository2() {
		};
	}

}
