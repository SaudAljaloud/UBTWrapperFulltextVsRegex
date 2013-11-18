package de.l3s.ubt.wrapper.virtuoso5;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import edu.lehigh.swat.bench.ubt.api.QueryResult;

public class VirtuosoQueryResult implements QueryResult {
	
	private final static Logger log = LoggerFactory
			.getLogger(VirtuosoQueryResult.class);	
	private final ResultSet rs;
	private int num = 0;

	public VirtuosoQueryResult(ResultSet rs) {
		this.rs = rs;
	}

	public long getNum() {
		return this.num;
	}

	public boolean next() {
		try {
			if(this.rs.next()) {
				this.num++;
				return true;
			}
		} catch (SQLException e) {
			log.error("could not determine next results", e);
		}
		return false;
	}

}
