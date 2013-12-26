package controllers;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import models.AnzeigeDetails;
import models.Zaehler;

import org.bson.types.ObjectId;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;

public class Anwendung extends Controller {

	public static Result mfg_anbieten_index() {

		String nutzer = session("connected");
		String typ = session("typ");

		// user_statistike lesen

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

		return ok(views.html.anwendung.anwendung_mfg_anbieten.render(
				"ProTramp Mitfahrgelegenheit", nutzer, "", typ, zaehler));

	}

	public static Result anbieten() {

		String nutzer = session("connected");
		String typ = session("typ");

		// user_statistike lesen

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

		List<AnzeigeDetails> details = new ArrayList<>();

		Map<String, String[]> parameters = request().body().asFormUrlEncoded();

		// Parameteruebergaben werden ueberprueft
		if (!(parameters != null
				&& parameters.containsKey("mfg_anbieten_start")
				&& parameters.containsKey("mfg_anbieten_ziel")
				&& parameters.containsKey("mfg_anbieten_strecke")
				&& parameters.containsKey("Tag")
				&& parameters.containsKey("Monat")
				&& parameters.containsKey("Jahr")
				&& parameters.containsKey("Stunden")
				&& parameters.containsKey("Minuten") && parameters
					.containsKey("mfg_anbieten_plaetze"))) {

			Logger.warn("bad login request");
			return ok("Versuchen Sie es erneut.");
		}

		// Parameterwerte werden ausgelesen
		String start = parameters.get("mfg_anbieten_start")[0];
		String ziel = parameters.get("mfg_anbieten_ziel")[0];
		String strecke = parameters.get("mfg_anbieten_strecke")[0];
		String tag = parameters.get("Tag")[0];
		String monat = parameters.get("Monat")[0];
		String jahr = parameters.get("Jahr")[0];
		String stunden = parameters.get("Stunden")[0];
		String minuten = parameters.get("Minuten")[0];
		String plaetze = parameters.get("mfg_anbieten_plaetze")[0];

		if (Integer.parseInt(tag) < 10) {
			tag = "0" + tag;
		}

		if (Integer.parseInt(monat) < 10) {
			monat = "0" + monat;
		}

		String datum = tag + "." + monat + "." + jahr;

		GregorianCalendar aktuell = new GregorianCalendar();
		GregorianCalendar auswahl = new GregorianCalendar(
				Integer.parseInt(jahr), Integer.parseInt(monat) - 1,
				Integer.parseInt(tag));

		long difference = aktuell.getTimeInMillis() - auswahl.getTimeInMillis();

		Date d = new Date();
		DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
		String testdatum = df.format(d);
		String aktuelleStunde = new SimpleDateFormat("kk").format(d);
		String aktuelleMinute = new SimpleDateFormat("mm").format(d);

		boolean datumGueltig = false;
		boolean plaetzeGueltig = false;
		boolean streckeGueltig = false;

		// Auswahldatum mit aktuellem Datum vergleichen
		// Variable muss im negativen Zahlenbereich liegen, damit das Datum
		// gueltig ist
		if (difference >= 0) {
			// MFGs die am selben Tag stattfinden, müssen zusätzlich überprüft
			// werden
			if (testdatum.equals(datum)) {
				// Die Überprüfung erfolgt minutengenau
				if (Integer.parseInt(stunden) > Integer
						.parseInt(aktuelleStunde)) {
					datumGueltig = true;
				}

				if (Integer.parseInt(stunden) == Integer
						.parseInt(aktuelleStunde)) {
					if (Integer.parseInt(minuten) >= Integer
							.parseInt(aktuelleMinute)) {
						datumGueltig = true;
					}

				}

			}

		} else {
			// falls difference negativ ist, ist das Datum in der Zukunft das im
			// Formular ausgewählt wurde
			datumGueltig = true;
		}

		try {
			if (Integer.parseInt(plaetze) >= 1
					&& Integer.parseInt(plaetze) <= 10) {
				plaetzeGueltig = true;
			} else {
				return ok(views.html.anwendung.anwendung_mfg_anbieten.render(
						"ProTramp Mitfahrgelegenheit", nutzer,
						"Die Anzahl der Plätze muss zwischen 1"
								+ " und 10 liegen.", typ, zaehler));
			}
		} catch (NumberFormatException e) {
			return ok(views.html.anwendung.anwendung_mfg_anbieten
					.render("ProTramp Mitfahrgelegenheit",
							nutzer,
							"Ihre Eingaben waren nicht korrekt. Versuchen Sie bitte erneut.",
							typ, zaehler));
		}

		if (start.equals(ziel) || start.equals(strecke) || ziel.equals(strecke)) {
			return ok(views.html.anwendung.anwendung_mfg_anbieten
					.render("ProTramp Mitfahrgelegenheit",
							nutzer,
							"Start-Haltestelle und Ziel-Haltestelle bzw. Zwischenhaltestelle"
									+ "\n dürfen nicht gleich sein. Geben Sie korrekte Daten ein.",
							typ, zaehler));
		} else {
			streckeGueltig = true;
		}

		if (Integer.parseInt(stunden) < 10) {
			stunden = "0" + stunden;
		}

		if (Integer.parseInt(minuten) < 10) {
			minuten = "0" + minuten;
		}

		String uhrzeit = stunden + ":" + minuten;

		String email = "";
		List<DBObject> feld;

		List<Zaehler> zaehler2 = new ArrayList<>();
		if (datumGueltig) {

			// MFG in die Datenbank schreiben
			try {
				MongoClient mongoClient = new MongoClient("localhost", 27017);
				DB db = mongoClient.getDB("play_basics");

				// Die Collection des Nutzers finden
				DBCollection coll = db.getCollection("user");
				com.mongodb.DBCursor cursor = coll.find();
				BasicDBObject query = (BasicDBObject) new BasicDBObject(
						"username", nutzer);
				cursor = coll.find(query);

				String vorname = "";
				String name = "";

				// Den Vornamen und Namen des Nutzers herausfinden
				feld = coll.find(query).toArray();
				for (DBObject s : feld) {
					if (s.containsField("vorname")) {
						vorname = (String) s.get("vorname");
						name = (String) s.get("name");
						email = (String) s.get("email");

					}
				}

				coll = db.getCollection("mfg");

				BasicDBObject doc = new BasicDBObject("start", start)
						.append("ziel", ziel)
						.append("strecke", strecke)
						.append("datum", datum)
						.append("uhrzeit", uhrzeit)
						.append("fahrer",
								vorname + " " + name.substring(0, 1) + ".")
						.append("email", email).append("fahrzeug", "")
						.append("anzahl_plaetze", plaetze).append("preis", "")
						.append("entfernung", "");

				coll.insert(doc);

				mongoClient.close();

			} catch (UnknownHostException e) {
				e.printStackTrace();
			}

			// AnzeigeDetails List erforderlich

			try {
				MongoClient mongoClient = new MongoClient("localhost", 27017);
				DB db = mongoClient.getDB("play_basics");

				DBCollection coll = db.getCollection("user");
				com.mongodb.DBCursor cursor = coll.find();
				BasicDBObject query = (BasicDBObject) new BasicDBObject("username",
						nutzer);
				List<DBObject> feldEmail = coll.find(query).toArray();
				
				for(DBObject s : feldEmail) {
					email = (String) s.get("email");
				}
				
				
				coll = db.getCollection("mfg");
				cursor = coll.find();
				query = (BasicDBObject) new BasicDBObject("email",
						email);
				cursor = coll.find(query);

				// suchergebnisse = (int) cursor.count();

				// Aktuelles Datum in Format dd.MM.yyyy holen
				DateFormat date = new SimpleDateFormat("dd.MM.yyyy HH:mm");
				Date todayDate = Calendar.getInstance().getTime();
				String reportDate = date.format(todayDate);
				Date dateDBParsen = null;

				
				
				difference = 0;

				for (DBObject s : cursor) {
					String dateDB = (String) s.get("datum") + " "
							+ (String) s.get("uhrzeit");
					// String uhrzeitDB = (String) s.get("uhrzeit");

					try {

						dateDBParsen = date.parse(dateDB);
						difference = todayDate.getTime() - dateDBParsen.getTime()
								- 1800000;
						// return ok(""+difference);

					} catch (Exception e) {
						e.printStackTrace();
					}
					// solang zahl negativ, dann fahrt gültig
					if (difference <= 0) {

						//test += " "+difference;
						// return ok(""+difference);
						details.add(new AnzeigeDetails(s.get("_id").toString(),
								(String) s.get("start"), (String) s.get("ziel"),
								(String) s.get("strecke"), (String) s
										.get("uhrzeit"), (String) s.get("datum"),
								(String) s.get("fahrer"),
								Integer.parseInt((String) s.get("anzahl_plaetze")),
								(String) s.get("email")));
						//suchergebnisse++;

					}
					

				}

				mongoClient.close();

			} catch (Exception e) {

				e.printStackTrace();
				

			}

			// user_statistiken bearbeiten und lesen

			zaehler = new ArrayList<>();

			meine_mfgs = 0;
			//int anfragen;

			try {
				MongoClient mongoClient = new MongoClient("localhost", 27017);
				DB db = mongoClient.getDB("play_basics");

				DBCollection coll = db.getCollection("user_statistiken");
				com.mongodb.DBCursor cursor = coll.find();
				BasicDBObject query = (BasicDBObject) new BasicDBObject("username",
						nutzer);
				cursor = coll.find(query);

				BasicDBObject doc = new BasicDBObject();
				doc.put("meine_mfgs", details.size());

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
				//return ok("-> Überprüfung notwendig");
			} catch (Exception e) {
				e.printStackTrace();
				
			}

			return ok(views.html.anwendung.anwendung_mfg_anzeigen.render(
					"ProTramp Mitfahrgelegenheit", nutzer, "", typ, details,
					zaehler));
		} else {

			// user_statistike lesen

			try {
				MongoClient mongoClient = new MongoClient("localhost", 27017);
				DB db = mongoClient.getDB("play_basics");

				DBCollection coll = db.getCollection("user_statistiken");
				com.mongodb.DBCursor cursor = coll.find();
				BasicDBObject query = (BasicDBObject) new BasicDBObject(
						"username", nutzer);
				cursor = coll.find(query);

				if (cursor.count() != 0) {

					for (DBObject s : cursor) {
						zaehler2.add(new Zaehler((int) s.get("suchergebnisse"),
								(int) s.get("meine_mfgs"), (int) s
										.get("anfragen")));

					}

				}

				mongoClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return ok(views.html.anwendung.anwendung_mfg_anbieten
					.render("ProTramp Mitfahrgelegenheit",
							nutzer,
							"Das Datum muss in der Zukunft liegen. Versuchen Sie es bitte erneut.",
							typ, zaehler2));
		}

	}

	/*********************************/

	public static Result anzeigen() {
		String nutzer = session("connected");
		String typ = session("typ");
		// List <String> details = new ArrayList<>();
		List<AnzeigeDetails> details = new ArrayList<>();

		if (!typ.equals("Fahrer")) {
			typ = "";
		}

		// AnzeigeDetails List erforderlich
		String email = "";
		List<DBObject> feld;
		String testfeld = "";
		int suchergebnisse = 0;
		String test = "";

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = db.getCollection("user");
			com.mongodb.DBCursor cursor = coll.find();
			BasicDBObject query = (BasicDBObject) new BasicDBObject("username",
					nutzer);
			List<DBObject> feldEmail = coll.find(query).toArray();
			
			for(DBObject s : feldEmail) {
				email = (String) s.get("email");
			}
			
			
			coll = db.getCollection("mfg");
			cursor = coll.find();
			query = (BasicDBObject) new BasicDBObject("email",
					email);
			cursor = coll.find(query);

			// suchergebnisse = (int) cursor.count();

			// Aktuelles Datum in Format dd.MM.yyyy holen
			DateFormat date = new SimpleDateFormat("dd.MM.yyyy HH:mm");
			Date todayDate = Calendar.getInstance().getTime();
			String reportDate = date.format(todayDate);
			Date dateDBParsen = null;

			
			
			long difference = 0;

			for (DBObject s : cursor) {
				String dateDB = (String) s.get("datum") + " "
						+ (String) s.get("uhrzeit");
				// String uhrzeitDB = (String) s.get("uhrzeit");

				try {

					dateDBParsen = date.parse(dateDB);
					difference = todayDate.getTime() - dateDBParsen.getTime()
							- 1800000;
					// return ok(""+difference);

				} catch (Exception e) {
					e.printStackTrace();
				}
				// solang zahl negativ, dann fahrt gültig
				if (difference <= 0) {

					//test += " "+difference;
					// return ok(""+difference);
					details.add(new AnzeigeDetails(s.get("_id").toString(),
							(String) s.get("start"), (String) s.get("ziel"),
							(String) s.get("strecke"), (String) s
									.get("uhrzeit"), (String) s.get("datum"),
							(String) s.get("fahrer"),
							Integer.parseInt((String) s.get("anzahl_plaetze")),
							(String) s.get("email")));
					//suchergebnisse++;

				}
				

			}

			mongoClient.close();

		} catch (Exception e) {

			e.printStackTrace();
			

		}

		// user_statistiken bearbeiten und lesen

		List<Zaehler> zaehler = new ArrayList<>();

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

			BasicDBObject doc = new BasicDBObject();
			doc.put("meine_mfgs", details.size());

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
			//return ok("-> Überprüfung notwendig");
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
		//return ok(""+details.size());
		
		return ok(views.html.anwendung.anwendung_mfg_anzeigen.render(
				"ProTramp Mitfahrgelegenheit", nutzer, "", typ, details,
				zaehler));
	}

	/*********************************/

	@SuppressWarnings("deprecation")
	public static Result detailsAnzeigen(String id) {

		String nutzer = session("connected");
		String typ = session("typ");
		DBObject teste;
		List<AnzeigeDetails> details2 = new ArrayList<>();

		if (!typ.equals("Fahrer")) {
			typ = "";
		}

		List<DBObject> feld;

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = db.getCollection("mfg");
			com.mongodb.DBCursor cursor = coll.find();

			DBObject query;
			query = (BasicDBObject) new BasicDBObject("_id", new ObjectId(id));

			cursor = coll.find(query);
			feld = coll.find(query).toArray();

			for (DBObject s : feld) {
				details2.add(new AnzeigeDetails(s.get("_id").toString(),
						(String) s.get("start"), (String) s.get("ziel"),
						(String) s.get("strecke"), (String) s.get("uhrzeit"),
						(String) s.get("datum"), s.get("fahrer").toString(),
						Integer.parseInt((String) s.get("anzahl_plaetze")),
						(String) s.get("email")));
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

		return ok(views.html.anwendung.mfg_details.render(
				"ProTramp Mitfahrgelegenheit", nutzer, "", typ, details2,
				zaehler));
	}

	public static Result mfg_suchen() {

		String nutzer = session("connected");
		String typ = session("typ");

		try {
			if (typ == null) {

			} else {
				if (!typ.equals("Fahrer")) {
					typ = "";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * GregorianCalendar datumDB = new
		 * GregorianCalendar(Integer.parseInt(jahr), Integer.parseInt(monat) -
		 * 1, Integer.parseInt(tag));
		 */

		Map<String, String[]> parameters = request().body().asFormUrlEncoded();
		List<AnzeigeDetails> details = new ArrayList<>();
		// Parameteruebergaben werden ueberprueft
		if (!(parameters != null && parameters.containsKey("exampleVon")
				&& parameters.containsKey("exampleNach")
				&& parameters.containsKey("Tag")
				&& parameters.containsKey("Monat") && parameters
					.containsKey("Jahr"))) {

			Logger.warn("bad login request");
			return redirect("/");
		}

		// Parameterwerte werden ausgelesen
		String start = parameters.get("exampleVon")[0];
		String ziel = parameters.get("exampleNach")[0];
		String tag = parameters.get("Tag")[0];
		String monat = parameters.get("Monat")[0];
		String jahr = parameters.get("Jahr")[0];

		int suchergebnisse = 0;

		String datum = tag + "." + monat + "." + jahr;

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = db.getCollection("mfg");
			com.mongodb.DBCursor cursor = coll.find();
			BasicDBObject query = (BasicDBObject) new BasicDBObject("start",
					start).append("ziel", ziel);
			cursor = coll.find(query);

			// suchergebnisse = (int) cursor.count();

			// Aktuelles Datum in Format dd.MM.yyyy holen
			DateFormat date = new SimpleDateFormat("dd.MM.yyyy HH:mm");
			Date todayDate = Calendar.getInstance().getTime();
			String reportDate = date.format(todayDate);
			Date dateDBParsen = null;

			long difference = 0;

			for (DBObject s : cursor) {
				String dateDB = (String) s.get("datum") + " "
						+ (String) s.get("uhrzeit");
				// String uhrzeitDB = (String) s.get("uhrzeit");

				try {

					dateDBParsen = date.parse(dateDB);
					difference = todayDate.getTime() - dateDBParsen.getTime()
							- 1800000;
					// return ok(""+difference);

				} catch (Exception e) {
					e.printStackTrace();
				}
				// solang zahl negativ, dann fahrt gültig
				if (difference <= 0) {

					suchergebnisse++;
					// return ok(""+difference);
					details.add(new AnzeigeDetails(s.get("_id").toString(),
							(String) s.get("start"), (String) s.get("ziel"),
							(String) s.get("strecke"), (String) s
									.get("uhrzeit"), (String) s.get("datum"),
							(String) s.get("fahrer"),
							Integer.parseInt((String) s.get("anzahl_plaetze")),
							(String) s.get("email")));

				}

			}

			mongoClient.close();

		} catch (Exception e) {

			e.printStackTrace();

		}

		// user_statistiken bearbeiten und lesen

		List<Zaehler> zaehler = new ArrayList<>();

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

			BasicDBObject doc = new BasicDBObject();
			doc.put("suchergebnisse", suchergebnisse);

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

		return ok(views.html.anwendung.anwendung_mfg_suchen.render(
				"ProTramp Mitfahrgelegenheit", nutzer, "", typ, details,
				zaehler));
	}

	public static Result suchDetailsAnzeigen(String id) {
		String nutzer = session("connected");
		String typ = session("typ");

		List<AnzeigeDetails> details2 = new ArrayList<>();
		if (typ == null) {

		} else {
			if (!typ.equals("Fahrer")) {
				typ = "";
			}
		}

		// if (!typ.equals("Fahrer")) {
		// typ = "";
		// }

		List<DBObject> feld;

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = db.getCollection("mfg");
			com.mongodb.DBCursor cursor = coll.find();

			DBObject query;
			query = (BasicDBObject) new BasicDBObject("_id", new ObjectId(id));

			cursor = coll.find(query);
			feld = coll.find(query).toArray();

			for (DBObject s : feld) {
				details2.add(new AnzeigeDetails(s.get("_id").toString(),
						(String) s.get("start"), (String) s.get("ziel"),
						(String) s.get("strecke"), (String) s.get("uhrzeit"),
						(String) s.get("datum"), s.get("fahrer").toString(),
						Integer.parseInt((String) s.get("anzahl_plaetze")),
						(String) s.get("email")));
			}

			mongoClient.close();

		} catch (Exception e) {
			// TODO: handle exception
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

		return ok(views.html.anwendung.mfg_details_suche.render(
				"ProTramp Mitfahrgelegenheit", nutzer, "", typ, details2,
				zaehler));
	}

}
