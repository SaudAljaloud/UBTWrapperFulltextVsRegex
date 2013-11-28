package saud.ubt.wrapper._4store.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saud.ubt.wrapper._4store._4storeQueryResult;
import saud.ubt.wrapper._4store._4storeRepository;
import uk.co.magus.fourstore.client.Store;

public class Test4Store {

	/**
	 * @param args
	 */

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
			Process pr2 = run.exec("pkill -f '" + DATABASE_BACKEND + " "
					+ this.database + "'");
			pr2.waitFor();
			Process proc = new ProcessBuilder("/bin/bash", "-c", "pkill", "-f",
					"'^" + DATABASE_BACKEND + " " + this.database + "$'")
					.start();

			proc.waitFor();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("closed database");

	}

	public void issueQuer3(String query) {

		// if ("" == null) {
		// log.error("cannot issue query because server connection is not setup properly");
		// return null;
		// }

		log.debug("querying repository with query\n{}", query);

		try {
			Process proc = new ProcessBuilder("/bin/bash", "-c", "4s-query "
					+ this.database + " -s -1 -f json '" + query + "'").start();
			Reader reader = new InputStreamReader(proc.getInputStream());
			int ch;
			String jsonFromBash = "";
			while ((ch = reader.read()) != -1) {
				jsonFromBash = jsonFromBash + (char) ch;
			}
			reader.close();
			_4storeQueryResult rs = new _4storeQueryResult(jsonFromBash);

		} catch (Exception e) {
			log.error("could not query repostiory", e);

		}

	}

	public void issueQuery(String query) {

		// if ("" == null) {
		// log.error("cannot issue query because server connection is not setup properly");
		// return null;
		// }

		log.debug("querying repository with query\n{}", query);

		try {
			store = new Store("http://localhost:" + sparqlPort);
			String response = store.query(query, Store.OutputFormat.JSON, -1);
			_4storeQueryResult rs = new _4storeQueryResult(response);
			System.out.println("====================");
			System.out.println(response);
			System.out.println("====================");
			int totalno = 0;
			while (rs.next()) {
				totalno++;
			}
			System.out.println("Total result: " + totalno);

		} catch (Exception e) {
			log.error("could not query repostiory", e);

		}

	}

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Test4Store t1 = new Test4Store();
		System.out.println("hihhhhhhh");
		System.exit(0);
		String op = "";

		t1.setOntology("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl");

		if (args.length > 0) {
			String databaseName = args[1];
			t1.open(databaseName);
			if (op.equals("load") || args[0].equals("load")) {
				if (t1.load("../../Data/"))
					t1.close();
			} else if (op.equals("load") || args[0].equals("query")) {
				String q1 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
						"PREFIX text: <http://4store.org/fulltext#>\n" + 
						"SELECT ?X ?P ?O ?C \n" + 
						"WHERE {\n" + 
						" ?X ?P ?O ?C . " + 
						"}";
				String q2 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
						"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n" + 
						"PREFIX text: <http://4store.org/fulltext#>\n" + 
						"SELECT ?X ?y\n" + 
						"WHERE {\n" + 
						"  ?X text:stem \"engineer\" .\n" +
						"  ?X ub:publicationText ?lit ." + 
						"}" +
						"limit 1000";
				Date startTime, endTime;
				long duration = 0l;
				// t1.flushFSCache();
				startTime = new Date();
				t1.issueQuery(q2);
				endTime = new Date();
				t1.close();
				duration = endTime.getTime() - startTime.getTime();
				System.out.println("the time for execution is: " + duration);

			} else {
				System.err
						.println("Make sure of the argument: laod or query, then a name for the dataabse");
			}
		} else {
			System.err
					.println("Add an argument: laod or query, then a name for the dataabse");
		}
	}

}
