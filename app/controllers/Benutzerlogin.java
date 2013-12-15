package controllers;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.AnzeigeDetails;
import models.Orte;
import models.Zaehler;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http.Session;
import play.mvc.Result;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
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

		// Kontrollvariable wird fuer die Suche verwendet
		boolean fertig = false;
		boolean testeFahrer = false;

		// Verbindung mit der Datenbank aufbauen
		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			// Die Collection user abrufen. Collection user enthaelt
			// Benutzerdaten
			DBCollection coll = db.getCollection("user");

			// Eine Abfrage die nach Username und Password sucht
			BasicDBObject query = new BasicDBObject("username", nutzer).append(
					"password", kennwort);
			com.mongodb.DBCursor cursor = coll.find();
			cursor = coll.find(query);

			String sucheDocumentFuerUser = "";
			// Dokumente ueberpruefen
			try {
				while (cursor.hasNext()) {
					sucheDocumentFuerUser += cursor.next();
				}
			} finally {
				cursor.close();
			}

			// Falls Username und Password korrekt, dann Login-Prozess
			// erfolgreich
			if (!sucheDocumentFuerUser.isEmpty()) {
				fertig = true;
				if (sucheDocumentFuerUser.contains("Mitfahrer")) {
					testeFahrer = true;
				}
			}

			// Falls der Benutzer eine Email statt Username eingegeben hat wird
			// die Email und das Password ueberprueft
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
				// Falls Email und Password korrekt, dann Login-Prozess
				// erfolgreich
				if (!sucheDocumentFuerEmail.isEmpty()) {
					fertig = true;
					if (sucheDocumentFuerEmail.contains("Mitfahrer")) {
						testeFahrer = true;
					}
				}
			}

			mongoClient.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		String info = "";
		String typ = "";

		List<Orte> ortsdetails = new ArrayList<>();
		List<DBObject> feld;

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = (DBCollection) db.getCollection("mfg");
			BasicDBObject query = new BasicDBObject();

			feld = coll.find(query).toArray();

			String vergleich = "";

			for (DBObject s : feld) {

				if (vergleich.contains(s.get("start").toString())) {

				} else {
					vergleich += s.get("start").toString();
					ortsdetails.add(new Orte((String) s.get("start")));
				}

				if (vergleich.contains(s.get("ziel").toString())) {

				} else {
					vergleich += s.get("ziel").toString();
					ortsdetails.add(new Orte((String) s.get("ziel")));
				}

				if (vergleich.contains(s.get("strecke").toString())) {

				} else {
					vergleich += s.get("strecke").toString();
					ortsdetails.add(new Orte((String) s.get("strecke")));
				}

			}

			mongoClient.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		// user_statistiken lesen

		List<Zaehler> zaehler = new ArrayList<>();

		int suchergebnisse;
		int meine_mfgs = 0;
		int anfragen;

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = db.getCollection("user_statistiken");
			com.mongodb.DBCursor cursor = coll.find();
			BasicDBObject query = (BasicDBObject) new BasicDBObject("username",
					nutzer);
			cursor = coll.find(query);

			if (cursor.count() != 0) {

				for (DBObject s : cursor) {
					zaehler.add(new Zaehler((int) s.get("suchergebnisse"),
							(int) s.get("meine_mfgs"), (int) s.get("anfragen")));

				}

			}

			mongoClient.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (fertig) {
			// Falls ein Fahrer sich einloggt, soll die Zielseite
			// dementsprechend gestaltet werden.
			if (testeFahrer) {
				session("typ", "Mitfahrer");
			} else {
				session("typ", "Fahrer");
			}

			typ = session("typ");

			if (!typ.equals("Fahrer")) {
				typ = "";
			}

			session("connected", nutzer);
			return ok(views.html.anwendung.anwendung.render(
					"ProTramp Mitfahrgelegenheit", nutzer, info, typ,
					ortsdetails, zaehler));
		} else {
			info = "Einloggen nicht erfolgreich. Versuchen Sie es erneut.";
			return ok(views.html.anwendung.anwendung.render(
					"ProTramp Mitfahrgelegenheit", "", info, typ, ortsdetails, zaehler));
		}

	}

	public static Result logout() {
		String info = "";
        String nutzer = session("connected");
		session().clear();

		List<Orte> ortsdetails = new ArrayList<>();
		List<DBObject> feld;

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = (DBCollection) db.getCollection("mfg");
			BasicDBObject query = new BasicDBObject();

			feld = coll.find(query).toArray();

			String vergleich = "";

			for (DBObject s : feld) {

				if (vergleich.contains(s.get("start").toString())) {

				} else {
					vergleich += s.get("start").toString();
					ortsdetails.add(new Orte((String) s.get("start")));
				}

				if (vergleich.contains(s.get("ziel").toString())) {

				} else {
					vergleich += s.get("ziel").toString();
					ortsdetails.add(new Orte((String) s.get("ziel")));
				}

				if (vergleich.contains(s.get("strecke").toString())) {

				} else {
					vergleich += s.get("strecke").toString();
					ortsdetails.add(new Orte((String) s.get("strecke")));
				}

			}

			mongoClient.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

            // user_statistiken lesen

		List<Zaehler> zaehler = new ArrayList<>();

		int suchergebnisse;
		int meine_mfgs = 0;
		int anfragen;

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = db.getCollection("user_statistiken");
			com.mongodb.DBCursor cursor = coll.find();
			BasicDBObject query = (BasicDBObject) new BasicDBObject("username",
					nutzer);
			cursor = coll.find(query);

			if (cursor.count() != 0) {

				for (DBObject s : cursor) {
					zaehler.add(new Zaehler((int) s.get("suchergebnisse"),
							(int) s.get("meine_mfgs"), (int) s.get("anfragen")));

				}

			}

			mongoClient.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}


		return ok(views.html.anwendung.anwendung.render(
				"ProTramp Mitfahrgelegenheit", "", info, "", ortsdetails, zaehler));

	}

}
