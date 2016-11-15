package sirs.project.clientapp;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client 
{
	
	private InputStream inFromServer = null;
	private Socket client = null;
	private DataInputStream in = null;
	private OutputStream outServer = null;
	private DataOutputStream out = null;
	private String phoneNumber = null;
	
	public Client(String serverName, int port) throws UnknownHostException, IOException{
		client = new Socket(serverName, port);
		inFromServer = client.getInputStream();
		in = new DataInputStream(inFromServer);
		outServer = client.getOutputStream();
		out = new DataOutputStream(outServer);
	}
	
	private void exitConnection() throws IOException{
		client.close();
	}
	
	private void ping() throws IOException{
		out.writeUTF("ping");    
        System.out.println(in.readUTF() + "\n");
	}

	public void setPhoneNumber(String number){
		phoneNumber = number;
	}
	
	private void sendRequest() throws IOException{
		out.writeUTF(phoneNumber + ",HELP!");
	}
	
	private void clearScreen(){
		final String ANSI_CLS = "\u001b[2J";
        final String ANSI_HOME = "\u001b[H";
        System.out.print(ANSI_CLS + ANSI_HOME);
        System.out.flush();
	}
	
    public static void main(String[] args ){
    	String serverName = args[0];
        int port = Integer.parseInt(args[1]);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      
        Client clientClass = null;
		try {
			clientClass = new Client(serverName, port);
			clientClass.clearScreen();
			System.out.print("Insert your phone number: ");
			clientClass.setPhoneNumber(br.readLine());
		} catch (IOException e) {
			System.err.println("[ERROR] Failed to initialize client");
			return ;
		}
        
        try{
        	System.out.println("[DEBUG] Connecting to " + serverName + " on port " + port);      	
        	System.out.println("[DEBUG] Successfully connected to server");
        	
        	String switchCase = null;

        	do{
            	System.out.println("Choose one option from the following");
            	System.out.println("-----------------------------------");
            	System.out.println("1) Ping server [DEBUG]");
            	System.out.println("2) Send Request");
            	System.out.println("3) Exit");
            	
            	
            	switchCase = br.readLine();
            	
            	switch(switchCase){
    	        	case "1":
    	        		clientClass.ping();
    	        		break;
    	        	case "2":
    	        		clientClass.sendRequest();
    	        		break;
            	}            	
        	} while(!switchCase.equals("3"));
        		
            clientClass.exitConnection();
        }catch(IOException e){
        	e.printStackTrace();
        }
    }
}
