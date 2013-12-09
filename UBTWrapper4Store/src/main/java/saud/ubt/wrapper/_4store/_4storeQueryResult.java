package saud.ubt.wrapper._4store;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import edu.lehigh.swat.bench.ubt.api.QueryResult;

/**
 * Auther: saudaljaloud Email: sza1g10@ecs.soton.ac.uk
 */
public class _4storeQueryResult implements QueryResult {
	private final static Logger log = LoggerFactory
			.getLogger(_4storeQueryResult.class);
	private String jsonString;
	private java.util.Iterator<JsonNode> itr = null;
	private List<JsonNode> aa = null;

	public _4storeQueryResult(String jsonString) {
		this.jsonString = jsonString;
		init();
	}

	private void init() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode actualObj = mapper.readTree(jsonString);
			aa = actualObj.get("results").get("bindings").findValues("value");
			itr = aa.iterator();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public long getNum() {
		return this.aa.size();
	}

	public boolean next() {
		try {
			return itr.hasNext() && itr.next() != null;

		} catch (Exception e) {
			log.error("could not determine next results", e);
		}
		return false;
	}

}
