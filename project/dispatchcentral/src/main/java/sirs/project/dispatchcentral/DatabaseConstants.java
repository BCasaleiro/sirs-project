package sirs.project.dispatchcentral;

public final class DatabaseConstants{

	public DatabaseConstants(){}

	public String requestsTableCreation = 
			"CREATE TABLE IF NOT EXISTS REQUESTS "+
            "(ID 			TEXT	PRIMARY KEY	NOT NULL, "+
            "PHONENUMBER   	TEXT				NOT NULL, "+
            "LOCALIZATION 	TEXT				NOT NULL, "+
            "MESSAGE		TEXT				NOT NULL, "+
            "TIMESTAMP		DATE 				NOT NULL);";

    public String ratingsTableCreation = 
    		"CREATE TABLE IF NOT EXISTS RATINGS " +
    		"(PHONENUMBER	TEXT	PRIMARY KEY NOT NULL, "+
    		"RATING 		INT					NOT NULL);";



}