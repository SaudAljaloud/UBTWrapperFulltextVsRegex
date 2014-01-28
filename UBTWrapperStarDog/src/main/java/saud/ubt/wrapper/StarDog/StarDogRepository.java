package saud.ubt.wrapper.StarDog;

import java.io.File;
import java.io.FileInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.rio.RDFFormat;

import com.complexible.common.protocols.server.Server;
import com.complexible.common.protocols.server.ServerException;
import com.complexible.stardog.Stardog;
import com.complexible.stardog.StardogException;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.SelectQuery;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.api.admin.AdminConnectionConfiguration;
import com.complexible.stardog.api.impl.SearchConnectionImpl;
import com.complexible.stardog.api.search.Searcher;
import com.complexible.stardog.protocols.snarl.SNARLProtocolConstants;
import com.complexible.stardog.search.SearchConstants;
import com.complexible.stardog.search.SearchIndex;
import com.complexible.stardog.search.SearchOptions;
import com.complexible.stardog.search.cli.Search;
import com.complexible.stardog.search.cli.SearchCLIModule;
import com.complexible.stardog.search.waldo.SearchLimit;

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
	protected boolean fulltext = true;
	private String database;

	public void provideFulltext() {
		fulltext = true;
	}

	public void clear() {

	}

	public void close() {
		log.debug("closing repository");
		//
		try {
			
			 

			if (aAdminConnection.isOpen()) {
				aAdminConnection.close();
			}
			if (aConn != null && aConn.isOpen()) {
				aConn.close();
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
			aConn = ConnectionConfiguration.to(database)
					.credentials("admin", "admin").connect();
			aConn.begin();
			SelectQuery aQuery = aConn.select(query.getString());
			
			
			TupleQueryResult aResult = aQuery.execute();
			aConn.commit();
			return new StarDogQueryResult(aResult);

		} catch (Exception e) {
			log.error("could not query repostiory", e);
			return null;
		}

	}

	public boolean load(String dataDir) {
		log.debug("loading data from dir '{}'", dataDir);

		File dir = new File(dataDir);
		File[] files = dir.listFiles();
		try {
			aConn = ConnectionConfiguration.to(database)
					.credentials("admin", "admin").connect();

			aConn.begin();

			for (File file : files) {
				if (file.getPath().contains("University")) {
					log.debug("loading data from file '{}' as '{}'", file,
							RDF_FORMAT);
					try {

						aConn.add()
								.io()
								.format(RDFFormat.RDFXML)
								.stream(new FileInputStream(file
										.getAbsoluteFile()));

					} catch (Exception e) {
						log.error(
								"could not load data from file '"
										+ file.getName() + "' as '"
										+ RDF_FORMAT + "'", e);
						return false;
					}

				}
			}
			aConn.commit();
		} catch (StardogException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		log.info("loaded data");

		return true;

	}

	public void open(String database) {
		log.debug("opening repository with database '{}'", database);
		this.database = database;
		if (database != null) {
			try {
				aServer = Stardog.buildServer()
						.bind(SNARLProtocolConstants.EMBEDDED_ADDRESS).start();

				aAdminConnection = AdminConnectionConfiguration
						.toEmbeddedServer().credentials("admin", "admin")
						.connect();
				if (!aAdminConnection.list().contains(database)) {
					log.debug("loading the ontoloy into the database from file: "
							+ ONTOLOGY_FILE);
					aAdminConnection.disk(database).searchable(fulltext)
							.create(ONTOLOGY_FILE);
					log.debug("Closing the adminConnection");
					aAdminConnection.close();
				}
				log.debug("Opening  a user connection");

				
			} catch (StardogException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServerException e) {
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
