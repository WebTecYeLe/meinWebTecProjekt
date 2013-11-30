package controllers;

import java.net.UnknownHostException;
import java.util.Map;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class Registrierung extends Controller {

	public static Result registrieren() {

		return redirect("/assets/html/registrierung.html");

	}

	public static Result abschicken() {

		Map<String, String[]> parameters = request().body().asFormUrlEncoded();
		// String requestBody = request().body().asText();
		System.out.println("hi");
		if (!(parameters != null
				&& parameters.containsKey("exampleInputUsername")
				&& parameters.containsKey("exampleInputEmailRegistrierung")
				&& parameters.containsKey("exampleInputPasswordRegistrierung")
				&& parameters.containsKey("exampleInputPasswordRegistrierungValidierung")
				&& parameters.containsKey("mfg_fahrer")
				&& parameters.containsKey("exampleInputVorname")
				&& parameters.containsKey("exampleInputName")
				&& parameters.containsKey("Tag")
				&& parameters.containsKey("Monat")
				&& parameters.containsKey("Jahr")
				&& parameters.containsKey("exampleInputTel"))) {
			// return badRequest("go away");
			Logger.warn("bad login request");
			return redirect("/assets/html/registrierung.html");
		}

		
		String username = parameters.get("exampleInputUsername")[0];
		String email = parameters.get("exampleInputEmailRegistrierung")[0];
		String password = parameters.get("exampleInputPasswordRegistrierung")[0];
		String passwordValidierung = parameters.get("exampleInputPasswordRegistrierungValidierung")[0];
		
		String[] fahrer = parameters.get("mfg_fahrer");
		
		if(fahrer[0].equals("Fahrer")) {
			
			
		}
		
		
		if(!password.equals(passwordValidierung)) {
			return redirect("/assets/html/registrierung.html");
			
		}
		
		String[] anrede = parameters.get("optionsRadios");
		String titel;
		
		if(anrede[0].equals("Herr")) {
			titel = "Herr";			
			
		} else {
			titel = "Frau";
		}
		
		
		
		String tag = parameters.get("Tag")[0];
		String monat = parameters.get("Monat")[0];
		String jahr = parameters.get("Jahr")[0];
		String geburtsdatum = tag+"."+monat+"."+jahr;
		
		String vorname = parameters.get("exampleInputVorname")[0];
		String name = parameters.get("exampleInputName")[0];
		String telefon = parameters.get("exampleInputTel")[0];
		

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");
			DBCollection coll = db.getCollection("user");
			BasicDBObject doc = new BasicDBObject("username", username).
					append("email", email).
					append("password", password).
					append("geburtsdatum", geburtsdatum).
					append("titel", titel).
					append("vorname", vorname).
					append("name", name).
					append("tel", telefon);
			coll.insert(doc);
			mongoClient.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return redirect("/assets/html/start_unlogged.html");
	}
}
