package de.l3s.ubt.wrapper.jena2_11Regex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;

import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextDatasetFactory;
import org.apache.jena.query.text.TextIndex;
import org.apache.jena.query.text.TextIndexLucene;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.PathBlock;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;
import com.hp.hpl.jena.util.FileManager;

import edu.lehigh.swat.bench.ubt.api.QueryResult;

public abstract class JenaRepository implements
		edu.lehigh.swat.bench.ubt.api.Repository {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final static File ROOT_DATA_DIR = new File("jena-repositories");
	// private final static File ONTOLOGY_FILE = new File("univ-bench.owl");
	private final static String RDF_FORMAT = "RDF/XML";

	private boolean fulltext;
	private String ontology;
	private String database;

	private TextIndex index;

	private Dataset ds;
	private Dataset ds2;
	private String queryString;
	private String regexVar;
	final Node search = Node.createURI("http://jena.apache.org/text#query");
	private Boolean regexIndexQuery = false;

	public Boolean getRegexIndexQuery() {
		return regexIndexQuery;
	}

	public void setRegexIndexQuery(Boolean regexIndexQuery) {
		this.regexIndexQuery = regexIndexQuery;
	}

	public String getRegexVar() {
		return regexVar;
	}

	public void setRegexVar(String regexVar) {
		this.regexVar = regexVar;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	protected abstract Dataset createModel(String... names);

	protected abstract String getName();

	public void provideFulltext() {
		this.fulltext = true;
	}

	public void clear() {
		log.debug("clearing repository");

		this.ds.removeNamedModel(this.ontology);
		this.ds2.removeNamedModel(this.ontology);
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

		this.ds2.close();
		log.info("closed repository");
	}

	public QueryResult issueQuery(edu.lehigh.swat.bench.ubt.api.Query query) {

		log.debug("querying repository with query\n{}", query.getString());

		try {
			log.info("queried repository");
			ds2.begin(ReadWrite.READ);
			Query q = QueryFactory.create(query.getString());
			ElementWalker.walk(q.getQueryPattern(), new ElementVisitorBase() {
				@Override
				public void visit(ElementPathBlock el) {
					PathBlock pathBlock = el.getPattern();
					Iterator<TriplePath> itr = pathBlock.iterator();
					Node searchSubject = null;
					Node ignoredSameObject = null;
					while (itr.hasNext()) {
						TriplePath tri = itr.next();
						if (tri.getPredicate().getURI()
								.equals("http://jena.apache.org/text#query")) {
							searchSubject = tri.getSubject();
							ignoredSameObject = tri.getObject();
							setRegexIndexQuery(true);
							break;
						}

					}
					Iterator<TriplePath> itr1 = pathBlock.iterator();
					while (itr1.hasNext()) {
						TriplePath tri = itr1.next();
						if (tri.getSubject().equals(searchSubject)
								&& !tri.getObject().equals(ignoredSameObject)) {
							String obj = tri.getObject().toString();
							setRegexVar(obj.replaceAll("\\?", ""));
						}

					}
					Iterator<TriplePath> itr2 = pathBlock.iterator();
					while (itr2.hasNext()) {
						TriplePath tri = itr2.next();
						if (tri.getSubject().isVariable()) {
							if (tri.getSubject().toString().contains("??")
									&& tri.getObject().isLiteral()) {
								setQueryString(tri.getObject()
										.getLiteralLexicalForm());
							}
						}

					}

				}
			});

			if (getRegexIndexQuery()) {
				Var varTitle = Var.alloc(getRegexVar());
				Expr expr = new E_Regex(new ExprVar(varTitle),
						getQueryString(), "");
				ElementFilter filter = new ElementFilter(expr);
				FilterAdder visitor = new FilterAdder(filter);
				q.getQueryPattern().visit(visitor);
			}
			System.out.println(q);
			QueryExecution qe = QueryExecutionFactory.create(q, ds2);
			ResultSet rs = qe.execSelect();
			ds2.end();
			return new JenaQueryResult(rs);

		} catch (Exception e) {
			log.error("could not query repostiory", e);
			return null;
		}
	}

	public class FilterAdder extends ElementVisitorBase implements
			ElementVisitor {
		// Filter to be added to query
		private ElementFilter filter;

		// Public constructor
		public FilterAdder(ElementFilter elFilter) {
			filter = elFilter;
		}

		// Override this function so that it adds the desired filter
		@Override
		public void visit(ElementGroup el) {
			el.addElementFilter(filter);
		}
	}

	public boolean load(String dataDir) {
		log.debug("loading data from dir '{}'", dataDir);

		// if fulltext support is requested, add LARQ to it

		if (!this.fulltext) {
			ds2 = ds;
		}

		File dir = new File(dataDir);
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.getPath().contains("University")) {
				log.debug("loading data from file '{}' as '{}'", file,
						RDF_FORMAT);
				try {
					log.info(file.getPath());
					ds2.begin(ReadWrite.WRITE);
					Model tdb = ds2.getDefaultModel();
					FileManager.get().readModel(tdb, file.getAbsolutePath(),
							ontology, RDF_FORMAT);
					ds2.commit();
				} catch (Exception e) {
					log.error(
							"could not load data from file '" + file.getName()
									+ "' as '" + RDF_FORMAT + "'", e);
					ds2.abort();
					return false;
				} finally {

				}
				log.debug("loaded data from file '{}'", file);
			}
		}

		log.info("loaded data");

		return true;
	}

	public void open(String database) {
		log.debug("opening repository with database '{}'", database);

		// memorize the database
		this.database = database;

		// get the actual model implementation from sub classes
		this.ds = createModel(getNames());

		log.info("opened repository with database '{}'", database);

		buildTextIndex();
	}

	public void buildTextIndex() {

		try {
			Directory directory = FSDirectory.open(getFulltextDir());
			Node publicationText = Node
					.createURI("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#publicationText");
			Node firstname = Node
					.createURI("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#firstname");
			Node surName = Node
					.createURI("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#surname");
			Node fullName = Node
					.createURI("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#fullname");
			EntityDefinition eDef = new EntityDefinition("iri",
					"publicationText", publicationText);
			eDef.set("firstname", firstname);
			eDef.set("surname", surName);
			eDef.set("fullname", fullName);
			index = new TextIndexLucene(directory, eDef);
			ds2 = TextDatasetFactory.create(ds, index);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

}
