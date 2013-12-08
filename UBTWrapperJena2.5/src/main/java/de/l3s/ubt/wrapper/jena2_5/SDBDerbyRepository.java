package de.l3s.ubt.wrapper.jena2_5;

import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;

public class SDBDerbyRepository extends SDBRepository {

	private static final String DEFAULT_OPTIONS = "create=true;upgrade=true";

	@Override
	protected String getName() {
		return super.getName() + "-derby";
	}

	@Override
	protected Store createStore(String... names) {
		StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.Derby);
		JDBC.loadDriverDerby();

		StringBuilder jdbcUrl = new StringBuilder();
		jdbcUrl.append("jdbc:derby:");
		for(String name : names) {
			jdbcUrl.append(name);
			jdbcUrl.append('/');
		}
		jdbcUrl.append(';');
		jdbcUrl.append(DEFAULT_OPTIONS);

		SDBConnection conn = new SDBConnection(jdbcUrl.toString(), null, null) ; 
		Store store = SDBFactory.connectStore(conn, storeDesc) ;
		
		return store;
	}

}
