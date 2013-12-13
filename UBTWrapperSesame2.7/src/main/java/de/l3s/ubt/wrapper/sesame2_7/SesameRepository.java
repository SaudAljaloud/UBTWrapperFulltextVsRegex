package de.l3s.ubt.wrapper.sesame2_7;

import java.io.File;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lehigh.swat.bench.ubt.api.Query;
import edu.lehigh.swat.bench.ubt.api.QueryResult;

public abstract class SesameRepository implements
		edu.lehigh.swat.bench.ubt.api.Repository {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final static File ROOT_DATA_DIR = new File("sesame-repositories");
	private final static File ONTOLOGY_FILE = new File("univ-bench.owl");

	protected Repository repo;
	protected RepositoryConnection conn;
	protected String ontology;

	protected void setUp() {
		this.repo = createRepository();
	}

	protected abstract Repository createRepository();

	protected abstract String getName();

	public void clear() {
		log.debug("clearing repository");

		try {
			this.repo.getConnection().clearNamespaces();
		} catch (RepositoryException e) {
			log.error("Could not clear repository", e);
			return;
		}

		log.info("cleared repository");
	}

	public void close() {
		log.debug("closing repository");

		try {
			this.conn.close();
			this.conn = null;
			this.repo.shutDown();
		} catch (RepositoryException e) {
			log.error("Could not close repository", e);
			return;
		}

		log.info("closed repository");
	}

	public QueryResult issueQuery(Query query) {
		log.debug("querying repository with query\n{}", query.getString());
		try {
			TupleQuery q = this.conn.prepareTupleQuery(QueryLanguage.SPARQL,
					query.getString());
			TupleQueryResult r = q.evaluate();
			log.info("queried repository");
			return new SesameQueryResult(r);
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

				log.info("loading data from file '{}' as '{}'", file,
						format.toString());
				try {
					this.conn.begin();
					this.conn.add(file, this.ontology, format);
					this.conn.commit();
				} catch (Exception e) {
					log.error(
							"could not load data from file '" + file.getName()
									+ "' as '" + format.toString() + "'", e);
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

		try {
			this.repo.setDataDir(new File(new File(ROOT_DATA_DIR, getName()),
					database));
			this.repo.initialize();
			this.conn = this.repo.getConnection();
		} catch (RepositoryException e) {
			log.error("Could not open repository", e);
			return;
		}

		// lets check if this repository is already populated
		// if not, load the univ-bench ontology
		try {
			if (this.conn.isEmpty()) {
				try {
					this.conn.add(ONTOLOGY_FILE, this.ontology, RDFFormat
							.forFileName(ONTOLOGY_FILE.getName(),
									RDFFormat.RDFXML), this.conn
							.getValueFactory().createURI(this.ontology));
				} catch (Exception e) {
					log.error(
							"could not load ontology file '"
									+ ONTOLOGY_FILE.getAbsolutePath() + "'", e);
				}
			}
		} catch (RepositoryException e) {
			log.error("could not determine whether repository is empty", e);
		}

		log.info("opened repository with database '{}'", database);
	}

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

}
