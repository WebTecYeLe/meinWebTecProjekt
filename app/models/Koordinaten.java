package models;

public class Koordinaten extends Entity {
	
	public Koordinaten(String ort, String lat, String lng) {
		
		this.ort = ort;
		this.lat = lat;
		this.lng = lng;
		
	}
	
	public String ort;
	public String lat;
	public String lng;

}
