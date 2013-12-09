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
import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;


@SuppressWarnings("deprecation")
public class OwlimAbstractRepository extends OwlimRepository {

//	private final static File ROOT_DATA_DIR = new File("Owlim-repositories");
	private RepositoryManager repositoryManager;
	private static final String DEFAULT_CONFIG = "lubm.ttl";

	public OwlimAbstractRepository() {
		super.provideFulltext();
	}

	@Override
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
}
