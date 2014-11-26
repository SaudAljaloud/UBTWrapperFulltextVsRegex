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
package de.l3s.ubt.wrapper.jena2_11;

import java.io.File;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.TDBFactory;

public class TDBRepository extends PersistentRepository {

	@Override
	protected String getName() {
		return super.getName() + "-tdb";
	}


	@Override
	protected Dataset createModel(String... names) {
		// build up the path again
		StringBuilder string = new StringBuilder();
		string.append(File.separator);
		for(String name : names) {
			string.append(name);
			string.append(File.separator);
		}

		// make sure the directory exists
		new File(string.toString()).mkdirs();
		
		// use that path for the model backend
		return TDBFactory.createDataset(string.toString()) ;
	}

}
