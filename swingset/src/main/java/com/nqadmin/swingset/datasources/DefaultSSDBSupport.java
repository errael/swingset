package com.nqadmin.swingset.datasources;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

/**
 * This implementation uses one connection for everything.
 * If any type of locking is needed, it must be done externally.
 * 
 * @author err
 */
public class DefaultSSDBSupport implements SSDBSupport {
	private Connection conn;

	public static SSDBSupport get(Connection conn) {
		return new DefaultSSDBSupport(conn);
	}

	private DefaultSSDBSupport(Connection conn) {
		this.conn = conn;
	}

	@Override
	public ResultSet execute(String query) throws SQLException {
		ResultSet rs;
		if (false) {
			Statement statement = conn.createStatement();
			rs = statement.executeQuery(query);
		} else {
			CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
			crs.setCommand(query);
			crs.execute(conn);
			rs = crs;
		}
		return rs;
	}

	@Override
	public void insertRow(ResultSet rs) throws SQLException {
		rs.insertRow();
		acceptChanges(rs);
	}

	@Override
	public void updateRow(ResultSet rs) throws SQLException {
		rs.updateRow();
		acceptChanges(rs);
	}

	private void acceptChanges(ResultSet rs) throws SQLException {
		// Note that JdbcRowSet does not need acceptChanges
		if(rs instanceof CachedRowSet) {
			CachedRowSet crs = (CachedRowSet) rs;
			// needed to autocommit false
			boolean isAutoCommit = conn.getAutoCommit();
			if(isAutoCommit)
				conn.setAutoCommit(false);
			crs.acceptChanges(conn);
			if(isAutoCommit)
				conn.setAutoCommit(true);
		}
	}
	
}
