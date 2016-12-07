package sirs.project.dispatchcentral;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Base64;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.StringTokenizer;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sirs.project.clientrequest.Request;

public class DispatchCentral{

	/*
	 * Variables
	 */
	final static String PROJ_DIR = System.getProperty("user.dir");
	final static String CA_IP = "localhost";
	final static String ALIAS = "dispatchcentral";
	final static String KEYSTORE_PATH = PROJ_DIR + "/src/main/resources/dispatchcentralkeystore.jks";
	private final static String TRUSTSTORE_PATH = PROJ_DIR + "/src/main/resources/cakeystore.jks";
	private final static String CC_ALIAS = "confirmationcentral";
	private final static char[] PASS = "changeit".toCharArray();
	final static int CA_PORT = 9998;

	final Logger log = LoggerFactory.getLogger(DispatchCentral.class);

	private static DispatchCentral server;
    private ExecutorService executorService = Executors.newFixedThreadPool(10);

	private static SSLSocket sslconfirmationsocket = null;
	private static ObjectOutputStream out_cc;
	private static ObjectInputStream in_cc;
	
    private Connection c = null;
    private DatabaseConstants dbConstants = null;
    private DatabaseFunctions dbFunctions = null;

    private static PriorityQueue < RequestObject > queue = null;

    /*
     * Main method
     */
    public static void main(String[] args) throws IOException {
		String serverName = args[2];
		SSLSocketFactory factory=(SSLSocketFactory) SSLSocketFactory.getDefault();
        PropertyConfigurator.configure(PROJ_DIR + "/log4j.properties");
        server = new DispatchCentral();
        int port = Integer.parseInt(args[0]);
		int confirmationPort =  Integer.parseInt(args[1]);

        sslconfirmationsocket = (SSLSocket) factory.createSocket(serverName, confirmationPort);
        out_cc = new ObjectOutputStream(sslconfirmationsocket.getOutputStream());
		in_cc = new ObjectInputStream(sslconfirmationsocket.getInputStream());

        queue = new PriorityQueue < RequestObject > (comparator);
        server.checkConnectivity();
        server.runServer(port);

    }

    public static Comparator < RequestObject > comparator = new Comparator < RequestObject > () {@
        Override
        public int compare(RequestObject a, RequestObject b) {
            return (int)(b.getRequest().getPriority() - a.getRequest().getPriority());
        }
    };

    /*
     * Database functions
     */
    public void createNecessaryTables() {
        dbFunctions.createTable(c, dbConstants.requestsTableCreation);
        dbFunctions.createTable(c, dbConstants.ratingsTableCreation);

        //Just for test
        //dbTestingFunction();
    }

    public void dbTestingFunction() {
        dbFunctions.insertUser(c, dbConstants.insertUser, "911111111");
        dbFunctions.updateRating(c, dbConstants.updateRating, "911111111", 20);
        System.out.println(dbFunctions.userExists(c, dbConstants.listPhoneNumbers, "9123213"));
        System.out.println(dbFunctions.userRating(c, dbConstants.userRating, "911111111"));
    }

    public void checkConnectivity() {
        dbConstants = new DatabaseConstants();
        if (connectToDatabase() == 1) {
            dbFunctions = new DatabaseFunctions();
            createNecessaryTables();
            System.out.println("Created necessary tables");
        } else {
            System.out.println("Error Connecting to Database");
        }
    }

    public int connectToDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/emergenciesdb",
                "postgres", "123");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
        return 1;
    }

    /*
     * Function that will wait for incoming connections
     */
    private void runServer(int serverPort) {
        try {
            log.info("Starting Server in port " + serverPort);

            SSLServerSocketFactory factory=(SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket sslServerSocket=(SSLServerSocket) factory.createServerSocket(serverPort);

            executorService.submit(new QueueRemover());

            while (true) {
                try {
                	SSLSocket sslsocket = (SSLSocket) sslServerSocket.accept();
                    log.info("Connection accepted from " + sslsocket.getInetAddress().getHostAddress());
                    executorService.submit(new ServiceRequest(sslsocket));
                } catch(IOException ioe) {
                    log.error("Error accepting connection");
                    log.error(ioe.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Error starting Server on port " + serverPort);
            log.error(e.getMessage());
        }
    }

    class QueueRemover implements Runnable {
        public QueueRemover() {}

        public void run() {
            while (true) {
                if (!queue.isEmpty()) {
                    synchronized(queue) {
                        RequestObject requestObject = queue.poll();
                        serveRequest(requestObject);
                        queue.notify();
                    }
                }
                try {
                    System.out.println("Thread sleeping");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {

                }
            }
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

        public int expectedTime(String clientCoords)
        {
            int RADIUS_EARTH = 6371;
            int VELOCITY = 65;
            //tecnico coordinates
            String coords = "38.7367117,-9.1380472";
            System.out.println("ClientCoords: " + clientCoords);
            String [] d1 = coords.split(",");
            String [] d2 = clientCoords.split(",");
            
            double lngDistance = Math.toRadians(Double.parseDouble(d2[0]) - Double.parseDouble(d1[0]));
            double latDistance = Math.toRadians(Double.parseDouble(d2[1]) - Double.parseDouble(d1[1]));

            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
              + Math.cos(Math.toRadians(Double.parseDouble(d1[1]))) * Math.cos(Math.toRadians(Double.parseDouble(d2[1])))
              * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            double distance = (double) (RADIUS_EARTH * c); 
            System.out.println("Km: "+ distance + " time: "+(int)Math.round(distance*60/VELOCITY));
            return (int)Math.round(distance*60/VELOCITY);
        }

		private boolean verifySignature(String message){
			Certificate cert = getCertificate(TRUSTSTORE_PATH, CC_ALIAS);
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

		private int sendConfirmationRequest(Request request) throws IOException{
			try{
				String message = "Rate the request (-5 to 5) with id " + request.getId() + " from user " + request.getUserId() + ": " ;
				out_cc.writeObject(message + "," + signAnswer(message));				
				String fromServer;
				try {
					fromServer = (String)in_cc.readObject();
					if(fromServer != null){
						 if(verifySignature(fromServer)){
							 System.out.println("[DEBUG] Message was from a trusted source");
							 StringTokenizer strTok = new StringTokenizer(fromServer, ",");
				             String answer = strTok.nextToken();
							 return Integer.parseInt(answer);
						 }
					 }
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return -10;
			}catch(EOFException e){
				return -10;
			}
			 
		}

        public void serveRequest(RequestObject requestObject) {
            Request request = requestObject.getRequest();
            ObjectOutputStream out = requestObject.getOut();

            String message = "Help is on the way. Expected Time: "+ expectedTime(request.getLocalization())+"Minutes."; 
            //System.out.println("Removed "+ request.getUserId()+" Priority: "+request.getPriority());
            try {
				out.writeObject(message + "," + signAnswer(message));  
				int rating = sendConfirmationRequest(request);
				if(rating<6 && rating>-6){
					System.out.println("Rating: " + rating);
                    dbFunctions.updateRating(c, dbConstants.updateRating, request.getUserId(), rating);
					//TODO: rate the user accordingly
				}           
			} catch (IOException e) {
				e.printStackTrace();
			}
            log.info("Removed "+ request.getUserId()+"Priority: "+request.getPriority());
            //dbFunctions.insertRequest(c, dbConstants.insertRequest, request);
        }

        //Needs testing
        public void updatePriorities(int value)
        {
            if(queue.size()==0) { return; }
            else {
              RequestObject firstRequest = queue.poll();
              firstRequest.getRequest().updatePriority(value);
              queue.add(firstRequest);

              RequestObject request = null;
              while((request = queue.poll())!=firstRequest)
              {
                  request.getRequest().updatePriority(value);
                  queue.add(request);
              }
            }
        }
    }

    /*
     * Nested class to process the requests
     */
    class ServiceRequest implements Runnable {

        private SSLSocket sslsocket;

        private Firewall firewall = new Firewall(c, log);
        public ServiceRequest(SSLSocket connection) {
            this.sslsocket = connection;
        }

        public void testRequestdbFunctions(Request request) {
            dbFunctions.insertRequest(c, dbConstants.insertRequest, request);
            int id = dbFunctions.getRequestId(c, dbConstants.getRequestId, request);
            dbFunctions.setDispatched(c, dbConstants.setDispatched, id);
        }

        private boolean verifySignature(Request request){
        	Certificate cert = getUserCertificate(request.getUserId());
        	PublicKey pk = cert.getPublicKey();
        	try {
        		byte[] signaturebytes = Base64.getDecoder().decode(request.getSignature());
				byte[] messagebytes = (request.getId()+","+request.getUserId()+","+request.getMessage()).getBytes();
				Signature sig = Signature.getInstance("SHA1WithRSA");
				sig.initVerify(pk);
				sig.update(messagebytes);
				return sig.verify(signaturebytes);
			} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
				e.printStackTrace();
				return false;
			}
        }

        private Certificate getUserCertificate(String phoneNumber){
    		SSLSocketFactory factory=(SSLSocketFactory) SSLSocketFactory.getDefault();
    		try {
    			SSLSocket sslcasocket = (SSLSocket) factory.createSocket(CA_IP, CA_PORT);
    			ObjectOutputStream toServer = new ObjectOutputStream(sslcasocket.getOutputStream());
				ObjectInputStream fromServer = new ObjectInputStream(sslcasocket.getInputStream());
				toServer.writeObject(phoneNumber);
				byte[] b = (byte[])fromServer.readObject();
				return CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(b));
    		}catch(IOException | ClassNotFoundException | CertificateException e){
            	e.printStackTrace();
            	return null;
            }
        }

        public void run() {

            log.info("Started processing the request");
            try (
            	ObjectOutputStream out = new ObjectOutputStream(sslsocket.getOutputStream());
            	ObjectInputStream in = new ObjectInputStream(sslsocket.getInputStream());
            ) {
                Request request = null;
                if ((request = (Request)in.readObject()) != null) {
                    log.info("ID: " + request.getUserId());
                    log.info("Message: " + request.getMessage());
                    log.info("Signature: " + request.getSignature());
                    log.info("Priority: " + request.getPriority());

                    if(verifySignature(request)){
                    	//pass through the firewall filter
                        System.out.println("Inserting user on Db");
                        dbFunctions.insertUser(c, dbConstants.insertUser,  request.getUserId());
                        int userRating = dbFunctions.userRating(c, dbConstants.userRating, request.getUserId());
                        request.setPriority(userRating);
                        firewall.filterRequest(new RequestObject(request, out, in), queue);

                    }else{
                    	log.error("Invalid Signature!!");
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                log.error(e.getMessage());
            }
        }
    }

}
