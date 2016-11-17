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

	//Not tested yet
/*
	public void insertRequest(Connection c, String command, Request request)
	{
		try{
			PreparedStatement ps = c.prepareStatement(command);
			ps.setString(1, request.getUserId());
			ps.setString(2, request.getMessage());
			ps.setDate(3, request.getDate());
			c.setAutoCommit(false);
			ps.execute();
			c.commit();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
*/
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
			ResultSet result = ps.executeQuery();

			return (!resultEmpty(result));
	
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public int userRating(Connection c, String command, String phoneNumber)
	{
		if(!userExists(c, dbConstants.listPhoneNumbers, phoneNumber))
		{
			System.out.println("User doesn't exist");
			return -1;
		}

		try
		{
			PreparedStatement ps = c.prepareStatement(command);
			ps.setString(1, phoneNumber);

			ResultSet result = ps.executeQuery();
			result.next();
			return result.getInt("RATING");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return -1;
	}

	public static boolean resultEmpty(ResultSet rs) throws SQLException {
 	   return (!rs.isBeforeFirst() && rs.getRow() == 0);
	}
}