package de.l3s.ubt.wrapper.jena2.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.larq.IndexBuilderModel;
import com.hp.hpl.jena.query.larq.IndexBuilderString;
import com.hp.hpl.jena.query.larq.IndexLARQ;
import com.hp.hpl.jena.query.larq.LARQ;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

public class FullTextTest {

	/**
	 * @param args
	 */
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final static File ROOT_DATA_DIR = new File(
			"jena-repositories");
	// private final static File ONTOLOGY_FILE = new File("univ-bench.owl");
	private final static String RDF_FORMAT = "RDF/XML";
	private static final String FLUSH_FS_CACHE_COMMAND = "purge";

	private boolean fulltext;
	private Model model;
	private String ontology;
	private String database;

	private IndexLARQ index;

	protected Model createModel(String... names) {
		// build up the path again
		StringBuilder string = new StringBuilder();
		string.append(File.separator);
		for (String name : names) {
			string.append(name);
			string.append(File.separator);
		}

		// make sure the directory exists
		new File(string.toString()).mkdirs();

		// use that path for the model backend
		return TDBFactory.createModel(string.toString());
	}

	protected String getName() {
		return "1" + "-tdb";
	}

	public void provideFulltext() {
		this.fulltext = true;
	}

	public void clear() {
		log.debug("clearing repository");

		this.model.removeAll();

		log.info("cleared repository");
	}

	public void close() {
		log.debug("closing repository");

		// if we have fulltext support, first close that index
		if (fulltext) {
			if (this.index != null) {
				this.index.close();
				this.index = null;
			}
		}

		// finally, close our model
		this.model.close();

		log.info("closed repository");
	}

	public void issueQuery(String query) {
		log.debug("querying repository with query\n{}", query);

		if (fulltext) {
			// if there is no index yet, create one
			if (this.index == null) {
				try {
					Directory dir = FSDirectory.getDirectory(getFulltextDir());
					this.index = new IndexLARQ(IndexReader.open(dir));
					LARQ.setDefaultIndex(this.index);
				} catch (IOException e) {
					log.error(
							"could create LARQ index to support fulltect on query time",
							e);

				}
			}
		}

		try {
			log.info("queried repository");
			int totalno = 0;
			Query q = QueryFactory.create(query);
			QueryExecution qe = QueryExecutionFactory.create(q, model);
			QueryExecution qe2 = QueryExecutionFactory.create(q, model);
			ResultSet rs = qe.execSelect();
			ResultSet rs2 = qe2.execSelect();
			ResultSetFormatter.out(System.out, rs) ;
			while(rs2.hasNext() && rs2.next()!=null) totalno++;
			
			System.out.println("No of Result: " + totalno);
		} catch (Exception e) {
			log.error("could not query repostiory", e);

		}
	}

	public boolean load(String dataDir) {
		log.debug("loading data from dir '{}'", dataDir);

		// if fulltext support is requested, add LARQ to it
		IndexBuilderModel larqBuilder = null;
		if (this.fulltext)
			larqBuilder = addFulltextSupport(this.model);

		File dir = new File(dataDir);
		File[] files = dir.listFiles();
		for (File file : files) {
			if (!file.toString().contains(".DS_Store")) {
				log.debug("loading data from file '{}' as '{}'", file,
						RDF_FORMAT);
				try {
					this.model.begin();
					FileManager.get().readModel(this.model,
							file.getAbsolutePath(), this.ontology, RDF_FORMAT);
					this.model.commit();
				} catch (Exception e) {
					log.error(
							"could not load data from file '" + file.getName()
									+ "' as '" + RDF_FORMAT + "'", e);
					return false;
				}
				log.debug("loaded data from file '{}'", file);
			}
		}

		// if the data dir was read, we can close the builder
		if (fulltext) {
			// close but don't optimize here, so that it is comparable to
			// LuceneSail
			larqBuilder.closeWriter();
			this.model.unregister(larqBuilder);
		}

		log.info("loaded data");

		return true;
	}

	public void open(String database) {
		log.debug("opening repository with database '{}'", database);

		// memorize the database
		this.database = database;

		// get the actual model implementation from sub classes
		this.model = createModel(getNames());

		log.info("opened repository with database '{}'", database);
	}

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

	private String[] getNames() {
		String name = getRepositoryName();

		// put the path to the database into a list of strings
		List<String> names = new ArrayList<String>(Arrays.asList(ROOT_DATA_DIR
				.getAbsolutePath().split(File.separator)));
		names.add(name);
		names.add(database);

		// return String[] of names
		return names.toArray(new String[0]);
	}

	private IndexBuilderModel addFulltextSupport(Model model) {
		// create index builder
		IndexBuilderModel larqBuilder = new IndexBuilderString(getFulltextDir());

		// register the index builder at the indexed model
		model.register(larqBuilder);

		// return the builder
		return larqBuilder;
	}

	private String getRepositoryName() {
		// get the name of the repository,
		// and mark it with 'fulltext'
		// if fulltext support is active
		String name = this.getName();
		if (this.fulltext)
			name += "-fulltext";

		return name;
	}

	private File getFulltextDir() {
		// create the File pointing to the directory used for the index
		File dir = ROOT_DATA_DIR;
		dir = new File(dir, getRepositoryName());
		dir = new File(dir, database + ".index");

		return dir;
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
		FullTextTest t1 = new FullTextTest();
		String op = "load";
		t1.setOntology("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl");
		t1.provideFulltext();
		t1.open("saud");
		if (op.equals("load")) {
			if (t1.load("../../Data/"))
				t1.close();
		} else {
			String q1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
					"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n" + 
					"PREFIX arq: <http://jena.hpl.hp.com/ARQ/property#>\n" + 
					"SELECT ?X ?score\n" + 
					"WHERE {\n" + 
					"  (?lit ?score) arq:textMatch (\"network\" 0.75) .\n" + 
					"  ?X ub:publicationText ?lit .\n" + 
					"}";
			String q2 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
					"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n" + 
					"PREFIX arq: <http://jena.hpl.hp.com/ARQ/property#>\n" + 
					"SELECT ?X\n" + 
					"WHERE {\n" + 
					"  ?lit arq:textMatch \"engineer*\" .\n" + 
					"  ?X ub:publicationText ?lit .\n" + 
					"}";
			Date startTime, endTime;
			long duration = 0l;
			// t1.flushFSCache();
			startTime = new Date();
			t1.issueQuery(q2);
			endTime = new Date();
			t1.close();
			duration = endTime.getTime() - startTime.getTime();
			System.out.println("the time for execution is: " + duration);

		}

	}

}
