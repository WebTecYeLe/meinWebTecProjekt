package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

import models.Orte;
import models.Zaehler;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

/*
 * HTWG Konstanz
 * Webtechnologien im 7. Semester
 * 
 * Abschlussdatum: 13.01.2014 
 * Team: Dominique Lebert, Erkan Yediok
 * 
 *  
 * Github: WebTecYeLe
 * 
 * */


//Diese Klasse kümmert sich darum, wenn Benutzer die Seite zum ersten Mal aufrufen.
//Anhand Sessions wird überprüft ob der Benutzer eingeloggt ist oder nicht

public class ExampleController extends Controller {

	@SuppressWarnings("unchecked")
	public static Result index() {

		// Hier muss der User überprüft werden ob er bereits schon eingeloggt
		// ist

		String user = session("connected");
		String typ = session("typ");

		List<Orte> ortsdetails = new ArrayList<>();
		List<DBObject> feld;

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = (DBCollection) db.getCollection("mfg");
			BasicDBObject query = new BasicDBObject();

			feld = coll.find(query).toArray();

			String vergleich = "";

			// Auflistung der unterschiedlichen Orte
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

		if (typ != null) {
			if (!typ.equals("Fahrer")) {
				typ = "";

			}

		} else {
			typ = "";
		}

		// Aktuelles Datum in Format dd.MM.yyyy holen
		long difference = 0;
		DateFormat date = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		Date todayDate = Calendar.getInstance().getTime();

		Date dateDBParsen = null;

		List<String> anfrageListe = new ArrayList<>();

		int anfragen = 0;
		String email = "";

		//Datenbankverbindung aufbauen
		//Hier werden gestartete Anfragen aufgelistet, die vom Datum her 30min in der Vergangenheit bzw. in der Zukunft liegen
		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = db.getCollection("user");

			BasicDBObject query = (BasicDBObject) new BasicDBObject("username",
					user);
			List<DBObject> feldEmail = coll.find(query).toArray();

			for (DBObject s : feldEmail) {
				email = (String) s.get("email");
			}

			coll = db.getCollection("mfg");

			query = (BasicDBObject) new BasicDBObject("email", email);

			feld = coll.find(query).toArray();

			for (DBObject s : feld) {
				String dateDB = (String) s.get("datum") + " "
						+ (String) s.get("uhrzeit");

				//1800000 sind angegeben in ms. Dies entspricht 30min.
				try {

					dateDBParsen = date.parse(dateDB);
					difference = todayDate.getTime() - dateDBParsen.getTime()
							- 1800000;

				} catch (Exception e) {
					e.printStackTrace();
				}

				//Falls es Anfragen gibt die gestartet wurden und vom Datum gültig sind werden übernommen
				if (difference <= 0) {
					anfrageListe = (List<String>) s.get("gestarteteAnfragen");

					if (anfrageListe.size() >= 1) {

						anfragen += anfrageListe.size();

					}

				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// user_statistiken bearbeiten und lesen

		List<Zaehler> zaehler = new ArrayList<>();

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = db.getCollection("user_statistiken");
			com.mongodb.DBCursor cursor = coll.find();
			BasicDBObject query = (BasicDBObject) new BasicDBObject("username",
					user);
			cursor = coll.find(query);

			BasicDBObject doc = new BasicDBObject();
			doc.put("anfragen", anfragen);

			BasicDBObject account = new BasicDBObject();
			account.put("$set", doc);

			coll.update(query, account);

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
		
		//Falls hier der Benutzer vorher noch nicht eingeloggt ist, wird er auf die Standardseite weitergeleitet
		
		String facebook = "";
		
		if(session("facebook_logged") != null) {
			facebook = "Ja";
		}
		
		if (user != null) {
			return ok(views.html.anwendung.anwendung.render(
					"ProTramp Mitfahrgelegenheit", user, "", typ, ortsdetails,
					zaehler, facebook));
		} else {
			return ok(views.html.anwendung.anwendung.render(
					"ProTramp Mitfahrgelegenheit", "", "", typ, ortsdetails,
					zaehler, facebook));

		}

	}

}
