package sirs.project;

import java.net.Socket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Base64;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.security.KeyStore;
import java.security.PublicKey;
import java.security.Signature;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.KeyStoreException;
import java.security.SignatureException;
import java.security.InvalidKeyException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class ConfirmationCentral {

    private static ConfirmationCentral server;
    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    final static String PROJ_DIR = System.getProperty("user.dir");
    final static String KEYSTORE_PATH = PROJ_DIR + "/src/main/resources/confirmationcentralkeystore.jks";
    private final static String TRUSTSTORE_PATH = PROJ_DIR + "/src/main/resources/cakeystore.jks";
    private final static String DC_ALIAS = "dispatchcentral";
    private final static char[] PASS = "changeit".toCharArray();
    final static String ALIAS = "confirmationcentral";

    public static void main( String[] args ) {
        server = new ConfirmationCentral();
        int port = Integer.parseInt(args[0]);

        server.runServer(port);
    }

    private void runServer(int serverPort) {
        try {
            System.out.println("Starting Server in port " + serverPort);

            SSLServerSocketFactory factory=(SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket sslServerSocket=(SSLServerSocket) factory.createServerSocket(serverPort);

            while (true) {
                try {
                	SSLSocket sslsocket = (SSLSocket) sslServerSocket.accept();
                    System.out.println("Connection accepted from " + sslsocket.getInetAddress().getHostAddress());
                    executorService.submit(new ServiceRequest(sslsocket));
                } catch(IOException ioe) {
                    System.out.println("Error accepting connection");
                    System.out.println(ioe.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error starting Server on port " + serverPort);
            System.out.println(e.getMessage());
        }
    }

    class ServiceRequest implements Runnable {

        private SSLSocket sslsocket;

        public ServiceRequest(SSLSocket connection) {
            this.sslsocket = connection;
        }

        private String signAnswer(String message){
        	try {
    			byte[] b = message.getBytes("UTF-8");
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

        public void run() {

            System.out.println("Started processing the request");
            try (
                ObjectOutputStream out = new ObjectOutputStream(sslsocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(sslsocket.getInputStream());
            ) {
                String message = null;
                if ((message = (String)in.readObject()) != null) {
                	if(verifySignature(message)){
                        System.out.println(message);

                        Scanner scan = new Scanner(System.in);
                        String s = scan.next();
                        int i = scan.nextInt();

                        String answer = i + "";
                        out.writeObject(answer + "," + signAnswer(answer));
                    }else{
                    	System.out.println("Invalid Signature!!");
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
