package controllers;

public class NearSearchParams{
	public Integer distance;
	public Double longitude;
	public Double latitude;
	public NearSearchParams(Integer distance, Double longitude, Double latitude) {
		super();
		this.distance = distance;
		this.longitude = longitude;
		this.latitude = latitude;
	}
	public NearSearchParams() {
	
	}
	
}