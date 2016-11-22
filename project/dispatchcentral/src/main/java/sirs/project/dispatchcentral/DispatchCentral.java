package sirs.project.dispatchcentral;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatchCentral{
	
	/*
	 * Variables
	 */
	
	final static String PROJ_DIR = System.getProperty("user.dir");
	final Logger log = LoggerFactory.getLogger(DispatchCentral.class);
	
	private static DispatchCentral server; 
    private ServerSocket serverSocket;
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    private Connection c = null;
    private DatabaseConstants dbConstants = null;
    private DatabaseFunctions dbFunctions = null;
    
    /*
     * Main method
     */   
    public static void main(String[] args) throws IOException {
    	
    	PropertyConfigurator.configure(PROJ_DIR + "/log4j.properties");
        server = new DispatchCentral();
        int port = Integer.parseInt(args[0]);
        server.checkConnectivity();
        server.runServer(port);
    }
    
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
    
    public void checkConnectivity(){
    	dbConstants = new DatabaseConstants();
    	if( connectToDatabase() == 1 ) {
	      dbFunctions = new DatabaseFunctions(dbConstants);
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
        }catch (Exception e) {
          e.printStackTrace();
          System.err.println(e.getClass().getName()+": "+e.getMessage());
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
            serverSocket = new ServerSocket(serverPort); 

            while(true) {
                try {
                    Socket s = serverSocket.accept();
                    log.info("Connection accepted from " + s.getInetAddress().getHostAddress());
                    executorService.submit(new ServiceRequest(s));
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
     * Function to be called to shutdown the server
     */
    private void stopServer() {
        executorService.shutdownNow();
        try {
            serverSocket.close();
        } catch (IOException e) {
            log.error("Error while closing the server");
            log.error(e.getMessage());
        }
        System.exit(0);
    }
    
    /*
     * Nested class to process the requests
     */
    class ServiceRequest implements Runnable {

        private Socket socket;

        public ServiceRequest(Socket connection) {
            this.socket = connection;
        }

        private Request processRequest(String request) {
            StringTokenizer strTok = new StringTokenizer(request, ",");
            Request r = new Request();
            r.setUserId(strTok.nextToken());
            r.setMessage(strTok.nextToken());

            return r;
        }
        
        public void testRequestdbFunctions(Request request) {
        	dbFunctions.insertRequest(c, dbConstants.insertRequest, request);
        	int id = dbFunctions.getRequestId(c, dbConstants.getRequestId, request);
        	dbFunctions.setDispatched(c, dbConstants.setDispatched, id);
        }

        public void run(){
        	
        	log.info("Started processing the request");
        	try(
        		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	){
        		String message = null;
        		if((message = in.readLine()) != null){
        			Request request = processRequest(message);
	    	        log.info(request.getUserId() + ", " + request.getMessage());
        			//insert on db
	            	dbFunctions.insertRequest(c, dbConstants.insertRequest, request);

        			System.out.println(message);
        			out.println("Help is on the way");
        		}
        	}catch(IOException e){
        		log.error(e.getMessage());
        	}
        }        
    }
	
}
