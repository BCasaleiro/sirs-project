package sirs.project.dispatchcentral;

import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class RequestObject {
    private Request request;
    private PrintWriter out;
    private BufferedReader in;

    public RequestObject(Request request, PrintWriter out, BufferedReader in) {
        this.request = request;
        this.out = out;
        this.in = in;
    }

    public Request getRequest(){
        return request;
    }

    public void setRequest(Request request){
        this.request = request;
    }
    
    public PrintWriter getOut(){
        return out;
    }

    public void setOut(PrintWriter out){
        this.out = out;
    }
    public BufferedReader getIn(){
        return in;
    }

    public void setIn(BufferedReader in){
        this.in = in;
    }
}
