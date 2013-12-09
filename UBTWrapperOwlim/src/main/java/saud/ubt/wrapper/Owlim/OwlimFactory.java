package saud.ubt.wrapper.Owlim;

import edu.lehigh.swat.bench.ubt.api.Repository;
import edu.lehigh.swat.bench.ubt.api.RepositoryFactory;

public class OwlimFactory extends RepositoryFactory {

	

	@Override
	public Repository create() {
		return new OwlimAbstractRepository();
	}

}
