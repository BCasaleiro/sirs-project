package sirs.project.clientapp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client 
{
    public static void main( String[] args )
    {
        String serverName = args[0];
        int port = Integer.parseInt(args[1]);
        
        try{
        	System.out.println("[DEBUG] Connecting to " + serverName + " on port " + port);
        	Socket client = new Socket(serverName, port);
        	System.out.println("[DEBUG] Successfully connected to server");
        	
        	OutputStream outServer = client.getOutputStream();
        	DataOutputStream out = new DataOutputStream(outServer);
        	
        	out.writeUTF("Helo!");
        	
        	InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);          
            System.out.println("Server says " + in.readUTF());
            client.close();
        }catch(IOException e){
        	e.printStackTrace();
        }
    }
}
