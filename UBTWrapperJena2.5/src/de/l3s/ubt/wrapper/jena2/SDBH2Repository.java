package de.l3s.ubt.wrapper.jena2;

import java.io.File;

import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;

public class SDBH2Repository extends SDBRepository {

	@Override
	protected String getName() {
		return super.getName() + "-h2";
	}

	@Override
	protected Store createStore(String... names) {
		StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.H2);
		JDBC.loadDriverH2();

		StringBuilder jdbcUrl = new StringBuilder();
		jdbcUrl.append("jdbc:h2:");
		
		StringBuilder path = new StringBuilder();
		for(String name : names) {
			path.append(name);
			path.append('/');
		}
		
		// make sure the folder exist
		new File(path.toString().substring(0, path.toString().length() - 1)).mkdirs();

		path.append("h2");
		jdbcUrl.append(path);

		// create the store
		SDBConnection conn = new SDBConnection(jdbcUrl.toString(), "sa", "") ; 
		Store store = SDBFactory.connectStore(conn, storeDesc) ;
		
		return store;
	}

}
