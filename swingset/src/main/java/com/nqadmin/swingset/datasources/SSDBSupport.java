
package com.nqadmin.swingset.datasources;

import com.nqadmin.swingset.utils.CentralLookup;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database specific handling and database access strategy.
 * 
 * @author err
 */
public interface SSDBSupport {

	static SSDBSupport getDefault() {
		SSDBSupport support = CentralLookup.getDefault().lookup(SSDBSupport.class);
		return support;
	}

	/**
	 * Execute an sql query that returns a ResultSet.
	 * 
	 * @param query
	 * @return 
	 */
	ResultSet execute(String query) throws SQLException;

	void insertRow(ResultSet rs) throws SQLException;

	void updateRow(ResultSet rs) throws SQLException;
	
}
