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
