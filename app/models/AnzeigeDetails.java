package models;

import play.data.validation.Constraints;

public class AnzeigeDetails {

	@Constraints.Required(message="Menge ist erforderlich")
	public String start;
	
	@Constraints.Required(message="Menge ist erforderlich")
	public String ziel;
	
	@Constraints.Required(message="Menge ist erforderlich")
	public String strecke;
	
	@Constraints.Required(message="Menge ist erforderlich")
	public String uhrzeit;
	
	@Constraints.Required(message="Menge ist erforderlich")
	public String datum;
	
	@Constraints.Required(message="Menge ist erforderlich")
	public String fahrer;
	
	@Constraints.Required(message="Menge ist erforderlich")
	public int anzahl_plaetze;
	
	@Constraints.Required(message="Menge ist erforderlich")
	public String email;
}
