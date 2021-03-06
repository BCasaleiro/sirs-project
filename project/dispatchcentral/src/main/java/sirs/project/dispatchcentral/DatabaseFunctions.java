package sirs.project.dispatchcentral;

import java.sql.Connection;

import sirs.project.clientrequest.Request;

import java.sql.*;

public final class DatabaseFunctions{

	DatabaseConstants dbConstants = null;

	public DatabaseFunctions(){
		this.dbConstants = new DatabaseConstants();
	}
	public void createTable(Connection c, String command)
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
			try{
				c.rollback();
			}catch(SQLException e2){e.printStackTrace();}
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
			try{
				c.rollback();
			}catch(SQLException e2){e.printStackTrace();}
		}

	}

	public boolean requestExists(Connection c, String command, Request request)
	{
		try{
			PreparedStatement ps = c.prepareStatement(command);
			ps.setString(1, request.getId());
			ResultSet result = ps.executeQuery();
			return (!resultEmpty(result));
		}catch(SQLException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public void insertRequest(Connection c, String command, Request request)
	{
		if(requestExists(c,dbConstants.requestExists ,request))
		{
			System.out.println("Request already exists");
			return;
		}
		try{
			PreparedStatement ps = c.prepareStatement(command);
			ps.setString(1, request.getId());
			ps.setString(2, request.getUserId());
			ps.setString(3, request.getLocalization());
			ps.setString(4, request.getMessage());
			ps.setTimestamp(5, new Timestamp(request.getDate().getTime()));
			c.setAutoCommit(false);
			ps.execute();
			c.commit();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			try{
				c.rollback();
			}catch(SQLException e2){e.printStackTrace();}
		}
	}

	public int getRequestId(Connection c, String command, Request request)
	{
		try{
			PreparedStatement ps = c.prepareStatement(command);
			ps.setString(1, request.getUserId());
			ps.setString(2, request.getLocalization());
			ps.setString(3, request.getMessage());
			ps.setTimestamp(4, new Timestamp(request.getDate().getTime()));

			ResultSet result = ps.executeQuery();
			result.next();
			return result.getInt("ID");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return -1;
	}
	public void setDispatched(Connection c, String command, int requestId)
	{
		try{
			PreparedStatement ps = c.prepareStatement(command);
			ps.setInt(1, requestId);
			c.setAutoCommit(false);
			ps.execute();
			c.commit();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			try{
				c.rollback();
			}catch(SQLException e2){e.printStackTrace();}
		}
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
			try{
				c.rollback();
			}catch(SQLException e2){e.printStackTrace();}
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

	public int lastRequestFromUser(Connection c, String command, String phoneNumber)
	{
		try{
			PreparedStatement ps = c.prepareStatement(command);
			ps.setString(1, phoneNumber);
			ResultSet result = ps.executeQuery();
			if(resultEmpty(result))
			{
				//user can send a request
				System.out.println("Result Empty");
				return 0;
			}
			else
			{
				//user sent a request in the last 20 seconds
				System.out.println("Result Has something");
				return 1;
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return 1;
		}
	}

	public static boolean resultEmpty(ResultSet rs) throws SQLException {
 	   return (!rs.isBeforeFirst() && rs.getRow() == 0);
	}
}
