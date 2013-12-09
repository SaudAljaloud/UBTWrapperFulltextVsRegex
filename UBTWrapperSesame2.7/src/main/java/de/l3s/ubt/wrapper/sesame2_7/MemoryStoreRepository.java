package de.l3s.ubt.wrapper.sesame2_7;

import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

public class MemoryStoreRepository extends SesameRepository {

	public MemoryStoreRepository() {
	}

	@Override
	protected Repository createRepository() {
		return new SailRepository(new MemoryStore());
	}

	@Override
	protected String getName() {
		return "memory";
	}

}
