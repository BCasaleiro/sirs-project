package sirs.project.dispatchcentral;

import java.util.Date;

public class Request {

    private String id;
    private String userId;
    private Date date;
    private String message;
    private boolean dispatched;

    //for database test
    private String localization = "38.7369914,-9.1303272";

    public Request() {
        this.date = new Date();
    }

    public Request(String userId, String message) {
        this.userId = userId;
        this.message = message;
        this.date = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(Sring id) {
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
}
