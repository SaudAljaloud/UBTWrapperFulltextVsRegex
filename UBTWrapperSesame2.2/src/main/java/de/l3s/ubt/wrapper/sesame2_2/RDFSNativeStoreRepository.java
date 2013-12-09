package de.l3s.ubt.wrapper.sesame2_2;

import java.io.File;

import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.nativerdf.NativeStore;

public class RDFSNativeStoreRepository extends SesameRepository {

	private final String indices;
	
	public RDFSNativeStoreRepository(String indices) {
		this.indices = indices;
		super.setUp();
	}

	@Override
	protected Repository createRepository() {
		return new SailRepository(new ForwardChainingRDFSInferencer(new NativeStore(new File("."), this.indices)));
	}

	@Override
	protected String getName() {
		return "rdfs-native-" + this.indices.replaceAll(" ", "-");
	}
	
}
