package de.l3s.ubt.wrapper.yars;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.openrdf.rio.RDFFormat;
import org.semanticweb.yars.api.Connection;
import org.semanticweb.yars.api.DriverManager;
import org.semanticweb.yars.api.N3Exception;
import org.semanticweb.yars.api.ResultSet;
import org.semanticweb.yars.api.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lehigh.swat.bench.ubt.api.Query;
import edu.lehigh.swat.bench.ubt.api.QueryResult;

public class YARSRepository implements edu.lehigh.swat.bench.ubt.api.Repository {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final static File ROOT_DATA_DIR = new File("yars-repositories");
	private final static String REPOSITORY_NAME = "yars";
	// private final static File ONTOLOGY_FILE = new File("univ-bench.owl");

	private Connection con;
	private Statement stmt;
	protected String ontology;

	public void clear() {
		log.debug("clearing repository");

		try {
			this.stmt.executeDelete(null);
		} catch (N3Exception e) {
			log.error("Could not clear repository", e);
			return;
		}

		log.info("cleared repository");
	}

	public void close() {
		log.debug("closing repository");

		try {
			this.stmt.close();
			this.con.close();

			this.stmt = null;
			this.con = null;
		} catch (N3Exception e) {
			log.error("Could not close repository", e);
			return;
		}

		log.info("closed repository");
	}

	public QueryResult issueQuery(Query query) {
		log.debug("querying repository with query\n{}", query.getString());
		try {
			ResultSet rs = stmt.executeQuery(query.getString());
			log.info("queried repository");
			return new YARSQueryResult(rs);
		} catch (Exception e) {
			log.error("could not query repostiory", e);
			return null;
		}
	}

	public boolean load(String dataDir) {
		log.debug("loading data from dir '{}'", dataDir);

		File dir = new File(dataDir);
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.getPath().contains("University")) {
				RDFFormat format = RDFFormat.forFileName(file.getName(),
						RDFFormat.RDFXML);

				// we probably do not get NTRIPLES here, so we convert whatever
				// we get
				// into NTRIPLES using Sesame RIO
				log.debug("loading data from file '{}' as '{}'", file,
						format.toString());
				try {
					log.info(file.getPath());
					InputStream in = null;
					if (format.equals(RDFFormat.NTRIPLES)) {
						in = new FileInputStream(file);
					} else {
						in = new RDFConverterInputStream(file, format,
								RDFFormat.NTRIPLES);
					}

					// add the input stream to the repository
					this.stmt.executeInsert(new InputStreamReader(in));
				} catch (Exception e) {
					log.error(
							"could not load data from file '" + file.getName()
									+ "' as '" + format.toString() + "'", e);
					return false;
				}

				// close the statement to force commit
				try {
					this.stmt.close();
					this.stmt = this.con.createStatement(null);
				} catch (Exception e) {
					log.error(
							"could not close and reopen statement of yars connection",
							e);
					return false;
				}

				log.debug("loaded data from file '{}'", file);
			}
		}

		log.info("loaded data");

		return true;
	}

	public void open(String database) {
		log.debug("opening repository with database '{}'", database);

		// make sure the ROOT_DATA_DIR and the REPOSITORY_NAME folder inside
		// exists
		new File(ROOT_DATA_DIR, REPOSITORY_NAME).mkdirs();

		// try to connect to that folder
		try {
			this.con = DriverManager.getConnection(Connection.FILE_PREFIX
					+ ROOT_DATA_DIR.getAbsolutePath() + File.separator
					+ REPOSITORY_NAME + File.separator + database);
		} catch (N3Exception e) {
			throw new IllegalStateException(
					"Could not connect to the configured YARS store: "
							+ ROOT_DATA_DIR.getAbsolutePath(), e);
		}

		try {
			this.stmt = this.con.createStatement(null);
		} catch (N3Exception e) {
			log.error("Could not create an Statement from the connection", e);
			this.con.close();
			throw new IllegalStateException(
					"Could not create a Statement from the connection", e);
		}

		log.info("opened repository with database '{}'", database);
	}

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

}
