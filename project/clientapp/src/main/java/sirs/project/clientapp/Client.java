package sirs.project.clientapp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Date;
import java.util.StringTokenizer;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import sirs.project.certificaterequest.CertificateRequest;
import sirs.project.clientrequest.Request;


public class Client{

	/*
	 * Variables
	 */
	private final static String PROJ_DIR = System.getProperty("user.dir");
	private final static String KEYSTORE_PATH = PROJ_DIR + "/src/main/resources/clientappkeystore.jks";
	private final static String TRUSTSTORE_PATH = PROJ_DIR + "/src/main/resources/cakeystore.jks";
	private final static char[] PASS = "changeit".toCharArray();
    private final static String ALIAS = "clientapp";
    private final static String DC_ALIAS = "dispatchcentral";
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
	
	private String signRequest(String request){
		try {
			byte[] b = request.getBytes("UTF-8");
			Signature sig = Signature.getInstance("SHA1WithRSA");
			sig.initSign(getPrivateKey());
			sig.update(b);
			byte[] signatureBytes = sig.sign();
			return Base64.getEncoder().encodeToString(signatureBytes);
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private boolean verifySignature(String message){
		Certificate cert = getCertificate(TRUSTSTORE_PATH, DC_ALIAS);
		PublicKey pk = cert.getPublicKey();
		StringTokenizer strTok = new StringTokenizer(message, ",");
		String answer = strTok.nextToken();
		String signature = strTok.nextToken();
    	try {
    		byte[] signaturebytes = Base64.getDecoder().decode(signature);
			byte[] messagebytes = answer.getBytes();
			Signature sig = Signature.getInstance("SHA1WithRSA");
			sig.initVerify(pk);
			sig.update(messagebytes);
			return sig.verify(signaturebytes);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
			return false;
		}
	}

	
	private void sendRequest() throws IOException{
		 ObjectOutputStream out = new ObjectOutputStream(ssldispatchsocket.getOutputStream());
		 ObjectInputStream in = new ObjectInputStream(ssldispatchsocket.getInputStream());
		 String id = hashText(phoneNumber + (new Date()).getTime());
		 Request request = new Request(id, phoneNumber, "HELP!");
		 String message = id + "," +  phoneNumber + ",HELP!";
		 request.setSignature(signRequest(message));
		 if(request.getSignature() != null){
			 out.writeObject(request);
		
		 }
		
		String fromServer;
		try {
			fromServer = (String)in.readObject();
			if(fromServer != null){
				 if(verifySignature(fromServer)){
					 //System.out.println("[DEBUG] Message was from a trusted source");
					 System.out.println("Received: " + fromServer.split(",")[0]);
				 }			 
			 }
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		 
	}

	private void clearScreen(){
		final String ANSI_CLS = "\u001b[2J";
        final String ANSI_HOME = "\u001b[H";
        System.out.print(ANSI_CLS + ANSI_HOME);
        System.out.flush();
	}
	
	private Certificate getCertificate(String path, String alias){
		FileInputStream fIn = null;
		try {
			fIn = new FileInputStream(path);
			KeyStore keystore = KeyStore.getInstance("JKS");
		    keystore.load(fIn, PASS);
		    return keystore.getCertificate(alias);
		} catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
			e.printStackTrace();
			return null;
		}    		
	}
	
	private PrivateKey getPrivateKey(){
		FileInputStream fIn = null;
		try {
			fIn = new FileInputStream(KEYSTORE_PATH);
			KeyStore keystore = KeyStore.getInstance("JKS");
		    keystore.load(fIn, PASS);
		    return (PrivateKey)keystore.getKey(ALIAS, PASS);
		} catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException | UnrecoverableKeyException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void contactCA(){
        try {
    		//Send certificate
        	Certificate cert = getCertificate(KEYSTORE_PATH, ALIAS);
			if(cert != null){
				ObjectOutputStream toServer = new ObjectOutputStream(sslcasocket.getOutputStream());
				ObjectInputStream fromServer = new ObjectInputStream(sslcasocket.getInputStream());
				toServer.writeObject("Sending Certificate");
				if(fromServer.readObject().equals("Proceed")){
					CertificateRequest cr = new CertificateRequest();
					cr.setCert(cert);
					cr.setPhoneNumber(phoneNumber);
					System.out.println("[DEBUG] Writing class to socket");
					toServer.writeObject(cr);
					toServer.flush();
					toServer.close();
				}
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
			System.out.print("Insert your phone number: ");
			clientClass.setPhoneNumber(br.readLine());
			System.out.println("[DEBUG] Contacting Certificate Authority");
			clientClass.contactCA();
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
