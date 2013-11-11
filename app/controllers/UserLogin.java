package controllers;

import java.util.Map;

import models.User;
import models.ValidUser;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

public class UserLogin extends Controller{

	
	public static Result index(){
		Form<ValidUser> form = Form.form(ValidUser.class);
		return ok(views.html.login.render(form));
	}
	
	
	/**
	 * login mit form API und Validierung
	 * @return
	 */
	public static Result login(){
		Form<ValidUser> form = Form.form(ValidUser.class);
		
		form = form.bindFromRequest();
	
		if (form.hasErrors()) {
			
		    return badRequest(views.html.login.render(form));
		} else {
		    ValidUser user = form.get();
			return ok("Deine Eingaben: " +user.email +" " +user.password +" remember: " +user.remember);
		}
		
	}
}
