package sirs.project;

import java.io.IOException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import java.util.Scanner;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConfirmationCentral
{
    private static ConfirmationCentral server;
    private ExecutorService executorService = Executors.newFixedThreadPool(10);

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

        private Socket socket;

        public ServiceRequest(Socket connection) {
            this.socket = connection;
        }

        public void run() {

            System.out.println("Started processing the request");
            try (
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ) {
                String message = null;
                if ((message = in .readLine()) != null) {

                    System.out.println(message);

                    Scanner scan = new Scanner(System.in);
                    String s = scan.next();
                    int i = scan.nextInt();

                    out.println(i + "");
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
