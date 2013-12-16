package models;

public class KontoDetails extends Entity {

	public KontoDetails(String registrierungsdatum, String username, String email ) {
		this.registrierungsdatum = registrierungsdatum;
		this.username = username;
		this.email = email;
	}
	
	public String registrierungsdatum;
	public String username;
	public String email;
}
