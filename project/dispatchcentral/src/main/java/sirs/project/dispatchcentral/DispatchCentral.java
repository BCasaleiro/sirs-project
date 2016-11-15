package sirs.project.dispatchcentral;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Hello world!
 *
 */
public class DispatchCentral extends Thread
{
	private ServerSocket serverSocket;
	private Connection c = null;

	public DispatchCentral(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		connectToDatabase();
	}

	public void connectToDatabase()
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
