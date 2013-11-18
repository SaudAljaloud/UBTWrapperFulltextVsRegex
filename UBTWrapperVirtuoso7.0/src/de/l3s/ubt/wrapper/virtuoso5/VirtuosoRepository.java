package de.l3s.ubt.wrapper.virtuoso5;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lehigh.swat.bench.ubt.api.QueryResult;

public class VirtuosoRepository implements
		edu.lehigh.swat.bench.ubt.api.Repository {

	private final static Logger log = LoggerFactory
			.getLogger(VirtuosoRepository.class);

	private final static File VIRTUOSO = new File("virtuoso/bin/virtuoso-t");
	private final static File ROOT_DATA_DIR = new File("virtuoso-repositories");
	private final static String REPOSITORY_NAME = "virtuoso";
	private final static int WAIT_FOR_STARTUP; // seconds
	private final static int WAIT_FOR_CONNECT_RETRY; // seconds
	private final static int CONNECT_RETRY_LIMIT; // times
	private final static int WAIT_FOR_SHUTDOWN; // seconds
	// private final static File ONTOLOGY_FILE = new File("univ-bench.owl");
	// private final static String RDF_FORMAT = "RDF/XML";

	static {
		WAIT_FOR_STARTUP = getValue(
				"de.l3s.ubt.wrapper.virtuoso5.WAIT_FOR_STARTUP", "30");
		WAIT_FOR_CONNECT_RETRY = getValue(
				"de.l3s.ubt.wrapper.virtuoso5.WAIT_FOR_CONNECT_RETRY", "10");
		CONNECT_RETRY_LIMIT = getValue(
				"de.l3s.ubt.wrapper.virtuoso5.CONNECT_RETRY_LIMIT", "3");
		WAIT_FOR_SHUTDOWN = getValue(
				"de.l3s.ubt.wrapper.virtuoso5.WAIT_FOR_SHUTDOWN", "3");
	}

	private static int getValue(String key, String def) {
		String valueString = System.getProperty(key, def);
		log.debug("setting " + key + " to " + valueString);
		try {
			int value = Integer.valueOf(valueString);
			return value;
		} catch (NumberFormatException e) {
			log.error("could not parse string of property value '"
					+ valueString + "' into integer", e);
			try {
				int value = Integer.valueOf(def);
				return value;
			} catch (NumberFormatException ee) {
				log.error("could not parse string of default value '" + def
						+ "' into integer", e);
				return -1;
			}
		}
	}

	private Process server;
	private Connection conn;
	private Statement stmt;
	private String ontology;

	// private String database;

	public void clear() {
		log.debug("clearing repository");

		log.info("cleared repository");
	}

	public void close() {
		log.debug("closing repository");

		try {
			if (this.stmt != null) {
				log.info("closing statement of the Virtuoso server");
				this.stmt.close();
				this.stmt = null;
			}

			if (this.conn != null) {
				log.info("closing connection to the Virtuoso server");
				this.conn.close();
				this.conn = null;
			}

			if (this.server != null) {
				log.info("shutting down the Virtuoso server");
				try {
					String[] cmd = { "killall", "-INT", "virtuoso-t" };
					Runtime.getRuntime().exec(cmd);
				} catch (IOException e) {
					log.error("could not killall -INT virtuoso-t", e);
				}

				try {
					log.info("Virtuoso server shut down with exit code {}",
							this.server.waitFor());
					log.info("waiting some to let Virtuoso server really shut down");
					Thread.sleep(WAIT_FOR_SHUTDOWN * 1000);
				} catch (InterruptedException e) {
					log.error(
							"got interrupted while waiting for Virtuoso server to shut down",
							e);
				}

				// log.debug("making sure the Virtuoso server really shut down");
				// try {
				// String[] cmd = {"killall", "-KILL", "virtuoso-t"};
				// Runtime.getRuntime().exec(cmd);
				// } catch (IOException e) {
				// log.error("could not killall -KILL virtuoso-t", e);
				// }

				this.server = null;
			}
		} catch (SQLException e) {
			log.error("could not close repostiory", e);
			return;
		}

		log.info("closed repository");
	}

	public QueryResult issueQuery(edu.lehigh.swat.bench.ubt.api.Query query) {
		if (this.stmt == null) {
			log.error("cannot issue query because server connection is not setup properly");
			return null;
		}

		log.debug("querying repository with query\n{}", query.getString());

		try {
			log.info("queried repository");
			ResultSet rs = this.stmt
					.executeQuery("sparql " + query.getString());
			return new VirtuosoQueryResult(rs);
		} catch (Exception e) {
			log.error("could not query repostiory", e);
			return null;
		}
	}

	public boolean load(String dataDir) {
		if (this.stmt == null) {
			log.error("cannot issue query because server connection is not setup properly");
			return false;
		}

		log.debug("loading data from dir '{}'", dataDir);

		// load all files
		File dir = new File(dataDir);
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.getPath().contains("University")) {
				try {
					log.debug("loading data from file '{}' as 'RDFXML'", file);
					this.stmt
							.execute("DB.DBA.RDF_LOAD_RDFXML (file_to_string ('"
									+ file.getAbsolutePath()
									+ "'), '"
									+ this.ontology
									+ "', '"
									+ this.ontology
									+ "')");
				} catch (Exception e) {
					log.error(
							"could not load data from file '" + file.getName()
									+ "' as 'RDFXML'", e);
					return false;
				}
				log.debug("loaded data from file '{}'", file);
			}
		}

		// start fulltext indexing
		log.debug("starting fulltext indexing");
		try {
			this.stmt
					.execute("DB.DBA.RDF_OBJ_FT_RULE_ADD ('http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl', null, 'fulltext-indexing-lubm')");
			this.stmt.execute("DB.DBA.VT_INC_INDEX_DB_DBA_RDF_OBJ ()");
		} catch (SQLException e) {
			log.error("could not start fultext indexing", e);
			return false;
		}
		log.debug("fulltext indexing complete");

		// make sure everything is committed
		try {
			this.conn.commit();
		} catch (SQLException e) {
			log.error("error while finally commiting loaded data", e);
			return false;
		}

		log.info("loaded data");
		return true;
	}

	public void open(String database) {
		log.debug("opening repository with database '{}'", database);

		// memorize the database
		// this.database = database;

		// make sure the repository dir exists
		File dir = new File(new File(ROOT_DATA_DIR, REPOSITORY_NAME), database);
		dir.mkdirs();

		// first we start the server
		try {
			log.info("starting the Virtuoso server");
			ProcessBuilder pb = new ProcessBuilder(VIRTUOSO.getAbsolutePath(),
					"+foreground");
			pb.redirectErrorStream(true);
			pb.directory(dir);
			server = pb.start();

			// write stdout and stderr to log using dedicated threads
			new InputStreamLoggerThread(server.getInputStream(), log).start();
			new InputStreamLoggerThread(server.getErrorStream(), log, true)
					.start();
		} catch (IOException e) {
			log.error("could not start virtuoso server", e);
			return;
		}

		try {
			log.info("wating {}s to let the Virtuoso server startup",
					WAIT_FOR_STARTUP);
			Thread.sleep(WAIT_FOR_STARTUP * 1000);
		} catch (InterruptedException e) {
			log.error("wating was interrupted", e);
			return;
		}

		try {
			log.info("connecting to the Virtuoso server");
			Class.forName("virtuoso.jdbc4.Driver");

			int retries = CONNECT_RETRY_LIMIT;
			do {
				try {
					// try to connect
					this.conn = DriverManager
							.getConnection("jdbc:virtuoso://localhost:1111/UID=dba/PWD=dba");

					// here we succeeded, so no more retries
					retries = -1;
				} catch (SQLException e) {
					// make sure the connection is not made
					this.conn = null;

					// check if the server process is still running
					try {
						// get the exist status of the server, throws exception
						// if not yet terminated
						int status = this.server.exitValue();

						// server terminated already
						log.error("Virtuoso server already terminated with status code '"
								+ status + "'!");
						retries = 0;
					} catch (IllegalThreadStateException ee) {
						// ignore, everything is fine here
					}

					// here it did not succeed, count down retries
					retries--;
					if (retries < 0) {
						log.error("could not open repository, no more retries",
								e);
						return;
					}
					log.debug("could not open repository, will retry in "
							+ WAIT_FOR_CONNECT_RETRY + "s", e);

					try {
						Thread.sleep(WAIT_FOR_CONNECT_RETRY * 1000);
					} catch (InterruptedException e2) {
						log.error(
								"got interrupted while waiting until the next retry",
								e);
					}
				}
			} while (retries >= 0);

			this.stmt = conn.createStatement();
		} catch (SQLException e) {
			log.error("could not open repository", e);
			return;
		} catch (ClassNotFoundException e) {
			log.error("could not load virtuoso jdbc driver", e);
			return;
		}

		log.info("opened repository with database '{}'", database);
	}

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

}
