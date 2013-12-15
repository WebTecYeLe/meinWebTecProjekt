package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.Logger;

public class Konto extends Controller {
	
	public static Result kontoeinstellungen_index() {
		String nutzer = session("connected");
		
		return ok(views.html.konto.kontoeinstellungen.render("ProTramp Mitfahrgelegenheit", nutzer));
	}
	

}
