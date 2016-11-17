package sirs.project.dispatchcentral;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.StringTokenizer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* Hello world!
*
*/
public class DispatchCentral extends Thread
{
  final Logger logger = LoggerFactory.getLogger(DispatchCentral.class);

  private ServerSocket serverSocket;
  private String inputStream;
  private Request request;
  private Connection c = null;
  private DatabaseConstants dbConstants = null;
  private DatabaseFunctions dbFunctions = null;

  public DispatchCentral(int port) throws IOException {
    serverSocket = new ServerSocket(port);
    dbConstants = new DatabaseConstants();
    if( connectToDatabase() == 1 ) {
      dbFunctions = new DatabaseFunctions(dbConstants);
      createNecessaryTables();
      System.out.println("Created necessary tables");
    } else {
      System.out.println("Error Connecting to Database");
    }
  }

  public void createNecessaryTables() {
    dbFunctions.execCmd(c, dbConstants.requestsTableCreation);
    dbFunctions.execCmd(c, dbConstants.ratingsTableCreation);

    //Just for test
    dbTestingFunction();
  }

  public void dbTestingFunction() {
    dbFunctions.insertUser(c, dbConstants.insertUser, "911111111");
    dbFunctions.updateRating(c, dbConstants.updateRating, "911111111", 20);
    System.out.println(dbFunctions.userExists(c, dbConstants.listPhoneNumbers, "9123213"));
    System.out.println(dbFunctions.userRating(c, dbConstants.userRating, "911111111"));

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

  private void logRequest(Request request) {
    logger.info("[" + request.getDate() + "]\t" + request.getUserId() + "\t" + request.getMessage());

    System.out.println("[DEBUG] Logged request.");
  }

  private Request processRequest(String request) {
    StringTokenizer strTok = new StringTokenizer(request, ",");
    Request r = new Request();
    r.setUserId(strTok.nextToken());
    r.setMessage(strTok.nextToken());

    return r;
  }

  public void run() {
    while(true) {
      try {
        System.out.println("[DEBUG] Waiting for client on port " + serverSocket.getLocalPort() + "...");
        Socket server = serverSocket.accept();

        System.out.println("[DEBUG] Connected to " + server.getRemoteSocketAddress());
        DataInputStream in = new DataInputStream(server.getInputStream());
        DataOutputStream out = new DataOutputStream(server.getOutputStream());
        inputStream =in.readUTF();

        switch(inputStream){
          case "ping":
          System.out.println("[DEBUG] Client sent ping. Answered with 'pong'");
          out.writeUTF("pong");
          server.close();
          break;
          default:
          System.out.println("[DEBUG] Client sent request.");
          request = processRequest(inputStream);
          logRequest(request);
          out.writeUTF("Help is on the way!");
          server.close();
        }

      }catch(SocketTimeoutException s) {
        System.out.println("Socket timed out!");
        break;
      }catch(IOException e) {
        e.printStackTrace();
        break;
      }
    }
  }

  public static void main( String[] args ) {
    int port = Integer.parseInt(args[0]);
    try {
      Thread t = new DispatchCentral(port);
      t.start();
    }catch(IOException e) {
      e.printStackTrace();
    }
  }
}
