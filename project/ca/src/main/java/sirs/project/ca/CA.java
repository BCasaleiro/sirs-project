package sirs.project.ca;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CA {
	
	final static String PROJ_DIR = System.getProperty("user.dir");
	final Logger log = LoggerFactory.getLogger(CA.class);
    private ServerSocket serverSocket;
    private ExecutorService executorService = Executors.newFixedThreadPool(10);

	
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

        private void processRequest(String request) {

        }

        public void run(){
        	
        	log.info("Started processing the request");
        	try(
        		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	){
        		String message = null;
        		if((message = in.readLine()) != null){
        			System.out.println(message);
        			out.println("Mensagem recebida!!!!");
        		}
        	}catch(IOException e){
        		log.error(e.getMessage());
        	}
        }        
    }
}
