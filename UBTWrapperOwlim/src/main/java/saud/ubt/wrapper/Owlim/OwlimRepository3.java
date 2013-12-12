package saud.ubt.wrapper.Owlim;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import edu.lehigh.swat.bench.ubt.api.Query;
import edu.lehigh.swat.bench.ubt.api.QueryResult;

public abstract class OwlimRepository3 implements
		edu.lehigh.swat.bench.ubt.api.Repository {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final static File ONTOLOGY_FILE = new File("univ-bench.owl");
	private RepositoryManager repositoryManager;
	private static final String DEFAULT_CONFIG = "lubm.ttl";
	private final static File ROOT_config_DIR = new File("owlimSE-Configrations");

	protected Repository repo;
	protected RepositoryConnection conn;
	protected String ontology;
	protected String database;

	

	public void provideFulltext() {
		
		try {
			repositoryManager = new LocalRepositoryManager(new File("."));
			repositoryManager.initialize();
			String configFile = System.getProperty("configFile", ROOT_config_DIR + "/" + this.database + "/" + DEFAULT_CONFIG);
			System.out.println(ROOT_config_DIR + "/" + this.database + "/" + DEFAULT_CONFIG);
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

			log.debug("Repository ID=" + repositoryId);

			// Check to see if the repository already exists
			RepositoryInfo repositoryInfo = null;
			try {
				repositoryInfo = repositoryManager.getRepositoryInfo(repositoryId);
			} catch (Exception e) {
			}

			// If not, then create it
			if (repositoryInfo == null) {
				log.debug("Creating repository configuration");
				RepositoryConfig repConfig = RepositoryConfig.create(graph, repositoryNode);
				repositoryManager.addRepositoryConfig(repConfig);
				log.debug("Creating new repository");
			} else {
				log.debug("Using existing repository");
			}

			Collection<RepositoryInfo> infos = repositoryManager.getAllRepositoryInfos();
			for (RepositoryInfo info : infos) {
				log.debug("Stored repository info: " + info.getId() + " - " + info.getLocation());
			}

			
			log.debug("Repository created: " + repo);
			
			
			log.debug("Repository created: " + repo);
//			conn = repo.getConnection();
//			System.out.println("Repository open");
//			conn.setAutoCommit(false);
			this.repo = repositoryManager.getRepository(repositoryId);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
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
			this.repositoryManager.shutDown();
		} catch (RepositoryException e) {
			log.error("Could not close repository", e);
			return;
		}

		log.info("closed repository");
	}

	public QueryResult issueQuery(Query query) {
		log.debug("querying repository with query\n{}", query);
		if (!query.getString().contains("ASK")){
		try {
			TupleQuery q = this.conn.prepareTupleQuery(QueryLanguage.SPARQL,
					query.getString());
			TupleQueryResult r = q.evaluate();
			log.info("queried repository");
			return new OwlimQueryResult(r);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		}
		else{
			try {
				System.out.println("This is ask query");
				boolean response = false;
				BooleanQuery result = conn.prepareBooleanQuery(QueryLanguage.SPARQL, query.getString());
				response = result.evaluate();
				System.out.println("---------");
				System.out.println(result);
				System.out.println("----------");
				System.out.println("the result is: " + response);
				return null;
			}
			catch (Exception e) {
				e.printStackTrace();
				return null;
			}
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
					this.conn.add(file, this.ontology, RDFFormat.RDFXML);
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
		
		this.database = database;
		this.provideFulltext();
		
		try {
			if (!this.repo.isInitialized()){
				this.repo.initialize();
			}
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
