package saud.ubt.wrapper._4store;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.magus.fourstore.client.Store;

import edu.lehigh.swat.bench.ubt.api.QueryResult;

/**
 * Auther: saudaljaloud Email: sza1g10@ecs.soton.ac.uk
 */
public class _4storeRepository implements
		edu.lehigh.swat.bench.ubt.api.Repository {

	private final static Logger log = LoggerFactory
			.getLogger(_4storeRepository.class);
	// private final static File ONTOLOGY_FILE = new File("univ-bench.owl");
	private final static String RDF_FORMAT = "rdfxml";
	private final static String CONFIGFILE = "config-test.ttl";

	private String ontology;
	private String database;
	private String DATABASE_ROOT = "/var/lib/4store/";
	private String sparqlPort = "8000";
	private Store store;
	private String CREATE_DATABASE = "4s-backend-setup";
	private String DATABASE_BACKEND = "4s-backend";
	private String SPARQL_PROTOCOL = "4s-httpd";
	private String IMPORT_DATA = "4s-import";
	Runtime run = Runtime.getRuntime();

	public void create() {

	}

	public void open(String database) {
		this.database = database;
		try {
			if (!new File(DATABASE_ROOT + this.database).exists()) {
				log.debug("give a name to the database: ", this.database);
				Process pr = run.exec(CREATE_DATABASE + " " + this.database);
				pr.waitFor();
			}
			
			log.debug("start the server");
			Process pr2 = run.exec(DATABASE_BACKEND + " " + this.database);
			pr2.waitFor();
			log.debug("Run SPARQL connection on database: " + this.database);
			log.debug("Run SPARQL connection on port: " + sparqlPort);
			Process pr3 = run.exec(SPARQL_PROTOCOL + " -p " + sparqlPort + " "
					+ this.database);
			pr3.waitFor();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean load(String dataDir) {
		// if ("" == null) {
		// log.error("cannot issue query because server connection is not setup properly");
		// return false;
		// }

		try {
			log.debug("stop the sparql protocol!");
			Process pr = run.exec("killall " + SPARQL_PROTOCOL);
			pr.waitFor();
			log.debug("Sparql is stopped!");
			
			log.debug("Loading fulltext conffigration file");
			Process pr4 = run.exec(IMPORT_DATA + " -v " + database 
					 + " -m " + "system:config" + " " + CONFIGFILE);
			pr4.waitFor();
			log.debug("fulltext is configered");

			
			log.debug("loading data from dir '{}'", dataDir);
			File file = new File(dataDir);
			File[] files = file.listFiles();
			String seprator = "";
			for (File file2 : files) {
				if (file2.toString().contains("University")) {
					seprator = seprator + " " + file2.getAbsolutePath();
				}
			}

			Process pr2 = run.exec(IMPORT_DATA + " -v " + database + " -f "
					+ RDF_FORMAT + " -m " + ontology + " " + seprator);
			pr2.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.info("loaded data");
		return true;

	}

	public void clear() {
		log.debug("clearing repository");

		log.info("cleared repository");
	}

	public void close() {
		try {
			log.debug("closing sparql protocol");
			Process pr = run.exec("killall " + SPARQL_PROTOCOL);
			pr.waitFor();
			log.debug("closing the database");
			Process pr2 = run.exec("pkill -f '" + DATABASE_BACKEND +  " " + this.database + "'");
			pr2.waitFor();
			Process proc = new ProcessBuilder("/bin/bash",
					"-c", "pkill", "-f", "'^" + DATABASE_BACKEND + " " + this.database + "$'").start();

			proc.waitFor();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("closed database");

	}

	public QueryResult issueQuer3(edu.lehigh.swat.bench.ubt.api.Query query) {

		// if ("" == null) {
		// log.error("cannot issue query because server connection is not setup properly");
		// return null;
		// }

		log.debug("querying repository with query\n{}", query.getString());

		try {
			Process proc = new ProcessBuilder("/bin/bash", "-c", "4s-query "
					+ this.database + " -s -1 -f json '" + query.getString()
					+ "'").start();
			Reader reader = new InputStreamReader(proc.getInputStream());
			int ch;
			String jsonFromBash = "";
			while ((ch = reader.read()) != -1) {
				jsonFromBash = jsonFromBash + (char) ch;
			}
			reader.close();
			_4storeQueryResult rs = new _4storeQueryResult(jsonFromBash);
			return rs;
		} catch (Exception e) {
			log.error("could not query repostiory", e);
			return null;
		}

	}

	public QueryResult issueQuery(edu.lehigh.swat.bench.ubt.api.Query query) {

		// if ("" == null) {
		// log.error("cannot issue query because server connection is not setup properly");
		// return null;
		// }

		log.debug("querying repository with query\n{}", query.getString());

		try {
			store = new Store("http://localhost:" + sparqlPort);
			String response = store.query(query.getString(),
					Store.OutputFormat.JSON, -1);
			_4storeQueryResult rs = new _4storeQueryResult(response);
			return rs;
		} catch (Exception e) {
			log.error("could not query repostiory", e);
			return null;
		}

	}

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

}
