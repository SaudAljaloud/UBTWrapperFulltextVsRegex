package de.l3s.ubt.wrapper.jena2_7;

import java.io.File;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;

public class TDBRepository extends PersistentRepository {

	@Override
	protected String getName() {
		return super.getName() + "-tdb";
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Model createModel(String... names) {
		// build up the path again
		StringBuilder string = new StringBuilder();
		string.append(File.separator);
		for(String name : names) {
			string.append(name);
			string.append(File.separator);
		}

		// make sure the directory exists
		new File(string.toString()).mkdirs();
		
		// use that path for the model backend
		return TDBFactory.createModel(string.toString()) ;
	}

}
