package de.l3s.ubt.wrapper.jena2_5;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;

public abstract class SDBRepository extends PersistentRepository {

//	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private Store store;
	
	protected abstract Store createStore(String... names);
	
	@Override
	protected String getName() {
		return super.getName() + "-sdb";
	}
	
	@Override
	public boolean load(String dataDir) {
		store.getTableFormatter().create();
		return super.load(dataDir);
	}

//	@Override
//	public QueryResult issueQuery(edu.lehigh.swat.bench.ubt.api.Query query) {
//		log.debug("querying repository with query\n{}", query.getString());
//
//		if(fulltext) {
//			// if there is no index yet, create one
//			if(this.index == null) {
//				try {
//					Directory dir = FSDirectory.getDirectory(getFulltextDir());
//					this.index = new IndexLARQ(IndexReader.open(dir));
//					LARQ.setDefaultIndex(this.index) ;
//				} catch (IOException e) {
//					log.error("could create LARQ index to support fulltect on query time", e);
//					return null;
//				}
//			}
//		}
//
//		try {
//			log.info("queried repository");
//			Dataset ds = DatasetStore.create(store) ;
//			QueryExecution qe = QueryExecutionFactory.create(query.getString(), ds) ;
//			ResultSet rs = qe.execSelect();
//			return new JenaQueryResult(rs);
//		} catch (Exception e) {
//			log.error("could not query repostiory", e);
//			return null;
//		}
//	}


	@Override
	protected Model createModel(String... names) {
		store = createStore(names);
		return SDBFactory.connectDefaultModel(store);
	}
	
}
