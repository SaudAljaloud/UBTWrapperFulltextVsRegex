package de.l3s.ubt.wrapper.jena2_5;

import edu.lehigh.swat.bench.ubt.api.Repository;

public class MySQLFactory extends JDBCFactory {

	@Override
	public Repository create() {
		return new MySQLRepository();
	}

}
