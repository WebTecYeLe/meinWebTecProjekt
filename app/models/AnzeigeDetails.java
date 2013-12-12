package models;

public class AnzeigeDetails extends Entity {

	public AnzeigeDetails(String id, String start, String ziel, String strecke, String uhrzeit, 
			String datum, String fahrer, int anzahl_plaetze, String email) {
		
		
		this.id = id;
		this.start = start;
		this.ziel = ziel;
		this.strecke = strecke;
		this.uhrzeit = uhrzeit;
		this.datum = datum;
		this.fahrer = fahrer;
		this.anzahl_plaetze = anzahl_plaetze;
		this.email = email;
		
	}
	
	public String id;
	public String start;	
	public String ziel;
	public String strecke;	
	public String uhrzeit;	
	public String datum;	
	public String fahrer;	
	public int anzahl_plaetze;	
	public String email;
	
	
}
