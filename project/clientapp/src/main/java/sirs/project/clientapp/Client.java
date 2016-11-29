package sirs.project.clientapp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Client 
{
	private final static String PROJ_DIR = System.getProperty("user.dir");
	private final static String KEYSTORE_PATH = PROJ_DIR + "/src/main/resources/clientappkeystore.jks";
	private final static char[] PASS = "changeit".toCharArray();
    private final static String ALIAS = "clientapp";
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
	
	private void exitConnection() throws IOException{
		ssldispatchsocket.close();
		sslcasocket.close();
	}
	
	public void setPhoneNumber(String number){
		phoneNumber = number;
	}
	
	private void sendRequest() throws IOException{
		 PrintWriter out = new PrintWriter(ssldispatchsocket.getOutputStream(), true);
		 out.println(phoneNumber + ",HELP!");
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
	
	private byte[] getCertificate(){
		FileInputStream fIn = null;
		try {
			fIn = new FileInputStream(KEYSTORE_PATH);
			KeyStore keystore = KeyStore.getInstance("JKS");
		    keystore.load(fIn, PASS);
		    return keystore.getCertificate(ALIAS).getEncoded();
		} catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
			e.printStackTrace();
			return null;
		}    		
	}
	
	private void contactCA(){
        try {
			PrintWriter out = new PrintWriter(sslcasocket.getOutputStream(), true);
    		BufferedReader in = new BufferedReader(new InputStreamReader(sslcasocket.getInputStream()));
    		//Send certificate
    		byte[] cert = getCertificate();
			if(cert != null){
				ObjectOutputStream toServer = new ObjectOutputStream(sslcasocket.getOutputStream());
				ObjectInputStream fromServer = new ObjectInputStream(sslcasocket.getInputStream());
				toServer.writeObject("Sending Certificate");
				if(fromServer.readObject().equals("Proceed")){
					toServer.writeObject(cert);
				}
			}    		
			String message = null;
    		if((message = in.readLine()) != null){
  			
    			//Here we can ask for the certificate for dispatch central
    			System.out.println(message);
    		}
		} catch (IOException | ClassNotFoundException e) {
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
