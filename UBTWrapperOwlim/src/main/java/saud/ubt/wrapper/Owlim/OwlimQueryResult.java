package saud.ubt.wrapper.Owlim;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lehigh.swat.bench.ubt.api.QueryResult;

public class OwlimQueryResult implements QueryResult {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final TupleQueryResult result;
	private int num = 0;

	public OwlimQueryResult(TupleQueryResult result) {
		this.result = result;
	}

	public long getNum() {
		return this.num;
	}

	public boolean next() {
		try {
			if(this.result.hasNext() && (this.result.next() != null)) {
				this.num++;
				return true;
			}
		} catch (QueryEvaluationException e) {
			log.error("could not get next result", e);
		}
		return false;
	}

}
