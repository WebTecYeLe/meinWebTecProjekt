package controllers;

import java.util.Map;

import play.Logger;
import play.mvc.Result;

import models.TestEingabeDatenbank;
import models.User;
import models.ValidUser;

import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;

public class TestLogin extends Controller {

	public static Result showLoginForm() {

		return redirect("/assets/html/loginform.html");
	}

	public static Result test() {

		Map<String, String[]> parameters = request().body().asFormUrlEncoded();
		// String requestBody = request().body().asText();

		if (!(parameters != null && parameters.containsKey("testEingabe"))) {
			// return badRequest("go away");
			Logger.warn("bad login request");
			return showLoginForm();
		}

		String testEingabe = parameters.get("testEingabe")[0];
	

		return ok("Hello. Dein Request war: " + parameters.keySet()
				+ " testEingabe:" + testEingabe);
	}

}
