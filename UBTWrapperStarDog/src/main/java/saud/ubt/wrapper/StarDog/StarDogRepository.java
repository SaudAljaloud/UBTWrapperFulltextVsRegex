package saud.ubt.wrapper.StarDog;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.query.TupleQueryResult;

import com.complexible.common.protocols.server.Server;
import com.complexible.common.protocols.server.ServerException;
import com.complexible.stardog.Stardog;
import com.complexible.stardog.StardogException;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.SelectQuery;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.api.admin.AdminConnectionConfiguration;
import com.complexible.stardog.protocols.snarl.SNARLProtocolConstants;

import edu.lehigh.swat.bench.ubt.api.Query;
import edu.lehigh.swat.bench.ubt.api.QueryResult;

public class StarDogRepository implements
		edu.lehigh.swat.bench.ubt.api.Repository {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final static String RDF_FORMAT = "RDF/XML";
	private final static File ONTOLOGY_FILE = new File("univ-bench.owl");

	protected Server aServer;
	protected AdminConnection aAdminConnection;
	protected Connection aConn;
	protected String ontology;
	protected boolean fulltext;
	private String database;

	protected void setUp() {
		createRepository();
	}

	public void provideFulltext() {
		fulltext = true;
		this.setUp();
	}

	protected void createRepository() {
		try {
			aServer = Stardog.buildServer()
					.bind(SNARLProtocolConstants.EMBEDDED_ADDRESS).start();

		} catch (ServerException e) {
			e.printStackTrace();
		}

	}

	public void clear() {
		// log.debug("clearing repository");
		//
		// try {
		// this.repo.getConnection().clearNamespaces();
		// } catch (RepositoryException e) {
		// log.error("Could not clear repository", e);
		// return;
		// }
		//
		// log.info("cleared repository");

	}

	public void close() {
		log.debug("closing repository");
		//
		try {
			if (aConn.isOpen()) {
				aConn.close();
			}
			if (aAdminConnection.isOpen()) {
				aAdminConnection.close();
			}
			if (aServer.isRunning()) {
				aServer.stop();
			}
		} catch (Exception e) {
			log.error("Could not close repository", e);
		}
		log.info("closed repository");
	}

	public QueryResult issueQuery(Query query) {
		log.debug("querying repository with query\n{}", query.getString());

		try {

			log.info("queried repository");
			System.out.println(query.getString());
			SelectQuery aQuery = aConn.select(query.getString());
			TupleQueryResult aResult = aQuery.execute();
			return new StarDogQueryResult(aResult);

		} catch (Exception e) {
			log.error("could not query repostiory", e);
			return null;
		}

	}

	public boolean load(String dataDir) {
		log.debug("loading data from dir '{}'", dataDir);

		log.debug("loading the ontoloy into the database from file: "
				+ ONTOLOGY_FILE);
		// try {
		// aAdminConnection.disk(database).create(ONTOLOGY_FILE);
		// } catch (StardogException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }

		File dir = new File(dataDir);
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.getPath().contains("University")) {
				log.debug("loading data from file '{}' as '{}'", file,
						RDF_FORMAT);
				try {

					aAdminConnection.disk(database).searchable(fulltext)
							.create(file.getAbsoluteFile());

				} catch (Exception e) {
					log.error(
							"could not load data from file '" + file.getName()
									+ "' as '" + RDF_FORMAT + "'", e);
					return false;
				}

			}
		}
		log.info("loaded data");

		return true;

	}

	public void open(String database) {
		log.debug("opening repository with database '{}'", database);
		this.database = database;
		if (database != null) {
			try {
				aAdminConnection = AdminConnectionConfiguration
						.toEmbeddedServer().credentials("admin", "admin")
						.connect();

				aConn = ConnectionConfiguration.to(database)
						.credentials("admin", "admin").connect();
				aConn.begin();
			} catch (StardogException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		log.info("opened repository with database '{}'", database);

	}

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

}
