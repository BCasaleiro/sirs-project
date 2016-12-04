package sirs.project.clientrequest;

import java.io.Serializable;
import java.util.Date;

public class Request implements Serializable{

	private static final long serialVersionUID = 2899772797212095685L;
	private String id;
    private String userId;
    private Date date;
    private String message;
    private String signature;
    private boolean dispatched;
    private int priority;

    //for database test
    private String localization = "38.7369914,-9.1303272";

    public Request() {
        this.date = new Date();
    }

    public Request(String id, String userId, String message) {
    	this.id = id;
        this.userId = userId;
        this.message = message;
        this.date = new Date();
    }

    public void updatePriority(int value)
    {
        this.priority+=value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public boolean isDispatched() {
        return dispatched;
    }

    public void setDispatched(boolean dispatched) {
        this.dispatched = dispatched;
    }

    public String getLocalization(){
        return localization;
    }

    public void setLocalization(String localization){
        this.localization=localization;
    }

    public int getPriority(){
        return priority;
    }

    public void setPriority(int priority){
        this.priority = priority;
    }
}

