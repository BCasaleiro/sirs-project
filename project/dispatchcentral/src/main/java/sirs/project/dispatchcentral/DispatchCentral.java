package sirs.project.dispatchcentral;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Hello world!
 *
 */
public class DispatchCentral extends Thread
{
	private ServerSocket serverSocket;
	   
	public DispatchCentral(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(10000);
	}
	
	public void run() {
	      while(true) {
	         try {
	            System.out.println("[DEBUG] Waiting for client on port " + serverSocket.getLocalPort() + "...");
	            Socket server = serverSocket.accept();
	            
	            System.out.println("[DEBUG] Connected to " + server.getRemoteSocketAddress());
	            DataInputStream in = new DataInputStream(server.getInputStream());
	            
	            System.out.println(in.readUTF());
	            DataOutputStream out = new DataOutputStream(server.getOutputStream());
	            out.writeUTF("Reply to hello");
	            server.close();
	            
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
