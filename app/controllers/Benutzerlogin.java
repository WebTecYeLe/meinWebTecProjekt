package controllers;

import java.net.UnknownHostException;
import java.util.Map;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Http.Session;
import play.mvc.Result;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class Benutzerlogin extends Controller {
    
    

	public static Result login() {

		Map<String, String[]> parameters = request().body().asFormUrlEncoded();

		// Parameteruebergaben werden ueberprueft
		if (!(parameters != null && parameters.containsKey("nutzer") && parameters
				.containsKey("kennwort"))) {

			Logger.warn("bad login request");
			return ok("Versuchen Sie es erneut.");
		}

		// Parameterwerte werden ausgelesen
		String nutzer = parameters.get("nutzer")[0];
		String kennwort = parameters.get("kennwort")[0];

		//Kontrollvariable wird fuer die Suche verwendet
		boolean fertig = false;

		//Verbindung mit der Datenbank aufbauen
		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");
			//Die Collection user abrufen. Collection user enthaelt Benutzerdaten
			DBCollection coll = db.getCollection("user");
			
			//Eine Abfrage die nach Username und Password sucht
			BasicDBObject query = new BasicDBObject("username", nutzer).append(
					"password", kennwort);
			com.mongodb.DBCursor cursor = coll.find();
			cursor = coll.find(query);

			String sucheDocumentFuerUser = "";
			//Dokumente ueberpruefen
			try {
				while (cursor.hasNext()) {
					sucheDocumentFuerUser += cursor.next();
				}
			} finally {
				cursor.close();
			}

			//Falls Username und Password korrekt, dann Login-Prozess erfolgreich
			if (!sucheDocumentFuerUser.isEmpty()) {
				fertig = true;
			}

			//Falls der Benutzer eine Email statt Username eingegeben hat wird die Email und das Password ueberprueft
			if (!fertig) {
				query = new BasicDBObject("email", nutzer).append("password",
						kennwort);
				cursor = coll.find(query);

				String sucheDocumentFuerEmail = "";

				try {
					while (cursor.hasNext()) {
						sucheDocumentFuerEmail += cursor.next();
					}
				} finally {
					cursor.close();
				}
				//Falls Email und Password korrekt, dann Login-Prozess erfolgreich
				if (!sucheDocumentFuerEmail.isEmpty()) {
					fertig = true;
				}
			}

			mongoClient.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

        String info = "";

		if (fertig) {
			
			session("connected", nutzer);			
			return ok(views.html.anwendung.anwendung.render("ProTramp Mitfahrgelegenheit", nutzer, info));
		} else {
			info = "Einloggen nicht erfolgreich. Versuchen Sie es erneut.";
			return ok(views.html.anwendung.anwendung.render("ProTramp Mitfahrgelegenheit", "", info));
		}

	}
	
	
	public static Result logout() {
        String info = "";
		session().clear();
		return ok(views.html.anwendung.anwendung.render("ProTramp Mitfahrgelegenheit", "", info));
		
	}
}
