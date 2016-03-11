package SPATIAL;

public class Coordinate{
	double latitude;
	double longitude;
	
	public Coordinate(Double Latitude, Double Longitude){
		this.latitude = Latitude;
		this.longitude = Longitude;
	}
	public double getLongitude(){
		return this.longitude;
	}
	public double getLatitude(){
		return this.latitude;
	}
}
