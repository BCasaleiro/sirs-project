package sirs.project.dispatchcentral;

public final class DatabaseConstants{

	public DatabaseConstants(){}

	String requestsTableCreation = 
			"CREATE TABLE IF NOT EXISTS REQUESTS "+
            "(ID 			SERIAL	PRIMARY KEY	, "+
            "PHONENUMBER   	TEXT				NOT NULL, "+
            "LOCALIZATION 	TEXT				NOT NULL, "+
            "MESSAGE		TEXT				NOT NULL, "+
            "TIMESTAMP		DATE 				NOT NULL);";

    String ratingsTableCreation = 
    		"CREATE TABLE IF NOT EXISTS RATINGS " +
    		"(PHONENUMBER	TEXT	PRIMARY KEY NOT NULL, "+
    		"RATING 		INT					NOT NULL);";

    String insertUser = "INSERT INTO RATINGS (PHONENUMBER, RATING) VALUES (?,100);";

    String updateRating = "UPDATE RATINGS SET RATING=RATING+? WHERE PHONENUMBER=?;";
}