package sirs.project.dispatchcentral;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.*;


/**
 * Hello world!
 *
 */
public class DispatchCentral extends Thread
{
	private ServerSocket serverSocket;
	private Connection c = null;
	private DatabaseConstants dbConstants = null;
	private DatabaseFunctions dbFunctions = null;


	public DispatchCentral(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		dbConstants = new DatabaseConstants();
		if(connectToDatabase()==1)
		{
			dbFunctions = new DatabaseFunctions(dbConstants);
			createNecessaryTables();
			System.out.println("Created necessary tables");
		}
		else
		{
			System.out.println("Error Connecting to Database");
			return;
		}
	}

	public void createNecessaryTables()
	{
		dbFunctions.execCmd(c, dbConstants.requestsTableCreation);
		dbFunctions.execCmd(c, dbConstants.ratingsTableCreation);
		
		//Just for test
		dbTestingFunction();
	}

	public void dbTestingFunction()
	{
		dbFunctions.insertUser(c, dbConstants.insertUser, "911111111");
		dbFunctions.updateRating(c, dbConstants.updateRating, "911111111", 20);
		System.out.println(dbFunctions.userExists(c, dbConstants.listPhoneNumbers, "9123213"));
	}

	public int connectToDatabase()
	{
		try {
        	Class.forName("org.postgresql.Driver");
         	c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/emergenciesdb",
            "postgres", "123");
      	}catch (Exception e) {
        e.printStackTrace();
        System.err.println(e.getClass().getName()+": "+e.getMessage());
        System.exit(0);
      }
      System.out.println("Opened database successfully");
      return 1;
	}
	
	public void run() {
		while(true) {
	         try {
	            System.out.println("[DEBUG] Waiting for client on port " + serverSocket.getLocalPort() + "...");
	            Socket server = serverSocket.accept();
	            
	            System.out.println("[DEBUG] Connected to " + server.getRemoteSocketAddress());
	            DataInputStream in = new DataInputStream(server.getInputStream());
	            
	            String received = in.readUTF();
	            
	            switch(received){
		            case "ping":
		            	System.out.println("[DEBUG] Client sent ping. Answered with 'pong'");
		            	DataOutputStream out = new DataOutputStream(server.getOutputStream());
			            out.writeUTF("pong");
			            server.close();
			            break;
			        default:
			        	System.out.println(received);
	            }	            
	            
	         }catch(SocketTimeoutException s) {
	            System.out.println("Socket timed out!");
	            break;
	         }catch(IOException e) {
	            e.printStackTrace();
	            break;
	         }
	      }
	}
	
    public static void main( String[] args )
    {
    	int port = Integer.parseInt(args[0]);
        try {
        	Thread t = new DispatchCentral(port);
        	t.start();
        }catch(IOException e) {
           e.printStackTrace();
        }
    }
}
