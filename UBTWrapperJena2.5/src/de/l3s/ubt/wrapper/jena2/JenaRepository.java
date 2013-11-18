package de.l3s.ubt.wrapper.jena2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import com.hp.hpl.jena.query.larq.IndexBuilderModel;
import com.hp.hpl.jena.query.larq.IndexBuilderString;
import com.hp.hpl.jena.query.larq.IndexLARQ;
import com.hp.hpl.jena.query.larq.LARQ;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

import edu.lehigh.swat.bench.ubt.api.QueryResult;

public abstract class JenaRepository implements
		edu.lehigh.swat.bench.ubt.api.Repository {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final static File ROOT_DATA_DIR = new File("jena-repositories");
	// private final static File ONTOLOGY_FILE = new File("univ-bench.owl");
	private final static String RDF_FORMAT = "RDF/XML";

	private boolean fulltext;
	private Model model;
	private String ontology;
	private String database;

	private IndexLARQ index;

	protected abstract Model createModel(String... names);

	protected abstract String getName();

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

	public QueryResult issueQuery(edu.lehigh.swat.bench.ubt.api.Query query) {
		log.debug("querying repository with query\n{}", query.getString());

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
					return null;
				}
			}
		}

		try {
			log.info("queried repository");
			Query q = QueryFactory.create(query.getString());
			QueryExecution qe = QueryExecutionFactory.create(q, model);
			ResultSet rs = qe.execSelect();
			return new JenaQueryResult(rs);
		} catch (Exception e) {
			log.error("could not query repostiory", e);
			return null;
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
			if (file.getPath().contains("University")) {
				log.info("loading data from file '{}' as '{}'", file,
						RDF_FORMAT);
				try {
					log.info(file.getPath());
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

}
