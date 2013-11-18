package de.l3s.ubt.wrapper.jena2;

import edu.lehigh.swat.bench.ubt.api.Repository;

public class DerbyFactory extends JDBCFactory {

	@Override
	public Repository create() {
		return new DerbyRepository();
	}

}
