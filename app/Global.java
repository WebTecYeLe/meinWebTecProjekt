import models.Thing;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import db.DBConnect;
import db.ThingDB;

public class Global extends GlobalSettings {

	/**
	 * is executed on application start
	 */
	@Override
	public void onStart(Application app) {
		// ensure db is up and indexes are set
		ThingDB.init();
		
		ThingDB things = ThingDB.get();
		
		Logger.info("Things: " + things.count());
		if (things.count() == 0) {
			things.create(new Thing("POI", "Spielplatz 1", 8.9639978, 47.6724461));
			things.create(new Thing("POI", "Spielplatz 2", 9.12, 47.6));
			things.create(new Thing("POI", "Spielplatz 3", 8.4, 47.6724461));
			things.create(new Thing("POI", "HTWG", 9.170449, 47.668009));
			things.create(new Thing("POI", "Fahrradbrücke", 9.174612, 47.668934));
			
			Logger.info("Things: " + things.count());
		}
	}
	
	@Override
	public void onStop(Application app) {
		DBConnect.dispose();
		super.onStop(app);
	}

}