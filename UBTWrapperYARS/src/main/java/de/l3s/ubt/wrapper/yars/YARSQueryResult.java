package de.l3s.ubt.wrapper.yars;

import org.semanticweb.yars.api.ResultSet;

import edu.lehigh.swat.bench.ubt.api.QueryResult;

public class YARSQueryResult implements QueryResult {
	
//	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final ResultSet rs;
	private int num = 0;

	public YARSQueryResult(ResultSet rs) {
		this.rs = rs;
	}

	public long getNum() {
		return this.num;
	}

	public boolean next() {
		if(this.rs.next()) {
			this.num++;
			
			return true;
		}
		return false;
	}

}
