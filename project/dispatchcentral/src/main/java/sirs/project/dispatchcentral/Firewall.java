package sirs.project.dispatchcentral;

import java.sql.Connection;
import java.sql.*;
import java.util.Date;

public class Firewall
{
	private Connection c = null;
	private DatabaseFunctions dbFunctions = null;
	private DatabaseConstants dbConstants = null;

	public Firewall(Connection c){
		this.c = c;
		this.dbFunctions = new DatabaseFunctions();
		this.dbConstants = new DatabaseConstants();
		
	}

	public int filterRequest(Request request)
	{
		//verify if already exists
		//dont serve it - Write to client
		verifyIfExists(request);
		
		//verify if something is blank
		//dont serve it - Write to client
		verifyIfBlankNull(request);

		//verify if the location is to random
		//verifyPreviousLocation(request);

		//verify if there's something strange in the message
		//get the priority of user
		//reduce priority and insert on queue
		//verifyStrangeMessage(request);


		return 0;
	}

	public boolean verifyIfExists(Request request)
	{
		return dbFunctions.requestExists(c, dbConstants.requestExists, request);
	}
	

	public boolean verifyIfBlankNull(Request request)
	{
		if(request.getId().equals("") || request.getId().equals(null))
		{
			return true;
		}
		if(request.getUserId().equals("") || request.getUserId().equals(null))
		{
			return true;
		}
		if(request.getDate()==null)
		{
			return true;
		}
		if(request.getMessage().equals(""))
		{
			return true;
		}
		if(request.getPriority()<=0)
		{
			return true;
		}
		return false;
	}

	public boolean verifyDate(Date date)
	{

		if(date.getTime() - (new Date()).getTime() > 0)
		{
			return false;
		}

		return true;
	}

	public int verifyStrangeMessage(Request request)
	{
		return 0;
	}

	public int verifyPreviousLocation(Request request)
	{
		return 0;
	}

}