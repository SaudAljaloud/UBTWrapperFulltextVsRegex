package de.l3s.ubt.wrapper.jena2_7;

import com.hp.hpl.jena.query.ResultSet;

import edu.lehigh.swat.bench.ubt.api.QueryResult;

public class JenaQueryResult implements QueryResult {
	
//	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final ResultSet rs;

	public JenaQueryResult(ResultSet rs) {
		this.rs = rs;
	}

	public long getNum() {
		return this.rs.getRowNumber();
	}

	public boolean next() {
		return rs.hasNext() && rs.next() != null;
	}

}
