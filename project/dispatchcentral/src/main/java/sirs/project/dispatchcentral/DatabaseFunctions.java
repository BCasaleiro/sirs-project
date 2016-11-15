package sirs.project.dispatchcentral;

import java.sql.Connection;
import java.sql.*;

public final class DatabaseFunctions{
	Connection c = null;

	public DatabaseFunctions(Connection c){
		this.c = c;
	}


	public void createTable(String command)
	{
		try
		{
			c.setAutoCommit(false);
			Statement stmt = c.createStatement();
			stmt.executeUpdate(command);
			stmt.close();
			c.commit();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

	}
}