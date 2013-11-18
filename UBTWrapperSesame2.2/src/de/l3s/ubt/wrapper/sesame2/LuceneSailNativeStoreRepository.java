package de.l3s.ubt.wrapper.sesame2;

import java.io.File;

import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.lucene.LuceneSail;
import org.openrdf.sail.nativerdf.NativeStore;

public class LuceneSailNativeStoreRepository extends SesameRepository {

	private final String indices;
	
	public LuceneSailNativeStoreRepository(String indices) {
		this.indices = indices;
		super.setUp();
	}

	@Override
	protected Repository createRepository() {
		Sail sail = new NativeStore(new File("."), this.indices);
		LuceneSail luceneSail = new LuceneSail();
		luceneSail.setParameter(LuceneSail.LUCENE_DIR_KEY, "nativestore.index");
		luceneSail.setBaseSail(sail);
		return new SailRepository(luceneSail);
	}

	@Override
	protected String getName() {
		String flavor = "";
		try {
			Class.forName("org.openrdf.sail.lucene.LuceneHitsSet");
			flavor = "hits-set";
		} catch (ClassNotFoundException e) {
			try {
				Class.forName("org.openrdf.sail.lucene.LuceneTripleSource");
				flavor = "triple-source";
			} catch (ClassNotFoundException ee) {
				flavor = "orig";
			}
		}
		return "lucenesail-" + flavor + "-native-" + this.indices.replaceAll(" ", "-");
	}

}
