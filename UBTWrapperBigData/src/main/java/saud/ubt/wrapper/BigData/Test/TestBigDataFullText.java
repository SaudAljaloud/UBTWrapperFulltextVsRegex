package saud.ubt.wrapper.BigData.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;

public class TestBigDataFullText {

	/**
	 * @param args
	 */

	private static final String FLUSH_FS_CACHE_COMMAND = "purge";

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final static File ROOT_DATA_DIR = new File("Tests");
	private final static File ONTOLOGY_FILE = new File("univ-bench.owl");

	protected Repository repo;
	protected RepositoryConnection conn;
	protected String ontology;
	private String database;

	public void provideFulltext() {
		BigdataSail sail = null;
		try {
			Properties properties = loadProperties("fastload.properties");
			File journal = new File(ROOT_DATA_DIR + "/" + this.database
					+ "/bigdata.jnl");
			properties.setProperty(BigdataSail.Options.FILE,
					journal.getAbsolutePath());
			sail = new BigdataSail(properties);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.repo = new BigdataSailRepository(sail);
	}

	public Properties loadProperties(String resource) throws Exception {
		Properties p = new Properties();
		InputStream is = new FileInputStream(resource);
		p.load(new InputStreamReader(new BufferedInputStream(is)));
		return p;
	}

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

	public void issueQuery(String query) {
		log.debug("querying repository with query\n{}", query);
		try {
			int totalno = 0;
			TupleQuery q = this.conn.prepareTupleQuery(QueryLanguage.SPARQL,
					query);
			TupleQueryResult r = q.evaluate();
			TupleQueryResult r2 = q.evaluate();
			log.info("queried repository");

			ArrayList reslist = new ArrayList();
			while (r.hasNext()) {
				BindingSet b = r.next();
				Set names = b.getBindingNames();
				HashMap hm = new HashMap();
				for (Object n : names) {
					hm.put((String) n, b.getValue((String) n));
				}
				reslist.add(hm);
			}
			System.out.println("========");

			for (Object object : reslist) {
				System.out.println(object);
			}
			System.out.println("========");

			while (r2.hasNext() && r2.next() != null)
				totalno++;

			System.out.println("Total result: " + totalno);
		} catch (Exception e) {
			log.error("could not query repostiory", e);

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

				log.debug("loading data from file '{}' as '{}'", file,
						format.toString());
				try {
					this.conn.setAutoCommit(false);
					this.conn.add(file, this.ontology, RDFFormat.RDFXML);
					this.conn.commit();
				} catch (Exception e) {
					log.error(
							"could not load data from file '" + file.getName()
									+ "' as '" + format.toString() + "'", e);
					e.printStackTrace();
					return false;
				}
				log.debug("loaded data from file '{}'", file);
			}
		}

		log.info("loaded data");

		return true;
	}

	public boolean loadOneFile(String dataDir) {
		log.debug("loading data from dir '{}'", dataDir);

		File file = new File(dataDir);

		if (file.getPath().contains("University")) {
			RDFFormat format = RDFFormat.forFileName(file.getName(),
					RDFFormat.RDFXML);

			log.debug("loading data from file '{}' as '{}'", file,
					format.toString());
			try {
				this.conn.setAutoCommit(false);
				this.conn.add(file, this.ontology, RDFFormat.RDFXML);
				this.conn.commit();
			} catch (Exception e) {
				log.error("could not load data from file '" + file.getName()
						+ "' as '" + format.toString() + "'", e);
				e.printStackTrace();
				return false;
			}
			log.debug("loaded data from file '{}'", file);
		}

		log.info("loaded data");

		return true;
	}

	public void open(String database) {
		log.debug("opening repository with database '{}'", database);
		this.database = database;
		this.provideFulltext();
		try {
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
				this.conn.setAutoCommit(false);
				try {
					this.conn.add(ONTOLOGY_FILE, this.ontology,
							RDFFormat.RDFXML);
					this.conn.commit();
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

	private void flushFSCache() {
		try {
			Process flush = Runtime.getRuntime().exec(FLUSH_FS_CACHE_COMMAND);
			if (flush.waitFor() != 0) {
				System.err
						.println("Could not flush system filesystem caches, ignoring!");
				System.err
						.println("Subsequent queries now may influence each others performance!");
			} else {
				// wait some
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		} catch (IOException e) {
			System.err.println("could not run filesystem cache flush command '"
					+ FLUSH_FS_CACHE_COMMAND + "':");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.err
					.println("interruption while waiting for filesystem cache flush command '"
							+ FLUSH_FS_CACHE_COMMAND + "' to terminate:");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BasicConfigurator.configure();

		TestBigDataFullText t1 = new TestBigDataFullText();
		String op = "";
		t1.provideFulltext();
		t1.setOntology("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl");

		t1.open("");

		if (op.equals("load")) {
			if (t1.load("../../Data/"))
				t1.close();
		} else {

			String q5 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
					+ "PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n"
					+ "prefix bd: <http://www.bigdata.com/rdf/search#>\n"
					+ "SELECT ?X ?rank\n"
					+ "WHERE {\n"
					+ "  ?lit bd:search \"network\" .\n"
					+ "  ?lit bd:rank ?rank .\n"
					+ "  ?lit bd:minRank \"1\" .\n"
					+ "  ?X ub:publicationText ?lit .\n" + "}";
			String q4 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
					+ "PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n"
					+ "prefix bd: <http://www.bigdata.com/rdf/search#>\n"
					+ "SELECT ?X\n"
					+ "WHERE {\n"
					+ "  ?lit bd:search \"engineer*\" .\n"
					+ "  ?X ub:publicationText ?lit .\n" + "}";
			// t1.flushFSCache();
			Date startTime, endTime;
			Runtime runtime = Runtime.getRuntime();
			long duration = 0l;
			startTime = new Date();
			long usedMem = runtime.totalMemory() - runtime.freeMemory();
			t1.issueQuery(q4);
			long usedMem2 = runtime.totalMemory() - runtime.freeMemory();
			endTime = new Date();
			t1.close();
			duration = endTime.getTime() - startTime.getTime();
			System.out.println("the time for execution is: " + duration);
			System.out.println("memory was used: "
					+ ((usedMem2 - usedMem) / 1024) + " kB");

		}

	}

}
