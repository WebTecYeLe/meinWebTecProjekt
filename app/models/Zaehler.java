package models;

public class Zaehler extends Entity {
	
	public Zaehler(int suchergebnisse, int meine_mfgs, int anfragen) {
		this.suchergebnisse = suchergebnisse;
		this.meine_mfgs = meine_mfgs;
		this.anfragen = anfragen;
		
		
	}
	
	public int suchergebnisse;
	public int meine_mfgs;
	public int anfragen;

}
