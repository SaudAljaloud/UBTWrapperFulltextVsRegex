/**

* Copyright 2012 Saud Aljaloud
* 
* This file is part of UBTWrapperFulltextVsRegex.
* 
* UBTWrapperFulltextVsRegex is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
* 
* UBTWrapperFulltextVsRegex is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License along with UBTWrapperFulltextVsRegex. If not, see http://www.gnu.org/licenses/.
 */
package saud.ubt.wrapper.BigData;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

//import org.apache.log4j.BasicConfigurator;
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



import edu.lehigh.swat.bench.ubt.api.Query;
import edu.lehigh.swat.bench.ubt.api.QueryResult;

public abstract class BigDataRepository2 implements
		edu.lehigh.swat.bench.ubt.api.Repository {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final static File ONTOLOGY_FILE = new File("univ-bench.owl");
	private final static File ROOT_DATA_DIR = new File("BigData-repositories");
	
	protected Repository repo;
	protected RepositoryConnection conn;
	protected String ontology;
	private String database;

	

	public void provideFulltext() {
		BigdataSail sail = null;
		try {
			Properties properties = loadProperties("fastload.properties");
			File journal = new File(ROOT_DATA_DIR +  "/" + this.database + "/bigdata.jnl");
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

	public QueryResult issueQuery(Query query) {
		log.debug("querying repository with query\n{}", query);
		
		try {
			TupleQuery q = this.conn.prepareTupleQuery(QueryLanguage.SPARQL,
					query.getString());
			TupleQueryResult r = q.evaluate();
			log.info("queried repository");
			return new BigDataQueryResult(r);
		} catch (Exception e) {
			e.printStackTrace();
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
					this.conn.add(ONTOLOGY_FILE, this.ontology, RDFFormat.RDFXML);
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
