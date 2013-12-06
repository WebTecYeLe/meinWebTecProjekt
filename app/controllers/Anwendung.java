package controllers;

import play.mvc.Controller;
import play.mvc.Result;

public class Anwendung extends Controller {
	
	public static Result mfg_anbieten() {
		
		String nutzer = session("connected");
		String typ = session("typ");		
		
		return ok(views.html.anwendung.anwendung_mfg_anbieten.render("ProTramp Mitfahrgelegenheit", nutzer, "", typ));
		
	}
	
	public static Result anbieten() {
		
		String nutzer = session("connected");
		String typ = session("typ");		
		
		
		
		
		return ok("Ok");
		
	}
	
	

}
