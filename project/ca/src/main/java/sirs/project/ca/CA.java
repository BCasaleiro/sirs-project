package sirs.project.ca;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sirs.project.certificaterequest.CertificateRequest;


public class CA {
	
	final static String PROJ_DIR = System.getProperty("user.dir");
	final Logger log = LoggerFactory.getLogger(CA.class);
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private Map<String, Certificate> certificates = new HashMap<String, Certificate>();
	
    public static void main(String[] args ){
    	PropertyConfigurator.configure(PROJ_DIR + "/log4j.properties");
        int port = Integer.parseInt(args[0]);
        CA ca = new CA();
        ca.runServer(port); 
    }
    
    /*
     * Function that will wait for incoming connections
     */
    private void runServer(int serverPort) {  
        try {
            log.info("Starting Server in port " + serverPort);
            SSLServerSocketFactory factory=(SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket sslServerSocket=(SSLServerSocket) factory.createServerSocket(serverPort);
            while(true) {
                try {
        			SSLSocket sslsocket = (SSLSocket) sslServerSocket.accept();
                    log.info("Secured connection accepted from " + sslsocket.getInetAddress().getHostAddress());
                    executorService.submit(new ServiceRequest(sslsocket));
                } catch(IOException ioe) {
                    log.error("Error accepting connection");
                    log.error(ioe.getMessage());
                }
            }
        }catch(IOException e) {
            log.error("Error starting Server on port " + serverPort);
            log.error(e.getMessage());
        }
    }
    
    /*
     * Nested class to process the requests
     */
    class ServiceRequest implements Runnable {

        private SSLSocket sslsocket;

        public ServiceRequest(SSLSocket connection) {
            this.sslsocket = connection;
        }

        public void run(){
        	
        	log.info("Started processing the request");
        	try(
        		ObjectInputStream fromClient = new ObjectInputStream(sslsocket.getInputStream());
        		ObjectOutputStream toClient = new ObjectOutputStream(sslsocket.getOutputStream());
        	){
        		String message = (String)fromClient.readObject();
        		if(message.equals("Sending Certificate")){
        			toClient.writeObject("Proceed");
        			CertificateRequest cr = (CertificateRequest)fromClient.readObject();
        			certificates.put(cr.getPhoneNumber(), cr.getCert());
        			log.info("Successfully added certificate to Map");
        		}
        		else{ //When dispatch central requests for an user certificate
        			//Message will contain the phone number to search in the map
        			Certificate cert = certificates.get(message);
        			try {
        				//Send the certificate to dispatch central
						toClient.writeObject(cert.getEncoded());
					} catch (CertificateEncodingException e) {
						e.printStackTrace();
					}
        		}
        	}catch(IOException | ClassNotFoundException e){
        		e.printStackTrace();
        		log.error(e.getMessage());
        	}
        }        
    }
}
