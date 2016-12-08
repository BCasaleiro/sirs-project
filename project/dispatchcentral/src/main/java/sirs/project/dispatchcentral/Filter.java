package sirs.project.dispatchcentral;

import java.sql.Connection;
import java.sql.*;
import java.util.Date;
import java.io.ObjectOutputStream;
import java.util.PriorityQueue;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sirs.project.clientrequest.Request;
public class Filter
{
	private Connection c = null;
	private DatabaseFunctions dbFunctions = null;
	private DatabaseConstants dbConstants = null;
	private Logger log = null;

	public Filter(Connection c, Logger log){
		this.c = c;
		this.dbFunctions = new DatabaseFunctions();
		this.dbConstants = new DatabaseConstants();
		this.log = log;
		
	}

	public int filterRequest(RequestObject requestObject)
	{

		Request request = requestObject.getRequest();

		if(verifyIfBlockedUser(request))
		{
			return -4;
		}

		//check if user sent a request in the last 20 seconds
		if(dbFunctions.lastRequestFromUser(c, dbConstants.lastRequestFromUser, request.getUserId())==1)
		{
			
			dbFunctions.updateRating(c, dbConstants.updateRating, request.getUserId(), -1);
			return -1;
		}

		//verify if already exists
		//dont serve it - Write to client
		if(verifyIfExists(request))
		{
			return -2;
		} 
		//verify if something is blank
		//dont serve it - Write to client
		
		if(verifyIfBlankNull(request))
		{
			return -3;
		}

		if(verifyDate(request.getDate())==false)
		{
			return -5;
		}
		
		
        return 0;

	}

	public boolean verifyIfBlockedUser(Request request)
	{
		if(request.getPriority() < 5)
		{
			return true;
		}
		return false;
	}

	public boolean verifyIfExists(Request request)
	{
		return dbFunctions.requestExists(c, dbConstants.requestExists, request);
	}
	

	public boolean verifyIfBlankNull(Request request)
	{
		if(request.getId()==null || request.getId().isEmpty() || request.getId().trim().length()==0)
		{
			return true;
		}
		
		if(request.getUserId()==null || request.getUserId().isEmpty() || request.getUserId().trim().length()==0)
		{
			return true;
		}
		
		if(request.getDate()==null)
		{
			return true;
		}

		if(request.getMessage()==null || request.getMessage().isEmpty() || request.getMessage().trim().length()==0)
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

}