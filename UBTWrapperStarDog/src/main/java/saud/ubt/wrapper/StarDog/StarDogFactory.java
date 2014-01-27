package saud.ubt.wrapper.StarDog;

import edu.lehigh.swat.bench.ubt.api.Repository;
import edu.lehigh.swat.bench.ubt.api.RepositoryFactory;

public class StarDogFactory extends RepositoryFactory {

	

	@Override
	public Repository create() {
		StarDogRepository repo = new StarDogRepository();
		repo.provideFulltext();
		return repo;
	}

}
