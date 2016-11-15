package sirs.project.dispatchcentral;

import java.sql.Connection;
import java.sql.*;

public final class DatabaseFunctions{
	//TODO add ROLLBACK instead of print when catches an exception 
	
	DatabaseConstants dbConstants = null;

	public DatabaseFunctions(DatabaseConstants constants){
		dbConstants = constants;
	}
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
		if(userExists(c, dbConstants.listPhoneNumbers, phoneNumber))
		{
			System.out.println("User Already Registered");
			return;
		}

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

	public void updateRating(Connection c, String command, String phoneNumber, int value)
	{
		try
		{
			PreparedStatement ps = c.prepareStatement(command);
			ps.setInt(1, value);
			ps.setString(2, phoneNumber);
			c.setAutoCommit(false);
			ps.execute();
			c.commit();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public boolean userExists(Connection c, String command, String phoneNumber)
	{
		try
		{
			PreparedStatement ps = c.prepareStatement(command);
			ps.setString(1, phoneNumber);
			c.setAutoCommit(false);
			ResultSet result = ps.executeQuery();
			c.commit();

			return (!resultEmpty(result));
	
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public void getUserRating(Connection c, String command, String phoneNumber)
	{

	}

	public static boolean resultEmpty(ResultSet rs) throws SQLException {
 	   return (!rs.isBeforeFirst() && rs.getRow() == 0);
	}
}
