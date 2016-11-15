package sirs.project.dispatchcentral;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.StringTokenizer
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import Request;

/**
 * Hello world!
 *
 */
public class DispatchCentral extends Thread
{
    private ServerSocket serverSocket;
    private String inputStream;
    private Request request;

    public DispatchCentral(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    private void logRequest(Request request) {
		File file = new File("log.txt");

		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());

		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("[" + request.getDate() + "]\t" + request.getUserId + "\t" + request.getMessage());

		bw.close();

		System.out.println("[DEBUG] Logged request.");
	}

    private Request processRequest(String request) {
		StringTokenizer strTok = new StringTokenizer(request)
		Request r = new Request();
		r.setUserId(strTok.nextToken());
		r.setMessage(strTok.nextToken())

		return r;
    }

    public void run() {
        while(true) {
             try {
				 System.out.println("[DEBUG] Waiting for client on port " + serverSocket.getLocalPort() + "...");
                 Socket server = serverSocket.accept();

                 System.out.println("[DEBUG] Connected to " + server.getRemoteSocketAddress());
                 DataInputStream in = new DataInputStream(server.getInputStream());
				 inputStream =in.readUTF();

				 switch(inputStream){
                    case "ping":
                        System.out.println("[DEBUG] Client sent ping. Answered with 'pong'");
                        DataOutputStream out = new DataOutputStream(server.getOutputStream());
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

    public static void main( String[] args )
    {
        int port = Integer.parseInt(args[0]);
        try {
            Thread t = new DispatchCentral(port);
            t.start();
        }catch(IOException e) {
           e.printStackTrace();
        }
    }
}
