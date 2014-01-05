package models;

public class Person extends Entity {
	
	public Person(String id, String user, String vorname, int alt, String email, String tel) {
		
		this.id = id;
		this.user = user;
		this.vorname = vorname;
		this.alt = alt;
		this.email = email;
		this.tel = tel;
		
	}
	
	public String id;
	public String user;
	public String vorname;
	public int alt;
	public String email;
	public String tel;

}
