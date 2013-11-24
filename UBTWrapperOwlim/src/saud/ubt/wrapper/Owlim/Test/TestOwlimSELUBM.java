 package saud.ubt.wrapper.Owlim.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.MutableTupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontotext.trree.OwlimSchemaRepository;

public class TestOwlimSELUBM {

	/**
	 * @param args
	 */
	
	private static final String FLUSH_FS_CACHE_COMMAND = "purge";

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final static File ROOT_DATA_DIR = new File("local-sotrage/");
	private final static File ONTOLOGY_FILE = new File("univ-bench.owl");
	private static final String DEFAULT_CONFIG = "lubm.ttl";

	protected Repository repo;
	protected RepositoryConnection conn;
	protected String ontology;
	private RepositoryManager repositoryManager;

	protected void setUp() {
		this.repo = createRepository();
	}

	public void provideFulltext() {
		this.setUp();
	}

	private Graph parseFile(File configurationFile, RDFFormat format, String defaultNamespace) throws Exception {
		final Graph graph = new GraphImpl();
		RDFParser parser = Rio.createParser(format);
		RDFHandler handler = new RDFHandler() {
			public void endRDF() throws RDFHandlerException {
			}

			public void handleComment(String arg0) throws RDFHandlerException {
			}

			public void handleNamespace(String arg0, String arg1) throws RDFHandlerException {
			}

			public void handleStatement(Statement statement) throws RDFHandlerException {
				graph.add(statement);
			}

			public void startRDF() throws RDFHandlerException {
			}
		};

		Reader reader = new FileReader(configurationFile);
		parser.setRDFHandler(handler);
		parser.parse(reader, defaultNamespace);
		reader.close();
		return graph;
	}

	protected Repository createRepository() {
		
		try {
			repositoryManager = new LocalRepositoryManager(new File("."));
			repositoryManager.initialize();
			String configFile = System.getProperty("configFile", DEFAULT_CONFIG);
			final Graph graph = parseFile(new File(configFile), RDFFormat.TURTLE, "http://example.org#");
			Iterator<Statement> iter = graph.match(null, RDF.TYPE, new URIImpl(
					"http://www.openrdf.org/config/repository#Repository"));
			Resource repositoryNode = null;
			if (iter.hasNext()) {
				Statement st = iter.next();
				repositoryNode = st.getSubject();
			}

			// Find the repository ID
			iter = graph.match(repositoryNode, new URIImpl("http://www.openrdf.org/config/repository#repositoryID"), null);
			String repositoryId = null;
			if (iter.hasNext()) {
				Statement st = iter.next();
				Literal value = (Literal) st.getObject();
				repositoryId = value.stringValue();
			}

			System.out.println("Repository ID=" + repositoryId);

			// Check to see if the repository already exists
			RepositoryInfo repositoryInfo = null;
			try {
				repositoryInfo = repositoryManager.getRepositoryInfo(repositoryId);
			} catch (Exception e) {
			}

			// If not, then create it
			if (repositoryInfo == null) {
				System.out.println("Creating repository configuration");
				RepositoryConfig repConfig = RepositoryConfig.create(graph, repositoryNode);
				repositoryManager.addRepositoryConfig(repConfig);
				System.out.println("Creating new repository");
			} else {
				System.out.println("Using existing repository");
			}

			Collection<RepositoryInfo> infos = repositoryManager.getAllRepositoryInfos();
			for (RepositoryInfo info : infos) {
				System.out.println("Stored repository info: " + info.getId() + " - " + info.getLocation());
			}

			
			System.out.println("Repository created: " + repo);
			
			
			System.out.println("Repository created: " + repo);
//			conn = repo.getConnection();
//			System.out.println("Repository open");
//			conn.setAutoCommit(false);
			return repositoryManager.getRepository(repositoryId);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
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

			System.out.println("Totlal Result: " + totalno);
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
					this.conn.begin();
					this.conn.add(file, this.ontology, format);
					this.conn.commit();
				} catch (Exception e) {
					log.error(
							"could not load data from file '" + file.getName()
									+ "' as '" + format.toString() + "'", e);
					System.out.println(file.getName());
					System.out.println(format.toString());
					e.printStackTrace();
					return false;
				}
				log.debug("loaded data from file '{}'", file);
			}
		}
		this.addLuceneIndex();

		log.info("loaded data");

		return true;
	}
	private void addLuceneIndex(){
		
		String query = "PREFIX luc: <http://www.ontotext.com/owlim/lucene#>\n" + 
				"ASK { luc:myIndex luc:createIndex \"true\" . }";
		
		try {
			log.debug("Adding Lucene Index ...");
			boolean response = false;
			BooleanQuery result = conn.prepareBooleanQuery(QueryLanguage.SPARQL, query);
			response = result.evaluate();
			if (response){
			log.debug("Lucene index has been created");
			}
			else{
				log.error("Lucene index wasn't created!!");
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
			
		}
		
		
	}

	public void open(String database) {
		log.debug("opening repository with database '{}'", database);

		try {
//			this.repo.initialize();
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
					this.conn.begin();

					this.conn.add(ONTOLOGY_FILE, this.ontology, RDFFormat
							.forFileName(ONTOLOGY_FILE.getName(),
									RDFFormat.RDFXML), this.conn
							.getValueFactory().createURI(this.ontology));
					this.conn.commit();
				} catch (Exception e) {
					this.conn.rollback();
					log.error(
							"could not load ontology file '"
									+ ONTOLOGY_FILE.getAbsolutePath() + "'", e);
					System.out.println("hiiiii");
					e.printStackTrace();
					System.out.println(ONTOLOGY_FILE.getAbsolutePath());
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
		TestOwlimSELUBM t1 = new TestOwlimSELUBM();
		String op = "load";
		t1.setOntology("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl");
		t1.provideFulltext();

		
		t1.open("");
		
		if (op.equals("load")) {
			if (t1.load("/Users/saudaljaloud/Benchmarking/Data/"))
				t1.close();
		} else {
			String q5 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
					"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n" + 
					"PREFIX luc: <http://www.ontotext.com/owlim/lucene#>\n" + 
					"SELECT ?X ?score\n" + 
					"WHERE {\n" + 
					"  ?lit luc:myIndex \"network\" .\n" + 
					"  ?lit luc:score ?score .\n" + 
					"  ?X ub:publicationText ?lit .\n" + 
					"}\n" + 
					"ORDER BY DESC(?score)\n" + 
					"LIMIT 10";

						// t1.flushFSCache();
			Date startTime, endTime;
			Runtime runtime = Runtime.getRuntime();
			long duration = 0l;
			startTime = new Date();
			long usedMem = runtime.totalMemory() - runtime.freeMemory();
			t1.issueQuery(q5);
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
