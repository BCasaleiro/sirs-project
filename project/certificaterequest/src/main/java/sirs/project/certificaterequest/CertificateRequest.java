package sirs.project.certificaterequest;

import java.io.Serializable;
import java.security.cert.Certificate;

public class CertificateRequest implements Serializable{

	private static final long serialVersionUID = -1309477691085156765L;  
	private Certificate cert = null;
	private String phoneNumber;
	
	public Certificate getCert() {
		return cert;
	}
	public void setCert(Certificate cert) {
		this.cert = cert;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
}
