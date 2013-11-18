package saud.ubt.wrapper.BigData;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;


public class BigDataAbstractRepository extends
		BigDataRepository {

	private final static File ROOT_DATA_DIR = new File("BigData-repositories");

	
	public BigDataAbstractRepository(){
		super.provideFulltext();
	}
	
	@Override
	protected BigdataSailRepository createRepository() {
		BigdataSail sail = null;
		try {
			Properties properties = loadProperties("fastload.properties");
			File journal = new File(ROOT_DATA_DIR + "/bigdata.jnl");
			properties.setProperty(BigdataSail.Options.FILE,
					journal.getAbsolutePath());
			sail = new BigdataSail(properties);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new BigdataSailRepository(sail);

	}
	
	public Properties loadProperties(String resource) throws Exception {
		Properties p = new Properties();
		InputStream is = new FileInputStream(resource); 
		p.load(new InputStreamReader(new BufferedInputStream(is)));
		return p;
	}

}
