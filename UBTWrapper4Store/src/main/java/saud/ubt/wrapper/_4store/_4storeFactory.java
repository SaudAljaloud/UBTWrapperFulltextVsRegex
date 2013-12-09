package saud.ubt.wrapper._4store;

import edu.lehigh.swat.bench.ubt.api.Repository;
import edu.lehigh.swat.bench.ubt.api.RepositoryFactory;

/**
Auther: saudaljaloud
Email: sza1g10@ecs.soton.ac.uk
 */
public class _4storeFactory extends RepositoryFactory{

	@Override
	public Repository create() {
		return new _4storeRepository();
	}
	

}
