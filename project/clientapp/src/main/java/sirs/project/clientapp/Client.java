package sirs.project.clientapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.net.UnknownHostException;

import java.util.Date;
import java.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


public class Client
{

	private SSLSocket ssldispatchsocket = null;
	private SSLSocket sslcasocket = null;
	private String phoneNumber = null;

	public Client(String serverName, int dispatchport, int caport) throws UnknownHostException, IOException{
		SSLSocketFactory factory=(SSLSocketFactory) SSLSocketFactory.getDefault();
        try {
        	ssldispatchsocket = (SSLSocket) factory.createSocket(serverName, dispatchport);
			sslcasocket = (SSLSocket) factory.createSocket(serverName, caport);
        }catch(IOException e){
        	e.printStackTrace();
        }
	}

	private String hashText(String text) {
		try{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(text.getBytes("UTF-8"));
			byte[] digest = md.digest();
			return new String(Base64.getEncoder().encodeToString(digest));
		} catch(NoSuchAlgorithmException|UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null;
	}

	private void exitConnection() throws IOException{
		ssldispatchsocket.close();
		sslcasocket.close();
	}

	public void setPhoneNumber(String number){
		phoneNumber = number;
	}

	private void sendRequest() throws IOException{
		 PrintWriter out = new PrintWriter(ssldispatchsocket.getOutputStream(), true);
		 String id = hashText(phoneNumber + (new Date()).getTime());
		 out.println( id + "," +  phoneNumber + ",HELP!");
		 BufferedReader in = new BufferedReader(new InputStreamReader(ssldispatchsocket.getInputStream()));

		 String fromServer = in.readLine();
		 if(fromServer != null){
			 System.out.println(fromServer);
		 }
	}

	private void clearScreen(){
		final String ANSI_CLS = "\u001b[2J";
        final String ANSI_HOME = "\u001b[H";
        System.out.print(ANSI_CLS + ANSI_HOME);
        System.out.flush();
	}

	private void contactCA(){
        try {
			PrintWriter out = new PrintWriter(sslcasocket.getOutputStream(), true);
    		BufferedReader in = new BufferedReader(new InputStreamReader(sslcasocket.getInputStream()));
    		out.println("Primeiro contacto");
    		String message = null;
    		if((message = in.readLine()) != null){
    			System.out.println(message);
    		}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public static void main(String[] args ){
    	String serverName = args[0];
        int dispatchport = Integer.parseInt(args[1]);
        int caport = Integer.parseInt(args[2]);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        Client clientClass = null;
		try {
			clientClass = new Client(serverName, dispatchport, caport);
			clientClass.clearScreen();
			System.out.println("[DEBUG] Contacting Certificate Authority");
			clientClass.contactCA();
			System.out.print("Insert your phone number: ");
			clientClass.setPhoneNumber(br.readLine());
		} catch (IOException e) {
			System.err.println("[ERROR] Failed to initialize client");
			return ;
		}

        try{
        	System.out.println("[DEBUG] Connecting to " + serverName + " on port " + dispatchport);
        	System.out.println("[DEBUG] Successfully connected to dispatch central");

        	System.out.println("Choose one option from the following");
        	System.out.println("-----------------------------------");
        	System.out.println("1) Send Request");
        	System.out.println("2) Exit");

        	String switchCase = br.readLine();

        	switch(switchCase){
	        	case "1":
	        		clientClass.sendRequest();
	        		break;
	        	default:
	        		break;
        	}

            clientClass.exitConnection();
        }catch(IOException e){
        	e.printStackTrace();
        }
    }
}
