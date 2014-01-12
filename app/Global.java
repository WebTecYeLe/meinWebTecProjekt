import play.Application;
import play.GlobalSettings;

public class Global extends GlobalSettings {

	/**
	 * is executed on application start
	 */
	@Override
	public void onStart(Application app) {
		// ensure db is up and indexes are set
		
	}
	
	@Override
	public void onStop(Application app) {
		super.onStop(app);
	}

}