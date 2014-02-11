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
import com.hp.hpl.jena.graph.NodeFactory;
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



	private String nameSpace = "http://jena.apache.org/text#regex";
	private String jena_textQueryFirst = "http://www.w3.org/1999/02/22-rdf-syntax-ns#first";
	private String jena_textQueryRest = "http://www.w3.org/1999/02/22-rdf-syntax-ns#rest";
	private Node publicationText = NodeFactory
			.createURI("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#publicationText");
	private Node firstname = NodeFactory
			.createURI("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#firstname");
	private Node surName = NodeFactory
			.createURI("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#surname");
	private Node fullName = NodeFactory
			.createURI("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#fullname");

	private Boolean regexIndexQuery = false;
	private ArrayList<String> regexVar;

	public String getRegexVar(int i) {
		return regexVar.get(i);
	}

	public void addRegexVar(String regexVar) {
		this.regexVar.add(regexVar);
	}

	private ArrayList<String> queryString;

	public String getQueryString(int i) {
		return queryString.get(i);
	}

	public void addQueryString(String queryString) {
		this.queryString.add(queryString);
	}

	public Boolean getRegexIndexQuery() {
		return regexIndexQuery;
	}

	public void setRegexIndexQuery(Boolean regexIndexQuery) {
		this.regexIndexQuery = regexIndexQuery;
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
		queryString = new ArrayList<String>();
		regexVar = new ArrayList<String>();

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
					Node fieldObject = null;
					Node querySubject = null;
					boolean fieldObjectIdeantified = false;
					while (itr.hasNext()) {

						TriplePath tri = itr.next();

						if (tri.getPredicate().getURI().equals(nameSpace)) {

							searchSubject = tri.getSubject();
							if (tri.getObject().isVariable()) {
								ignoredSameObject = tri.getObject();
								// System.out.println("ignoredSameObject "
								// + ignoredSameObject);

								Iterator<TriplePath> itr2 = pathBlock
										.iterator();
								while (itr2.hasNext()) {
									TriplePath tri2 = itr2.next();
									if (tri2.getSubject().equals(
											ignoredSameObject)
											&& tri2.getPredicate()
													.toString()
													.equals(jena_textQueryFirst)) {
										fieldObject = tri2.getObject();
										// System.out.println("fieldObject "
										// + fieldObject);
										fieldObjectIdeantified = true;

									}
									if (tri2.getSubject().equals(
											ignoredSameObject)
											&& tri2.getPredicate().toString()
													.equals(jena_textQueryRest)) {
										querySubject = tri2.getObject();
										// System.out.println("querySubject "
										// + querySubject);
										break;

									}
								}
								Iterator<TriplePath> itr3 = pathBlock
										.iterator();
								while (itr3.hasNext()) {
									TriplePath tri3 = itr3.next();
									if (tri3.getSubject().equals(querySubject)
											&& tri3.getPredicate()
													.toString()
													.equals(jena_textQueryFirst)
											&& tri3.getObject().isLiteral()) {
										// System.out
										// .println("query string "
										// + tri3.getObject()
										// .getLiteralLexicalForm());
										addQueryString(tri3.getObject()
												.getLiteralLexicalForm());
										break;

									}
								}

							} else if (tri.getObject().isLiteral()) {
								searchSubject = tri.getSubject();
								addQueryString(tri.getObject()
										.getLiteralLexicalForm());
								Iterator<TriplePath> itr4 = pathBlock
										.iterator();
								while (itr4.hasNext()) {
									TriplePath tri4 = itr4.next();
									if (tri4.getSubject().equals(searchSubject)
											&& tri4.getObject().isVariable()) {
										String obj = tri4.getObject()
												.toString();
										addRegexVar(obj.replaceAll("\\?", ""));
										break;
									}

								}
								break;
							}

							setRegexIndexQuery(true);

						}
						if (fieldObjectIdeantified) {
							if (tri.getSubject().equals(searchSubject)
									&& tri.getPredicate().equals(fieldObject)) {
								String obj = tri.getObject().toString();
								addRegexVar(obj.replaceAll("\\?", ""));
								// System.out.println("getRegexVar"
								// + obj.replaceAll("\\?", ""));
								fieldObjectIdeantified = false;
							}
						}

					}

				}
			});

			System.out.println("number of regex var " + regexVar.size());
			System.out.println("number of query var " + queryString.size());
			for (int i = 0; i < queryString.size(); i++) {
				Var varTitle = Var.alloc(getRegexVar(i));
				// System.out.println("varTitle" + varTitle);
				// System.out.println("getQueryString" + getQueryString(i));
				Expr expr = new E_Regex(new ExprVar(varTitle),
						getQueryString(i), "i");
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
			EntityDefinition eDef = new EntityDefinition("iri",
					"publicationText", publicationText);
			eDef.set("firstname", firstname);
			eDef.set("surname", surName);
			eDef.set("fullname", fullName);
			index = new TextIndexLucene(directory, eDef);
			ds2 = TextDatasetFactory.create(ds, index);
		} catch (IOException e) {
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
