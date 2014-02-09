package de.l3s.ubt.wrapper.jena2_11Regex.Test2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextDatasetFactory;
import org.apache.jena.query.text.TextIndex;
import org.apache.jena.query.text.TextQueryPF;

import org.apache.jena.query.text.TextIndexLucene;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.core.PathBlock;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.syntax.Element1;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

import de.l3s.ubt.wrapper.jena2_11Regex.JenaRepository.FilterAdder;

public class Index {

	/**
	 * @param args
	 */
	
	private Boolean regexIndexQuery = false;
	private String regexVar;
	private String queryString;

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
		
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Index test = new Index();
//		 test.load();
		test.query();

	}

	String database = "testing/tdb/";
	String luceneDir = "testing/Lucene/";

	public void delete(File f) {
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				delete(c);
		}
		if (!f.delete())
			try {
				throw new FileNotFoundException("Failed to delete file: " + f);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	public void load() {
		System.out.println("Start loading");
		if (!new File(database).exists()) {
			new File(database).mkdirs();
		} else {
			delete(new File(database));
		}

		if (!new File(luceneDir).exists()) {
			new File(luceneDir).mkdirs();
		} else {
			delete(new File(luceneDir));
		}

		String ontology = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl";
		String rdfpath = "../../LUBM/LUBM-fulltext-1/";
		String RDF_FORMAT = "RDF/XML";
		Dataset ds1 = null;
		Dataset ds2 = null;
		TextIndex tidx = null;
		try {
			ds1 = TDBFactory.createDataset(database);
			Directory directory = FSDirectory.open(new File(luceneDir));
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
			tidx = new TextIndexLucene(directory, eDef);
			ds2 = TextDatasetFactory.create(ds1, tidx);
			ds2.begin(ReadWrite.WRITE);
			File dir = new File(rdfpath);
			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.getPath().contains("University")) {
					try {
						ds2.begin(ReadWrite.WRITE);
						Model tdb = ds2.getDefaultModel();
						FileManager.get().readModel(tdb,
								file.getAbsolutePath(), ontology, RDF_FORMAT);
						ds2.commit();
					} catch (Exception e) {
						ds2.abort();
					} finally {

					}
				}
			}

		} catch (IOException e) {
			ds2.abort();
			e.printStackTrace();

		} finally {
			System.out.println("Loaded ...!");
			ds2.end();

		}
		tidx.close();
		ds2.close();

	}

	public void query() {

		Dataset ds1 = null;
		Dataset ds2 = null;
		TextIndex tidx = null;

		try {
			ds1 = TDBFactory.createDataset(database);
			Directory directory = FSDirectory.open(new File(luceneDir));
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
			tidx = new TextIndexLucene(directory, eDef);
			ds2 = TextDatasetFactory.create(ds1, tidx);
			ds2.begin(ReadWrite.READ);
			String q2 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
					"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n" + 
					"PREFIX text: <http://jena.apache.org/text#>\n" + 
					"SELECT ?X\n" + 
					"WHERE {\n" + 
					"  ?X text:query (ub:surname \"smi\") .\n" + 
					"  ?X ub:surname ?lit .\n" + 
					"}";
			Query q = QueryFactory.create(q2);
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
						getQueryString(), "i");
				ElementFilter filter = new ElementFilter(expr);
				FilterAdder visitor = new FilterAdder(filter);
				q.getQueryPattern().visit(visitor);
			}
			System.out.println(q);
			QueryExecution qe = QueryExecutionFactory.create(q, ds2);
			ResultSet rs = qe.execSelect();

			int i = 0;
			while (rs.hasNext() && rs.next() != null) {
				i++;
			}
			System.out.println("result: " + i);
			// ResultSetFormatter.out(System.out, rs);
			ds2.end();

		} catch (IOException e) {

		} finally {
			ds2.close();
			tidx.close();
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

}
