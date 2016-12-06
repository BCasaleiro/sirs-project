package sirs.project.dispatchcentral;

import java.sql.Connection;
import java.sql.*;
import java.util.Date;
import java.io.ObjectOutputStream;
import java.util.PriorityQueue;
import java.io.IOException;
import java.io.ObjectInputStream;

import sirs.project.clientrequest.Request;
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

	public void filterRequest(RequestObject requestObject, PriorityQueue queue)
	{
		ObjectOutputStream out = requestObject.getOut();
		Request request = requestObject.getRequest();

		//verify if already exists
		//dont serve it - Write to client
		
		//verify if something is blank
		//dont serve it - Write to client

		if(verifyIfExists(request) || verifyIfBlankNull(request))
		{
			try {
				out.writeObject("Invalid Request");  
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		if(verifyDate(request.getDate())==false)
		{
			try {
				out.writeObject("Bad Date Input");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		//verify if the location is to random
		//verifyPreviousLocation(request);

		//verify if there's something strange in the message
		//get the priority of user
		//reduce priority and insert on queue
		//verifyStrangeMessage(request);

		synchronized(queue) {
            queue.add(requestObject);
            System.out.println("Queue size: " + queue.size());
            try{
              queue.wait();
            }catch(InterruptedException e)
            {
              e.printStackTrace();
            }
        }

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

}