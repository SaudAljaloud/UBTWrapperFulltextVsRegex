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

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lehigh.swat.bench.ubt.api.QueryResult;

public class BigDataQueryResult implements QueryResult {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final TupleQueryResult result;
	private int num = 0;

	public BigDataQueryResult(TupleQueryResult result) {
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
