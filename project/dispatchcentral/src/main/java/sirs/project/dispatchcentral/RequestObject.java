
package sirs.project.dispatchcentral;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import sirs.project.clientrequest.Request;

public class RequestObject {
	
    private Request request;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public RequestObject(Request request, ObjectOutputStream out, ObjectInputStream in) {
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
    
    public ObjectOutputStream getOut(){
        return out;
    }

    public void setOut(ObjectOutputStream out){
        this.out = out;
    }
    public ObjectInputStream getIn(){
        return in;
    }

    public void setIn(ObjectInputStream in){
        this.in = in;
    }
}
