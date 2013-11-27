package models;

import java.util.Date;

import play.data.validation.Constraints;

/**
 * could be anything
 * @author mike
 *
 */
public class Thing extends Entity{
	
	/**
	 * name it
	 */
	@Constraints.Required
	public String name;
	
	/**
	 * location latitude longitude
	 */
	public double[] lonLat;
	
	/**
	 * ewhat type is it
	 */
	@Constraints.Required
	public String what;
	
	public Date dateCreated;
	
	public Thing(){
		
	}
	
	public Thing(String what, String name, double longitude, double latitude){
		this.what = what;
		this.name = name;
		this.lonLat = new double[]{longitude, latitude};
	}
	
	public void specifyLocation(double longitude, double latitude){
		lonLat = new double[]{longitude, latitude};
	}
	
	@Override
	public String toString() {
		return super.toString() +" what:" +what +" dateCreated:" +dateCreated +" lonLat:" +toLocString();
	}
	
	private String toLocString(){
		if(lonLat != null && lonLat.length == 2){
			return lonLat[0] +"," +lonLat[1];
		}else return "null";
	}
	
	/**
	 * NOTE: cannot name it getXXX if there is no such field
	 * @return
	 */
	public double latitude(){
		return hasLocation() ? lonLat[1] : Double.NaN;
	}
	public double longitude(){
		return hasLocation() ? lonLat[0] : Double.NaN;
	}
	
	public boolean hasLocation(){
		return lonLat != null && lonLat.length == 2;
	}
}
