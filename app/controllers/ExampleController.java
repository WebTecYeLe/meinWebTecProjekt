package controllers;

import java.util.ArrayList;
import java.util.Date;

import play.mvc.Controller;
import play.mvc.Http.Cookie;
import play.mvc.Result;

import java.util.List;

import models.Orte;
import models.Zaehler;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class ExampleController extends Controller {

	/**
	 * weiterleitung zu statischer Seite
	 * 
	 * @return
	 */
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

			// typ = "Fahrer";

		} else {
			typ = "";
		}

		//user_statistiken lesen
		
		List<Zaehler> zaehler = new ArrayList<>();
		
		int suchergebnisse;
		int meine_mfgs = 0;
		int anfragen;
		
		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");
			
			DBCollection coll = db.getCollection("user_statistiken");
			com.mongodb.DBCursor cursor = coll.find();
			BasicDBObject query = (BasicDBObject) new BasicDBObject("username", user);
			cursor = coll.find(query);
			
			if(cursor.count() != 0) {
				
				for(DBObject s : cursor) {
					zaehler.add(new Zaehler((int) s.get("suchergebnisse"), (int) s.get("meine_mfgs"), (int) s.get("anfragen")));
					
				}
									
			} 
			
			mongoClient.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
		if (user != null) {
			return ok(views.html.anwendung.anwendung.render(
					"ProTramp Mitfahrgelegenheit", user, "", typ, ortsdetails, zaehler));
		} else {
			return ok(views.html.anwendung.anwendung.render(
					"ProTramp Mitfahrgelegenheit", "", "", typ, ortsdetails, zaehler));

		}

	}

	/**
	 * Einfache Ausgabe in Response
	 * 
	 * @return
	 */
	public static Result output() {

		// testen:
		// response().setContentType("text/html");
		// return ok("<h1>Hello World!</h1>").as("text/html");
		return ok("Hallo");
	}

	/**
	 * Besuch-Zähler mit Cookie
	 * 
	 * @return
	 */
	public static Result cookiecounter() {
		Cookie counter = request().cookie("counter");
		int count;
		if (null == counter) {
			count = 0;
		} else {
			count = Integer.valueOf(counter.value());
		}
		count++;
		response().setCookie("counter", "" + count);
		return ok("Hallo. Dein Besuch Nummer " + count);
	}

	/**
	 * einfaches scala template mit dynamischem Inhalt
	 * 
	 * @return
	 */
	public static Result simpletemplate() {
		String message = "Auf dem Server ist es diese Zeit";
		Date date = new Date();
		return ok(views.html.example.simpletemplate.render("Beispiel", message,
				date));
	}
}
