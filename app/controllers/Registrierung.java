package controllers;

import java.net.UnknownHostException;
import java.util.GregorianCalendar;
import java.util.Map;

import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
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
		
		//Parameteruebergaben werden ueberprueft
		if (!(parameters != null
				&& parameters.containsKey("exampleInputUsername")
				&& parameters.containsKey("exampleInputEmailRegistrierung")
				&& parameters.containsKey("exampleInputPasswordRegistrierung")
				&& parameters.containsKey("exampleInputPasswordRegistrierungValidierung")
				&& parameters.containsKey("exampleInputVorname")
				&& parameters.containsKey("exampleInputName")
				&& parameters.containsKey("Tag")
				&& parameters.containsKey("Monat")
				&& parameters.containsKey("Jahr")
				&& parameters.containsKey("exampleInputTel"))) {
			
			Logger.warn("bad login request");
			return redirect("/assets/html/registrierung.html");
		}

		//Parameterwerte werden ausgelesen
		String username = parameters.get("exampleInputUsername")[0];
		String email = parameters.get("exampleInputEmailRegistrierung")[0];
		String password = parameters.get("exampleInputPasswordRegistrierung")[0];
		String passwordValidierung = parameters.get("exampleInputPasswordRegistrierungValidierung")[0];
		
		String[] fahrer = parameters.get("optionsFahrer");
		String fahrertyp;
		
		//Unterscheidung ob der Nutzer ein Fahrer oder Mitfahrer ist
		if(fahrer[0].equals("Fahrer")) {
			fahrertyp = "Fahrer";
			
		} else {
			fahrertyp = "Mitfahrer";
		}
		
		//Ueberpruefung ob das Password korrekt wiederholt eingegeben wurde
		if(!password.equals(passwordValidierung)) {
			return redirect("/assets/html/registrierung.html");
			
		}
		
		//Die Anrede wird ermittelt
		String[] anrede = parameters.get("optionsRadios");
		String titel;
		
		if(anrede[0].equals("Herr")) {
			titel = "Herr";			
			
		} else {
			titel = "Frau";
		}
		
		
		//Parameterwerte werden ausgelesen
		String tag = parameters.get("Tag")[0];
		String monat = parameters.get("Monat")[0];
		String jahr = parameters.get("Jahr")[0];
		String geburtsdatum = tag+"."+monat+"."+jahr;
		
		String vorname = parameters.get("exampleInputVorname")[0];
		String name = parameters.get("exampleInputName")[0];
		String telefon = parameters.get("exampleInputTel")[0];
		
		String alter = "";
		int alt;
		
		//Das Alter wird zunaechst ermittelt
		GregorianCalendar today = new GregorianCalendar();
		GregorianCalendar past = new GregorianCalendar(Integer.parseInt(jahr), Integer.parseInt(monat) - 1, Integer.parseInt(tag));
		
		
		long difference = today.getTimeInMillis() - past.getTimeInMillis();
		int days = (int)(difference / (1000 * 60 * 60 * 24));
		double years = (double)days / 365;
		
		StringBuilder sb = new StringBuilder();
		sb.append("");
		sb.append(years);
		alter = sb.toString();
		
		//Bestimmung des Alters als ganze Zahl
		alt = (int) Math.floor(Double.parseDouble(alter));
		
		//Prueft ob der Fahrer mindestens 18 Jahre alt ist
		if(fahrertyp.equals("Fahrer") && alt < 18) {
			return redirect("/assets/html/registrierung.html");
		}
		
		
		//Daten werden an die Datenbank uebertragen
		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");
			DBCollection coll = db.getCollection("user");
			BasicDBObject doc = new BasicDBObject("username", username).
					append("email", email).
					append("password", password).
					append("titel", titel).
					append("fahrertyp", fahrertyp).
					append("geburtsdatum", geburtsdatum).
					append("alt", alt).
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
