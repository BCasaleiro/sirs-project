package sirs.project.dispatchcentral;

import java.sql.Connection;
import java.sql.*;

public final class DatabaseFunctions{

	public DatabaseFunctions(){}

	public void execCmd(Connection c, String command)
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


	public void insertUser(Connection c, String command, String phoneNumber)
	{
		try
		{
			PreparedStatement ps = c.prepareStatement(command);
			ps.setString(1, phoneNumber);
			c.setAutoCommit(false);
			ps.execute();
			c.commit();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

	}

	public void insertRequest(Connection c, String command)
	{

	}

	public void updateRating(Connection c, String command, int value)
	{

	}

	public void getUserRating(Connection c, String command, String phoneNumber)
	{

	}

}