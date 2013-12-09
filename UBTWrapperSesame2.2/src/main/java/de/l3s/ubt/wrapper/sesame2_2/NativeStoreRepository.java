package de.l3s.ubt.wrapper.sesame2_2;

import java.io.File;

import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

public class NativeStoreRepository extends SesameRepository {

	private final String indices;
	
	public NativeStoreRepository(String indices) {
		this.indices = indices;
		super.setUp();
	}

	@Override
	protected Repository createRepository() {
		return new SailRepository(new NativeStore(new File("."), this.indices));
	}

	@Override
	protected String getName() {
		return "native-" + this.indices.replaceAll(" ", "-");
	}

}
