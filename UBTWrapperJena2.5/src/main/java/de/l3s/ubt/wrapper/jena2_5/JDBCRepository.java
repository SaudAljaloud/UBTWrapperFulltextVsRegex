package de.l3s.ubt.wrapper.jena2_5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

public abstract class JDBCRepository extends PersistentRepository {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private IDBConnection jdbc = null;
	
	protected abstract String type();
	protected abstract String driver();
	protected abstract String jdbcURL(String... names);

	@Override
	protected String getName() {
		return super.getName() + "-jdbc";
	}

	@Override
	protected Model createModel(String... names) {
		// get the JDBC configuration
		String url = jdbcURL(names);
		String user = "sa";
		String pass = "";
		String type = type();
		String driver = driver();

		// load the the driver class
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			// log the error, and throw it as an illegal state exception
			log.error("Could not find the given jdbc driver '" + driver + "'!", e);
			throw new IllegalStateException("Could not find the given jdbc driver '" + driver + "'!", e);
		}

		// get the database connection
		this.jdbc = new DBConnection(url, user, pass, type);

		// we need a model maker with the given connection parameters
		ModelMaker maker = ModelFactory.createModelRDBMaker(this.jdbc);

		// and finally we create the default model
		Model model = null;
		try {
			model = maker.createDefaultModel();
		} catch (Exception e) {
			// log the error, and throw it as an illegal argument exception
			log.error("Something with the DB connection went wrong!", e);
			throw new IllegalArgumentException("Something with the DB connection went wrong!", e);
		}
		
		return model;
	}

}
