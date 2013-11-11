package controllers;

import java.util.Map;

import models.User;
import models.ValidUser;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

public class Login extends Controller{

	
	public static Result showLoginForm(){
		
		return redirect("/assets/html/loginform.html");
	}
	
	/**
	 * login formular mit requets API
	 * @return
	 */
	public static Result login1(){
		
		Map<String, String[]> parameters = request().body().asFormUrlEncoded();
		//String requestBody =  request().body().asText();
		
		if( ! (parameters != null && parameters.containsKey("email") && parameters.containsKey("password"))){
			//return badRequest("go away");
			Logger.warn("bad login request");
			return showLoginForm();
		}
		
		String  email = parameters.get("email")[0];
		String password = parameters.get("password")[0];
		
		return ok("Hello. Dein Request war: " +parameters.keySet() +" useremail:" +email +" password:" +password);
	}
	
	/**
	 * login mit form API
	 * @return
	 */
	public static Result login2(){
		Form<User> form = Form.form(User.class);
		
		User user = form.bindFromRequest().get();
		return ok("Deine Eingaben: " +user.email +" " +user.password +" remember: " +user.remember);
	}
	
	/**
	 * login mit form API und Validierung
	 * @return
	 */
	public static Result login3(){
		Form<ValidUser> form = Form.form(ValidUser.class);
		
		form = form.bindFromRequest();
		
		if (form.hasErrors()) {
		    return badRequest("war nix. Fehler: " +form.errors());
		} else {
		    ValidUser user = form.get();
			return ok("Deine Eingaben: " +user.email +" " +user.password +" remember: " +user.remember);
		}
		
	}
	
	public static Result dynamicform() {
	    DynamicForm requestData = Form.form().bindFromRequest();
	    String email = requestData.get("email");
	    String password = requestData.get("password");
	    return ok("Your data: " + email + " " + password);
	}
}
