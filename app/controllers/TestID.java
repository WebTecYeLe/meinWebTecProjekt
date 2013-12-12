package controllers;

import play.mvc.Controller;
import play.mvc.Result;

public class TestID extends Controller {
	
	public static Result testen(String id){
		
		
		return ok("id: "+id);
	}

}
