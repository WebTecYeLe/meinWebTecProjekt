package controllers;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.AnzeigeDetails;
import models.Orte;
import models.Person;
import models.Zaehler;

import org.bson.types.ObjectId;

import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
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

//Das ist die Hauptklasse der MFG
//Sie kümmert sich um Funktionen wie MFG Anbieten, MFG anzeigen, MFG Suchen, MFG Details anzeigen, MFG Anfrage  starten, MFG Nachricht lesen, 
//MFG Fahrtfunktionen wie zustimmen oder ablehnen für Fahrer ermöglichen

public class Anwendung extends Controller {

	//

	public static Result info() {

		String nutzer = session("connected");
		String typ = session("typ");

		List<Orte> ortsdetails = new ArrayList<>();

		List<DBObject> feld;

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

		String facebook = "";

		if (session("facebook_logged") != null) {
			facebook = "Ja";
		}

		return ok(views.html.anwendung.info.render(
				"ProTramp Mitfahrgelegenheit", nutzer, "", typ, ortsdetails,
				facebook));
	}

	public static Result mfg_anbieten_index() {

		String nutzer = session("connected");
		String typ = session("typ");

		// user_statistike lesen

		List<Zaehler> zaehler = new ArrayList<>();

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

		String facebook = "";

		if (session("facebook_logged") != null) {
			facebook = "Ja";
		}

		return ok(views.html.anwendung.anwendung_mfg_anbieten.render(
				"ProTramp Mitfahrgelegenheit", nutzer, "", typ, zaehler,
				facebook));

	}

	// Funktion ermöglicht es, dass Fahrer eine MFG anbieten dürfen
	// Nur Fahrer können dies, nicht Mitfahrer

	@SuppressWarnings("rawtypes")
	public static Result anbieten() {

		String nutzer = session("connected");
		String typ = session("typ");

		String facebook = "";

		if (session("facebook_logged") != null) {
			facebook = "Ja";
		}

		List<String> nachrichtenfeld = new ArrayList<>();

		List<Zaehler> zaehler = new ArrayList<>();

		// In dieser Liste werden später die Personen erfasst die angefragt
		// haben.
		List<String> gestarteteAnfragen = new ArrayList<>();

		// Personen die bei der Anfrage angenommen wurden
		List<String> erfolgreicheAnfragen = new ArrayList<>();

		List<String> abgelehnteAnfragen = new ArrayList<>();

		// user_statistiken lesen

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
				&& parameters.containsKey("Minuten")
				&& parameters.containsKey("mfg_anbieten_plaetze")
				&& parameters.containsKey("latstart")
				&& parameters.containsKey("lngstart")
				&& parameters.containsKey("latziel")
				&& parameters.containsKey("lngziel")
				&& parameters.containsKey("latstrecke") && parameters
					.containsKey("lngstrecke")

		)) {

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

		// Es soll interaktiv eine Map mit Route erstellt werden. Dafür werden
		// Koordinaten benötigt

		double latstart = Double.parseDouble(parameters.get("latstart")[0]);
		double lngstart = Double.parseDouble(parameters.get("lngstart")[0]);
		double latziel = Double.parseDouble(parameters.get("latziel")[0]);
		double lngziel = Double.parseDouble(parameters.get("lngziel")[0]);
		double latstrecke = Double.parseDouble(parameters.get("latstrecke")[0]);
		double lngstrecke = Double.parseDouble(parameters.get("lngstrecke")[0]);

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

			} else {
				return ok(views.html.anwendung.anwendung_mfg_anbieten.render(
						"ProTramp Mitfahrgelegenheit", nutzer,
						"Die Anzahl der Plätze muss zwischen 1"
								+ " und 10 liegen.", typ, zaehler, facebook));
			}
		} catch (NumberFormatException e) {
			return ok(views.html.anwendung.anwendung_mfg_anbieten
					.render("ProTramp Mitfahrgelegenheit",
							nutzer,
							"Ihre Eingaben waren nicht korrekt. Versuchen Sie bitte erneut.",
							typ, zaehler, facebook));
		}

		// Hier wird überprüft dass der Fahrer nicht absichtlich Start und Ziel
		// bzw. Streckenort gleich benannt

		if (start.equals(ziel) || start.equals(strecke) || ziel.equals(strecke)) {
			return ok(views.html.anwendung.anwendung_mfg_anbieten
					.render("ProTramp Mitfahrgelegenheit",
							nutzer,
							"Start-Haltestelle und Ziel-Haltestelle bzw. Zwischenhaltestelle"
									+ "\n dürfen nicht gleich sein. Geben Sie korrekte Daten ein.",
							typ, zaehler, facebook));
		} else {

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

				BasicDBObject query = (BasicDBObject) new BasicDBObject(
						"username", nutzer);

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

				List<Double> startkoordinaten = new ArrayList<>();
				List<Double> zielkoordinaten = new ArrayList<>();
				List<Double> streckenkoordinaten = new ArrayList<>();

				startkoordinaten.add(latstart);
				startkoordinaten.add(lngstart);
				zielkoordinaten.add(latziel);
				zielkoordinaten.add(lngziel);
				streckenkoordinaten.add(latstrecke);
				streckenkoordinaten.add(lngstrecke);

				coll = db.getCollection("mfg");

				// Daten werden in die Datenbank geschrieben
				// Dadurch können Mitfahrer MFGs in der Datenbank suchen

				BasicDBObject doc = new BasicDBObject("start", start)
						.append("ziel", ziel)
						.append("strecke", strecke)
						.append("datum", datum)
						.append("uhrzeit", uhrzeit)
						.append("fahrer",
								vorname + " " + name.substring(0, 1) + ".")
						.append("email", email).append("fahrzeug", "")
						.append("anzahl_plaetze", plaetze).append("preis", "")
						.append("entfernung", "")
						.append("gestarteteAnfragen", gestarteteAnfragen)
						.append("erfolgreicheAnfragen", erfolgreicheAnfragen)
						.append("abgelehnteAnfragen", abgelehnteAnfragen)
						.append("nachrichten", nachrichtenfeld);

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
				BasicDBObject query = (BasicDBObject) new BasicDBObject(
						"username", nutzer);
				List<DBObject> feldEmail = coll.find(query).toArray();

				for (DBObject s : feldEmail) {
					email = (String) s.get("email");
				}

				coll = db.getCollection("mfg");
				cursor = coll.find();
				query = (BasicDBObject) new BasicDBObject("email", email);
				cursor = coll.find(query);

				// suchergebnisse = (int) cursor.count();

				// Aktuelles Datum in Format dd.MM.yyyy holen
				DateFormat date = new SimpleDateFormat("dd.MM.yyyy HH:mm");
				Date todayDate = Calendar.getInstance().getTime();

				Date dateDBParsen = null;

				difference = 0;

				for (DBObject s : cursor) {
					String dateDB = (String) s.get("datum") + " "
							+ (String) s.get("uhrzeit");
					// String uhrzeitDB = (String) s.get("uhrzeit");

					try {

						dateDBParsen = date.parse(dateDB);
						difference = todayDate.getTime()
								- dateDBParsen.getTime() - 1800000;
						// return ok(""+difference);

					} catch (Exception e) {
						e.printStackTrace();
					}
					// solang zahl negativ, dann fahrt gültig
					if (difference <= 0) {

						// test += " "+difference;
						// return ok(""+difference);
						details.add(new AnzeigeDetails(s.get("_id").toString(),
								(String) s.get("start"),
								(String) s.get("ziel"), (String) s
										.get("strecke"), (String) s
										.get("uhrzeit"), (String) s
										.get("datum"),
								(String) s.get("fahrer"), Integer
										.parseInt((String) s
												.get("anzahl_plaetze")),
								(String) s.get("email"), "", (String) s
										.get("status"), ""));
						// suchergebnisse++;

					}

				}

				mongoClient.close();

			} catch (Exception e) {

				e.printStackTrace();

			}

			// user_statistiken bearbeiten und lesen

			zaehler = new ArrayList<>();

			try {
				MongoClient mongoClient = new MongoClient("localhost", 27017);
				DB db = mongoClient.getDB("play_basics");

				DBCollection coll = db.getCollection("user_statistiken");
				com.mongodb.DBCursor cursor = coll.find();
				BasicDBObject query = (BasicDBObject) new BasicDBObject(
						"username", nutzer);
				cursor = coll.find(query);

				BasicDBObject doc = new BasicDBObject();
				doc.put("meine_mfgs", details.size());

				BasicDBObject account = new BasicDBObject();
				account.put("$set", doc);

				coll.update(query, account);

				if (cursor.count() != 0) {

					for (DBObject s : cursor) {
						zaehler.add(new Zaehler((int) s.get("suchergebnisse"),
								(int) s.get("meine_mfgs"), (int) s
										.get("anfragen")));

					}

				}

				mongoClient.close();
				// return ok("-> Überprüfung notwendig");
			} catch (Exception e) {
				e.printStackTrace();

			}

			return ok(views.html.anwendung.anwendung_mfg_anzeigen.render(
					"ProTramp Mitfahrgelegenheit", nutzer, "", typ, details,
					zaehler, facebook));
		} else {

			// user_statistiken lesen

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
							typ, zaehler2, facebook));
		}

	}

	// MFGs die gesucht wurden sollen anhand eine Liste dargestellt werden

	public static Result anzeigen() {
		String nutzer = session("connected");
		String typ = session("typ");
		// List <String> details = new ArrayList<>();
		List<AnzeigeDetails> details = new ArrayList<>();
		List<Zaehler> zaehler = new ArrayList<>();

		if (!typ.equals("Fahrer")) {
			typ = "";
		}

		if (typ.equals("Fahrer")) {
			// AnzeigeDetails List erforderlich
			String email = "";

			try {
				MongoClient mongoClient = new MongoClient("localhost", 27017);
				DB db = mongoClient.getDB("play_basics");

				DBCollection coll = db.getCollection("user");
				com.mongodb.DBCursor cursor = coll.find();
				BasicDBObject query = (BasicDBObject) new BasicDBObject(
						"username", nutzer);
				List<DBObject> feldEmail = coll.find(query).toArray();

				for (DBObject s : feldEmail) {
					email = (String) s.get("email");
				}

				coll = db.getCollection("mfg");
				cursor = coll.find();
				query = (BasicDBObject) new BasicDBObject("email", email);
				cursor = coll.find(query);

				// suchergebnisse = (int) cursor.count();

				// Aktuelles Datum in Format dd.MM.yyyy holen
				DateFormat date = new SimpleDateFormat("dd.MM.yyyy HH:mm");
				Date todayDate = Calendar.getInstance().getTime();

				Date dateDBParsen = null;

				long difference = 0;

				for (DBObject s : cursor) {
					String dateDB = (String) s.get("datum") + " "
							+ (String) s.get("uhrzeit");

					try {

						dateDBParsen = date.parse(dateDB);
						difference = todayDate.getTime()
								- dateDBParsen.getTime() - 1800000;

					} catch (Exception e) {
						e.printStackTrace();
					}

					// solang zahl negativ, dann fahrt gültig
					// -> nur MFGs die maximal 30min in der Vergangenheit bzw.
					// in der Zukunft liegen werden aufgelistet
					if (difference <= 0) {

						details.add(new AnzeigeDetails(s.get("_id").toString(),
								(String) s.get("start"),
								(String) s.get("ziel"), (String) s
										.get("strecke"), (String) s
										.get("uhrzeit"), (String) s
										.get("datum"),
								(String) s.get("fahrer"), Integer
										.parseInt((String) s
												.get("anzahl_plaetze")),
								(String) s.get("email"), "", (String) s
										.get("status"), ""));

					}

				}

				mongoClient.close();

			} catch (Exception e) {

				e.printStackTrace();

			}

			// user_statistiken bearbeiten und lesen

			try {
				MongoClient mongoClient = new MongoClient("localhost", 27017);
				DB db = mongoClient.getDB("play_basics");

				DBCollection coll = db.getCollection("user_statistiken");
				com.mongodb.DBCursor cursor = coll.find();
				BasicDBObject query = (BasicDBObject) new BasicDBObject(
						"username", nutzer);
				cursor = coll.find(query);

				BasicDBObject doc = new BasicDBObject();
				doc.put("meine_mfgs", details.size());

				BasicDBObject account = new BasicDBObject();
				account.put("$set", doc);

				coll.update(query, account);

				if (cursor.count() != 0) {

					for (DBObject s : cursor) {
						zaehler.add(new Zaehler((int) s.get("suchergebnisse"),
								(int) s.get("meine_mfgs"), (int) s
										.get("anfragen")));

					}

				}

				mongoClient.close();

			} catch (Exception e) {
				e.printStackTrace();

			}
			// ///////////////////////////Mitfahrer
		} else {

			// AnzeigeDetails List erforderlich

			int anfragen = 0;

			try {
				MongoClient mongoClient = new MongoClient("localhost", 27017);
				DB db = mongoClient.getDB("play_basics");

				DBCollection coll = db.getCollection("mfg");
				com.mongodb.DBCursor cursor = coll.find();
				BasicDBObject query = (BasicDBObject) new BasicDBObject();
				cursor = coll.find(query);

				// Aktuelles Datum in Format dd.MM.yyyy holen
				DateFormat date = new SimpleDateFormat("dd.MM.yyyy HH:mm");
				Date todayDate = Calendar.getInstance().getTime();

				Date dateDBParsen = null;

				long difference = 0;

				for (DBObject s : cursor) {
					String dateDB = (String) s.get("datum") + " "
							+ (String) s.get("uhrzeit");

					try {

						dateDBParsen = date.parse(dateDB);
						difference = todayDate.getTime()
								- dateDBParsen.getTime() - 1800000;

					} catch (Exception e) {
						e.printStackTrace();
					}
					// solang zahl negativ, dann fahrt gültig
					if (difference <= 0) {
						if (s.get("erfolgreicheAnfragen").toString()
								.contains(nutzer)) {

							details.add(new AnzeigeDetails(s.get("_id")
									.toString(), (String) s.get("start"),
									(String) s.get("ziel"), (String) s
											.get("strecke"), (String) s
											.get("uhrzeit"), (String) s
											.get("datum"), (String) s
											.get("fahrer"), Integer
											.parseInt((String) s
													.get("anzahl_plaetze")),
									(String) s.get("email"), "", (String) s
											.get("status"), ""));

						}

						if (s.get("gestarteteAnfragen").toString()
								.contains(nutzer)) {
							anfragen++;

						}
					}

				}

				mongoClient.close();

			} catch (Exception e) {

				e.printStackTrace();

			}

			// user_statistiken bearbeiten und lesen

			try {
				MongoClient mongoClient = new MongoClient("localhost", 27017);
				DB db = mongoClient.getDB("play_basics");

				DBCollection coll = db.getCollection("user_statistiken");
				com.mongodb.DBCursor cursor = coll.find();
				BasicDBObject query = (BasicDBObject) new BasicDBObject(
						"username", nutzer);
				cursor = coll.find(query);

				BasicDBObject doc = new BasicDBObject();
				doc.put("meine_mfgs", details.size());
				doc.put("anfragen", anfragen);

				BasicDBObject account = new BasicDBObject();
				account.put("$set", doc);

				coll.update(query, account);

				if (cursor.count() != 0) {

					for (DBObject s : cursor) {
						zaehler.add(new Zaehler((int) s.get("suchergebnisse"),
								(int) s.get("meine_mfgs"), (int) s
										.get("anfragen")));

					}

				}

				mongoClient.close();

			} catch (Exception e) {
				e.printStackTrace();

			}

		}

		String facebook = "";

		if (session("facebook_logged") != null) {
			facebook = "Ja";
		}

		return ok(views.html.anwendung.anwendung_mfg_anzeigen.render(
				"ProTramp Mitfahrgelegenheit", nutzer, "", typ, details,
				zaehler, facebook));
	}

	// MFGs werden mit der Funktion detailsAnzeigen(String id) detailliert
	// aufgelistet

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Result detailsAnzeigen(String id) {

		String nutzer = session("connected");
		String typ = session("typ");

		String[] splitResult;
		String usr = "";
		String msg = "";

		List<AnzeigeDetails> details2 = new ArrayList<>();

		List<String> nachrichtenfeld = new ArrayList<>();
		String nachricht = "";

		if (!typ.equals("Fahrer")) {
			typ = "";
		}

		List<DBObject> feld;

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = db.getCollection("mfg");

			DBObject query;

			// Da die _id in der Datenbank immer eindeutig ist, gibt es hier
			// immer genau einen Documenten aus der Collection der Datenbank
			query = (BasicDBObject) new BasicDBObject("_id", new ObjectId(id));

			feld = coll.find(query).toArray();

			for (DBObject s : feld) {

				nachrichtenfeld = (List<String>) s.get("nachrichten");

				for (int i = 0; i < nachrichtenfeld.size(); i++) {

					if (nachrichtenfeld.toString().contains(nutzer)) {
						nachricht = nachrichtenfeld.get(i);
						splitResult = nachricht.split(" _ ");
						usr = splitResult[0];
						msg = splitResult[1];

					}
					
					

				}

				details2.add(new AnzeigeDetails(s.get("_id").toString(),
						(String) s.get("start"), (String) s.get("ziel"),
						(String) s.get("strecke"), (String) s.get("uhrzeit"),
						(String) s.get("datum"), s.get("fahrer").toString(),
						Integer.parseInt((String) s.get("anzahl_plaetze")),
						(String) s.get("email"), "", "", msg));
			}

			mongoClient.close();
		} catch (Exception e) {
			e.printStackTrace();

		}

		
		
		
		// user_statistiken lesen

		List<Zaehler> zaehler = new ArrayList<>();

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

		String facebook = "";

		if (session("facebook_logged") != null) {
			facebook = "Ja";
		}

		return ok(views.html.anwendung.mfg_details.render(
				"ProTramp Mitfahrgelegenheit", nutzer, "", typ, details2,
				zaehler, facebook));
	}

	// Die Funktion soll MFGs suchen die den Eingabekriterien der Suchfelder
	// Start und Ziel entsprechen
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

		int suchergebnisse = 0;

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = db.getCollection("mfg");
			com.mongodb.DBCursor cursor = coll.find();

			// Durch start und ziel wird die Anzahl der Documente aus der mfg
			// Collection auf ein bestimmtes Maß reduziert
			BasicDBObject query = (BasicDBObject) new BasicDBObject("start",
					start).append("ziel", ziel);
			cursor = coll.find(query);

			// Aktuelles Datum in Format dd.MM.yyyy holen
			DateFormat date = new SimpleDateFormat("dd.MM.yyyy HH:mm");
			Date todayDate = Calendar.getInstance().getTime();

			Date dateDBParsen = null;

			long difference = 0;

			for (DBObject s : cursor) {
				String dateDB = (String) s.get("datum") + " "
						+ (String) s.get("uhrzeit");

				try {

					dateDBParsen = date.parse(dateDB);
					difference = todayDate.getTime() - dateDBParsen.getTime()
							- 1800000;

				} catch (Exception e) {
					e.printStackTrace();
				}
				// solang zahl negativ, dann fahrt gültig
				// und solange noch Plötze vorhanden sind
				// -> nur MFGs die 30min in der Vergangenheit bzw. in der
				// Zukunft liegen werden berücksichtigt
				if (difference <= 0
						&& Integer.parseInt(s.get("anzahl_plaetze").toString()) >= 1) {

					suchergebnisse++;

					details.add(new AnzeigeDetails(s.get("_id").toString(),
							(String) s.get("start"), (String) s.get("ziel"),
							(String) s.get("strecke"), (String) s
									.get("uhrzeit"), (String) s.get("datum"),
							(String) s.get("fahrer"),
							Integer.parseInt((String) s.get("anzahl_plaetze")),
							(String) s.get("email"), "", (String) s
									.get("status"), ""));

				}

			}

			mongoClient.close();

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

		String facebook = "";

		if (session("facebook_logged") != null) {
			facebook = "Ja";
		}

		return ok(views.html.anwendung.anwendung_mfg_suchen.render(
				"ProTramp Mitfahrgelegenheit", nutzer, "", typ, details,
				zaehler, facebook));
	}

	// MFGs Details werden angezeigt, die zuvor über das Suchformular gesucht
	// wurden

	@SuppressWarnings("unchecked")
	public static Result suchDetailsAnzeigen(String id) {
		String nutzer = session("connected");
		String typ = session("typ");

		List<AnzeigeDetails> details2 = new ArrayList<>();

		String info = "";
		String nutzerCheck = "";

		String check = null;

		if (typ == null) {

		} else {
			if (!typ.equals("Fahrer")) {
				typ = "";
			}
		}

		List<DBObject> feld;

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = db.getCollection("mfg");

			DBObject query;
			query = (BasicDBObject) new BasicDBObject("_id", new ObjectId(id));

			feld = coll.find(query).toArray();

			for (DBObject s : feld) {
				details2.add(new AnzeigeDetails(s.get("_id").toString(),
						(String) s.get("start"), (String) s.get("ziel"),
						(String) s.get("strecke"), (String) s.get("uhrzeit"),
						(String) s.get("datum"), s.get("fahrer").toString(),
						Integer.parseInt((String) s.get("anzahl_plaetze")),
						(String) s.get("email"), "", (String) s.get("status"),
						""));

				nutzerCheck = s.get("email").toString();

				List<String> anfrageListe = new ArrayList<>();
				List<String> erfolgreicheListe = new ArrayList<>();
				List<String> abgelehnteListe = new ArrayList<>();

				anfrageListe = (List<String>) s.get("gestarteteAnfragen");
				erfolgreicheListe = (List<String>) s
						.get("erfolgreicheAnfragen");
				abgelehnteListe = (List<String>) s.get("abgelehnteAnfragen");

				if (anfrageListe.contains(nutzer)
						|| erfolgreicheListe.contains(nutzer)
						|| abgelehnteListe.contains(nutzer)) {
					check = "Ok";

				}

			}

			coll = db.getCollection("user");
			query = new BasicDBObject("email", nutzerCheck);

			feld = coll.find(query).toArray();

			for (DBObject x : feld) {
				nutzerCheck = x.get("username").toString();

			}

			if (nutzer.equals(nutzerCheck)) {
				info = "Ok";
			}

			mongoClient.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		// user_statistiken lesen

		List<Zaehler> zaehler = new ArrayList<>();

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

		String facebook = "";

		if (session("facebook_logged") != null) {
			facebook = "Ja";
		}

		return ok(views.html.anwendung.mfg_details_suche.render(
				"ProTramp Mitfahrgelegenheit", nutzer, info, typ, details2,
				zaehler, check, facebook));
	}

	// Mit dieser Funktion haben die Mitfahrer die Möglichkeit eine Anfrage zu
	// einer MFG zu starten

	@SuppressWarnings("unchecked")
	public static Result anfrageStarten(String id) {

		String nutzer = session("connected");
		String typ = session("typ");

		List<String> anfrageListe = new ArrayList<>();
		List<String> gesamtAnfrageListe = new ArrayList<>();

		List<AnzeigeDetails> details2 = new ArrayList<>();
		List<Zaehler> zaehler = new ArrayList<>();

		int anfrageMenge = 0;

		// Benutzerüberprüfung ob Fahrer, Mitfahrer oder nicht angemeldeter User
		if (typ == null) {

		} else {
			if (!typ.equals("Fahrer")) {
				typ = "";
			}
		}

		List<DBObject> feld;

		// Die eindeutige MFG aus der Datenbank holen
		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = db.getCollection("mfg");
			com.mongodb.DBCursor cursor = coll.find();

			DBObject query;
			query = (BasicDBObject) new BasicDBObject("_id", new ObjectId(id));

			cursor = coll.find(query);
			feld = coll.find(query).toArray();

			// Jede MFG ist eindeutig. Deshalb gibt es hier immer nur einen
			// Dokumenten aus der Collection
			for (DBObject s : feld) {
				details2.add(new AnzeigeDetails(s.get("_id").toString(),
						(String) s.get("start"), (String) s.get("ziel"),
						(String) s.get("strecke"), (String) s.get("uhrzeit"),
						(String) s.get("datum"), s.get("fahrer").toString(),
						Integer.parseInt((String) s.get("anzahl_plaetze")),
						(String) s.get("email"), "", (String) "offen", ""));
			}

			// Der Anfrageprozess
			for (DBObject s : feld) {

				// Überprüfung ob noch Plätze verfügbar sind
				if (Integer.parseInt(s.get("anzahl_plaetze").toString()) >= 1) {
					anfrageListe = (List<String>) s.get("gestarteteAnfragen");

					// Die Collection mfg in der Datenbank muss aktualisiert
					// werden, damit andere Benutzer stets
					// die aktuellen Informationen sehen können. Dadurch wird
					// einen sicheren Anfrageprozess
					// gewährleistet

					if (!anfrageListe.contains(nutzer)) {
						anfrageListe.add(nutzer);

					}

					// Userstatistiken bearbeiten und lesen

					query = new BasicDBObject("_id", new ObjectId(id));
					BasicDBObject docMFG = new BasicDBObject();
					docMFG.put("gestarteteAnfragen", anfrageListe);
					BasicDBObject account = new BasicDBObject();
					account.put("$set", docMFG);
					coll.update(query, account);

					coll = db.getCollection("mfg");
					query = new BasicDBObject();

					feld = coll.find(query).toArray();

					for (DBObject i : feld) {

						gesamtAnfrageListe = (List<String>) i
								.get("gestarteteAnfragen");

						if (gesamtAnfrageListe.contains(nutzer)) {
							anfrageMenge++;

						}

					}

					coll = db.getCollection("user_statistiken");
					query = new BasicDBObject("username", nutzer);
					BasicDBObject doc = new BasicDBObject();
					doc.put("anfragen", anfrageMenge);
					account = new BasicDBObject();
					account.put("$set", doc);
					coll.update(query, account);

					cursor = coll.find(query);

					if (cursor.count() != 0) {

						for (DBObject x : cursor) {
							zaehler.add(new Zaehler((int) x
									.get("suchergebnisse"), (int) x
									.get("meine_mfgs"), (int) x.get("anfragen")));

						}

					}

				} else {

				}

			}

			mongoClient.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Die Seite der Anfragen aufrufen

		return redirect("/anwendung/anfragen_index");

	}

	// Diese Funktion hilft den Benutzern eine grobe Übersicht über ihre
	// Anfragen zu gewinnen

	@SuppressWarnings("unchecked")
	public static Result anfragen_index() {

		String test = "";
		
		String nutzer = session("connected");
		String typ = session("typ");

		

		List<String> nachrichtenfeld = new ArrayList<>();

		// Aktuelles Datum in Format dd.MM.yyyy holen
		long difference = 0;
		DateFormat date = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		Date todayDate = Calendar.getInstance().getTime();

		Date dateDBParsen = null;

		// If Entscheidung für den Fahrer. Fahrer hat eine andere Sicht als den
		// Mitfahrer
		if (typ.equals("Fahrer")) {
			List<DBObject> feld = new ArrayList<>();

			List<AnzeigeDetails> details = new ArrayList<>();

			List<String> anfrageListe = new ArrayList<>();

			int anfragen = 0;
			String email = "";

			try {
				MongoClient mongoClient = new MongoClient("localhost", 27017);
				DB db = mongoClient.getDB("play_basics");

				DBCollection coll = db.getCollection("user");

				BasicDBObject query = (BasicDBObject) new BasicDBObject(
						"username", nutzer);
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

					try {

						dateDBParsen = date.parse(dateDB);
						difference = todayDate.getTime()
								- dateDBParsen.getTime() - 1800000;

					} catch (Exception e) {
						e.printStackTrace();
					}

					if (difference <= 0) {
						anfrageListe = (List<String>) s
								.get("gestarteteAnfragen");

						if (anfrageListe.size() >= 1) {

							StringBuilder sb = new StringBuilder();
							sb.append("");
							sb.append(anfrageListe.size());

							anfragen += anfrageListe.size();

							details.add(new AnzeigeDetails(s.get("_id")
									.toString(), (String) s.get("start"),
									(String) s.get("ziel"), (String) s
											.get("strecke"), (String) s
											.get("uhrzeit"), (String) s
											.get("datum"), (String) s
											.get("fahrer"), Integer
											.parseInt((String) s
													.get("anzahl_plaetze")),
									(String) s.get("email"), sb.toString(), "",
									""));

						}

					}

				}

				mongoClient.close();

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
				BasicDBObject query = (BasicDBObject) new BasicDBObject(
						"username", nutzer);
				cursor = coll.find(query);

				BasicDBObject doc = new BasicDBObject();
				doc.put("anfragen", anfragen);

				BasicDBObject account = new BasicDBObject();
				account.put("$set", doc);

				coll.update(query, account);

				if (cursor.count() != 0) {

					for (DBObject s : cursor) {
						zaehler.add(new Zaehler((int) s.get("suchergebnisse"),
								(int) s.get("meine_mfgs"), (int) s
										.get("anfragen")));

					}

				}

				mongoClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			String facebook = "";

			if (session("facebook_logged") != null) {
				facebook = "Ja";
			}

			return ok(views.html.anwendung.anwendung_anfragen.render(
					"ProTramp Mitfahrgelegenheit", nutzer, "", typ, details,
					zaehler, facebook));

		} else {
			List<DBObject> feld = new ArrayList<>();
			List<AnzeigeDetails> details = new ArrayList<>();

			if (!typ.equals("Fahrer")) {
				typ = "";
			}

			int anfragen = 0;

			try {
				MongoClient mongoClient = new MongoClient("localhost", 27017);
				DB db = mongoClient.getDB("play_basics");

				DBCollection coll = db.getCollection("mfg");

				BasicDBObject query = (BasicDBObject) new BasicDBObject();

				feld = coll.find(query).toArray();

				String nachricht = "";

				
				
				for (DBObject s : feld) {

					String dateDB = (String) s.get("datum") + " "
							+ (String) s.get("uhrzeit");

					try {

						dateDBParsen = date.parse(dateDB);
						difference = todayDate.getTime()
								- dateDBParsen.getTime() - 1800000;

					} catch (Exception e) {
						e.printStackTrace();
					}

					
					
					
					if (difference <= 0) {
						String[] splitResult;
						String usr = "";
						String msg = "";

						nachrichtenfeld = (List<String>) s.get("nachrichten");
						
						for (int i = 0; i < nachrichtenfeld.size(); i++) {
							nachricht = nachrichtenfeld.get(i).toString();
							
							if (nachricht.contains(nutzer)) {								
								
								splitResult = nachricht.split(" _ ");
								usr = splitResult[0];
								msg = splitResult[1];
								test += msg;
							}

						}

						if(msg.isEmpty()) {
							msg = "";
						}

						if (s.get("gestarteteAnfragen").toString()
								.contains(nutzer)) {

							anfragen++;

							details.add(new AnzeigeDetails(s.get("_id")
									.toString(), (String) s.get("start"),
									(String) s.get("ziel"), (String) s
											.get("strecke"), (String) s
											.get("uhrzeit"), (String) s
											.get("datum"), (String) s
											.get("fahrer"), Integer
											.parseInt((String) s
													.get("anzahl_plaetze")),
									(String) s.get("email"), "", "offen", ""));

						}

						if (s.get("erfolgreicheAnfragen").toString()
								.contains(nutzer)) {
							
//							for (int i = 0; i < nachrichtenfeld.size(); i++) {
//
//
//								nachricht = nachrichtenfeld.get(i).toString();
//								
//								
//								if (nachricht.contains(nutzer)) {
//									
//									
//									
//									
//									splitResult = nachricht.split(" _ ");
//									usr = splitResult[0];
//									msg = splitResult[1];
//									test += msg;
//								}
//
//							}
							
							
							details.add(new AnzeigeDetails(s.get("_id")
									.toString(), (String) s.get("start"),
									(String) s.get("ziel"), (String) s
											.get("strecke"), (String) s
											.get("uhrzeit"), (String) s
											.get("datum"), (String) s
											.get("fahrer"), Integer
											.parseInt((String) s
													.get("anzahl_plaetze")),
									(String) s.get("email"), "", "zugesagt",
									msg));

						}

						if (s.get("abgelehnteAnfragen").toString()
								.contains(nutzer)) {
							
//							for (int i = 0; i < nachrichtenfeld.size(); i++) {
//
//
//								nachricht = nachrichtenfeld.get(i).toString();
//								
//								
//								if (nachricht.contains(nutzer)) {
//									
//									
//									
//									
//									splitResult = nachricht.split(" _ ");
//									usr = splitResult[0];
//									msg = splitResult[1];
//									test += msg;
//								}
//
//							}
							
							details.add(new AnzeigeDetails(s.get("_id")
									.toString(), (String) s.get("start"),
									(String) s.get("ziel"), (String) s
											.get("strecke"), (String) s
											.get("uhrzeit"), (String) s
											.get("datum"), (String) s
											.get("fahrer"), Integer
											.parseInt((String) s
													.get("anzahl_plaetze")),
									(String) s.get("email"), "", "abgelehnt",
									msg));

						}
					}

				}

				mongoClient.close();

			} catch (Exception e) {

				e.printStackTrace();

			}

//			if(true) {
//				return ok(""+test);
//			}
			
			// user_statistiken bearbeiten und lesen

			List<Zaehler> zaehler = new ArrayList<>();

			try {
				MongoClient mongoClient = new MongoClient("localhost", 27017);
				DB db = mongoClient.getDB("play_basics");

				DBCollection coll = db.getCollection("user_statistiken");
				com.mongodb.DBCursor cursor = coll.find();
				BasicDBObject query = (BasicDBObject) new BasicDBObject(
						"username", nutzer);
				cursor = coll.find(query);

				BasicDBObject doc = new BasicDBObject();
				doc.put("anfragen", anfragen);

				BasicDBObject account = new BasicDBObject();
				account.put("$set", doc);

				coll.update(query, account);

				if (cursor.count() != 0) {

					for (DBObject s : cursor) {
						zaehler.add(new Zaehler((int) s.get("suchergebnisse"),
								(int) s.get("meine_mfgs"), (int) s
										.get("anfragen")));

					}

				}

				mongoClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			String facebook = "";

			if (session("facebook_logged") != null) {
				facebook = "Ja";
			}

			return ok(views.html.anwendung.anwendung_anfragen.render(
					"ProTramp Mitfahrgelegenheit", nutzer, "", typ, details,
					zaehler, facebook));

		}

	}

	// Diese Funktion ruft eine View auf indem der Fahrer alle angefragten
	// Personen sehen kann.
	// Funktion ist nur für den Fahrer zugänglich
	@SuppressWarnings("unchecked")
	public static Result mfg_anfrage_details(String id) {

		String nutzer = session("connected");
		String typ = session("typ");

		List<DBObject> feld = new ArrayList<>();
		List<DBObject> personenfeld = new ArrayList<>();
		List<DBObject> teilnehmerfeld = new ArrayList<>();

		List<AnzeigeDetails> details = new ArrayList<>();

		List<String> anfrageListe = new ArrayList<>();
		List<String> erfolgreicheListe = new ArrayList<>();

		List<Person> person = new ArrayList<>();
		List<Person> teilnehmer = new ArrayList<>();

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = db.getCollection("mfg");

			BasicDBObject query = (BasicDBObject) new BasicDBObject("_id",
					new ObjectId(id));

			feld = coll.find(query).toArray();

			for (DBObject s : feld) {

				StringBuilder sb = new StringBuilder();
				sb.append("");
				sb.append(anfrageListe.size());

				details.add(new AnzeigeDetails(s.get("_id").toString(),
						(String) s.get("start"), (String) s.get("ziel"),
						(String) s.get("strecke"), (String) s.get("uhrzeit"),
						(String) s.get("datum"), (String) s.get("fahrer"),
						Integer.parseInt((String) s.get("anzahl_plaetze")),
						(String) s.get("email"), sb.toString(), "", ""));

				anfrageListe = (List<String>) s.get("gestarteteAnfragen");

				if (anfrageListe.size() >= 1) {

					coll = db.getCollection("user");

					for (int i = 0; i < anfrageListe.size(); i++) {
						query = new BasicDBObject("username",
								anfrageListe.get(i));
						personenfeld = coll.find(query).toArray();

						for (DBObject p : personenfeld) {
							person.add(new Person(s.get("_id").toString(), p
									.get("username").toString(), p.get(
									"vorname").toString(), (int) p.get("alt"),
									p.get("email").toString(), p.get("tel")
											.toString()));
						}

					}

				}

				erfolgreicheListe = (List<String>) s
						.get("erfolgreicheAnfragen");

				if (erfolgreicheListe.size() >= 1) {

					coll = db.getCollection("user");

					for (int i = 0; i < erfolgreicheListe.size(); i++) {
						query = new BasicDBObject("username",
								erfolgreicheListe.get(i));
						teilnehmerfeld = coll.find(query).toArray();

						for (DBObject p : teilnehmerfeld) {
							teilnehmer.add(new Person(s.get("_id").toString(),
									p.get("username").toString(), p.get(
											"vorname").toString(), (int) p
											.get("alt"), p.get("email")
											.toString(), p.get("tel")
											.toString()));
						}

					}
				}

			}

			mongoClient.close();

		} catch (Exception e) {

			e.printStackTrace();

		}

		// user_statistiken lesen

		List<Zaehler> zaehler = new ArrayList<>();

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

		String facebook = "";

		if (session("facebook_logged") != null) {
			facebook = "Ja";
		}

		return ok(views.html.anwendung.mfg_anfragen_details.render(
				"ProTramp Mitfahrgelegenheit", nutzer, "", typ, details,
				zaehler, person, teilnehmer, facebook));
	}

	// Mit dieser Funktion können die Benutzer(Mitfahrer) Nachrichten lesen, die
	// vom
	// Fahrer verschickt wurden.
	@SuppressWarnings("unchecked")
	public static Result nachricht_lesen(String id) {

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

		List<String> nachrichtenfeld = new ArrayList<>();

		String nachricht = "";

		String[] splitResult;
		String usr = "";
		String msg = "";

		List<DBObject> feld = new ArrayList<>();
		List<Zaehler> zaehler = new ArrayList<>();

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = db.getCollection("mfg");

			BasicDBObject query = (BasicDBObject) new BasicDBObject("_id",
					new ObjectId(id));

			feld = coll.find(query).toArray();

			for (DBObject s : feld) {

				nachrichtenfeld = (List<String>) s.get("nachrichten");

				for (int i = 0; i < nachrichtenfeld.size(); i++) {

					if (nachrichtenfeld.toString().contains(nutzer)) {
						nachricht = nachrichtenfeld.get(i);
						splitResult = nachricht.split(" _ ");
						usr = splitResult[0];
						msg = splitResult[1];

					}

				}

			}

			// user_statistiken lesen

			coll = db.getCollection("user_statistiken");
			com.mongodb.DBCursor cursor = coll.find();
			query = (BasicDBObject) new BasicDBObject("username", nutzer);
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

		String facebook = "";

		if (session("facebook_logged") != null) {
			facebook = "Ja";
		}

		return ok(views.html.anwendung.nachricht.render(
				"ProTramp Mitfahrgelegenheit", nutzer, "", typ, zaehler, msg,
				facebook));
	}

	@SuppressWarnings("unchecked")
	public static Result FB() {

		DynamicForm requestData = Form.form().bindFromRequest();

		String nutzer = session("connected");
		String typ = session("typ");

		String email = "";
		List<Orte> ortsdetails = new ArrayList<>();
		List<Zaehler> zaehler = new ArrayList<>();

		if (nutzer == null) {

			List<DBObject> feld;

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

			email = requestData.get("fb_emailAdresse");
			String name = requestData.get("fb_name");
			String id = requestData.get("fb_idNr");
			String geburtsdatum = requestData.get("fb_birthday");
			String gender;
			String mitfahrer = "Mitfahrer";
			String[] splitResult = name.split(" ");
			String vorname = splitResult[0];
			String nachname = splitResult[1];

			String[] splitResultDatum = geburtsdatum.split("/");
			String monat = splitResultDatum[0];
			String tag = splitResultDatum[1];
			String jahr = splitResultDatum[2];

			if (requestData.get("fb_geschlecht").equals("male")) {
				gender = "Herr";
			} else {
				gender = "Frau";
			}

			// Das Alter wird zunaechst ermittelt
			String alter = "";
			int alt;

			GregorianCalendar today = new GregorianCalendar();
			GregorianCalendar past = new GregorianCalendar(
					Integer.parseInt(jahr), Integer.parseInt(monat) - 1,
					Integer.parseInt(tag));

			long difference = today.getTimeInMillis() - past.getTimeInMillis();
			int days = (int) (difference / (1000 * 60 * 60 * 24));
			double years = (double) days / 365;

			StringBuilder sb = new StringBuilder();
			sb.append("");
			sb.append(years);
			alter = sb.toString();

			DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
			String registrierungsdatum = df.format(today.getTime());

			int anfragen = 0;

			// Bestimmung des Alters als ganze Zahl
			alt = (int) Math.floor(Double.parseDouble(alter));

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

				// Die Collection des Nutzers finden
				coll = db.getCollection("user");
				com.mongodb.DBCursor cursor;
				query = (BasicDBObject) new BasicDBObject("email", email);

				feld = coll.find(query).toArray();
				cursor = coll.find(query);

				if (cursor.count() < 1) {
					BasicDBObject doc = new BasicDBObject("email", email)
							.append("titel", gender)
							.append("fahrertyp", mitfahrer)
							.append("geburtsdatum",
									tag + "." + monat + "." + jahr)
							.append("vorname", vorname)
							.append("name", nachname)
							.append("registrierungsdatum", registrierungsdatum)
							.append("alt", alt).append("tel", "")
							.append("password", "").append("username", email);

					coll.insert(doc);

					coll = db.getCollection("user_statistiken");
					cursor = coll.find();
					// Hier muss Email und Username beachtet werden
					query = (BasicDBObject) new BasicDBObject();

					doc = new BasicDBObject("username", email)
							.append("suchergebnisse", 0)
							.append("meine_mfgs", 0).append("anfragen", 0);

					coll.insert(doc);

					if (cursor.count() != 0) {

						for (DBObject s : cursor) {
							zaehler.add(new Zaehler((int) s
									.get("suchergebnisse"), (int) s
									.get("meine_mfgs"), (int) s.get("anfragen")));

						}

					}

				} else {
					coll = db.getCollection("user_statistiken");

					// Hier muss Email und Username beachtet werden
					query = (BasicDBObject) new BasicDBObject("username", email);
					cursor = coll.find();

					if (cursor.count() != 0) {

						for (DBObject s : cursor) {
							zaehler.add(new Zaehler((int) s
									.get("suchergebnisse"), (int) s
									.get("meine_mfgs"), (int) s.get("anfragen")));

						}

					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			// Hier wird die Session für den Benutzer angelegt, damit später bei
			// der Nutzung der MFG Seite immer festgestellt werden
			// kann welcher Benutzer gerade aktiv ist.
			session("connected", email);
			session("facebook_logged", "Ja");
			session("typ", "Mitfahrer");

			return redirect("/anwendung/mfg_anzeigen");

			/*
			 * return ok(views.html.anwendung.anwendung.render(
			 * "ProTramp Mitfahrgelegenheit", email, "", "", ortsdetails,
			 * zaehler, "Ok"));
			 */

		} else {
			return ok(views.html.anwendung.anwendung.render(
					"ProTramp Mitfahrgelegenheit", "", "", "", ortsdetails,
					zaehler, ""));

		}

	}

	public static Result FBLogout() {

		session().clear();

		return redirect("/");
	}

	// Funktion soll eine Anfrage mit Ja bestätigen. Anpassung der Datenbank und
	// MFG
	@SuppressWarnings("unchecked")
	public static Result zustimmen(String id, String user) {

		String nutzer = session("connected");
		String typ = session("typ");

		Map<String, String[]> parameters = request().body().asFormUrlEncoded();

		String nachricht = parameters.get("nachricht_zustimmen")[0];

		List<String> nachrichtenfeld = new ArrayList<>();

		List<DBObject> feld = new ArrayList<>();

		List<DBObject> personenfeld = new ArrayList<>();
		List<DBObject> teilnehmerfeld = new ArrayList<>();

		List<AnzeigeDetails> details = new ArrayList<>();

		List<Zaehler> zaehler = new ArrayList<>();

		List<String> anfrageListe = new ArrayList<>();
		List<String> erfolgreicheListe = new ArrayList<>();

		List<Person> person = new ArrayList<>();
		List<Person> teilnehmer = new ArrayList<>();

		int anfragen = 0;
		String email = "";

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = db.getCollection("mfg");

			BasicDBObject query = (BasicDBObject) new BasicDBObject("_id",
					new ObjectId(id));
			com.mongodb.DBCursor cursor = coll.find(query);

			feld = coll.find(query).toArray();

			for (DBObject s : feld) {

				nachrichtenfeld = (List<String>) s.get("nachrichten");

				if (!nachricht.isEmpty()) {
					nachrichtenfeld.add(user + " _ " + nachricht);

				}

				anfrageListe = (List<String>) s.get("gestarteteAnfragen");

				if (anfrageListe.contains(user)) {
					anfrageListe.remove(user);

				}

				erfolgreicheListe = (List<String>) s
						.get("erfolgreicheAnfragen");

				if (!erfolgreicheListe.contains(user)) {
					erfolgreicheListe.add(user);
				}

				// Datenbank aktualisieren

				BasicDBObject doc = new BasicDBObject();
				doc.put("gestarteteAnfragen", anfrageListe);
				doc.put("erfolgreicheAnfragen", erfolgreicheListe);
				doc.put("nachrichten", nachrichtenfeld);

				StringBuilder sb = new StringBuilder();
				sb.append("");

				int zahl = Integer.parseInt(s.get("anzahl_plaetze").toString());
				zahl -= 1;

				sb.append(zahl);

				doc.put("anzahl_plaetze", sb.toString());

				BasicDBObject account = new BasicDBObject();
				account.put("$set", doc);

				coll.update(query, account);

			}

			// Daten für die View holen

			for (DBObject s : feld) {

				StringBuilder sb = new StringBuilder();
				sb.append("");
				sb.append(anfrageListe.size());

				details.add(new AnzeigeDetails(s.get("_id").toString(),
						(String) s.get("start"), (String) s.get("ziel"),
						(String) s.get("strecke"), (String) s.get("uhrzeit"),
						(String) s.get("datum"), (String) s.get("fahrer"),
						Integer.parseInt((String) s.get("anzahl_plaetze")),
						(String) s.get("email"), sb.toString(), "", ""));

				anfrageListe = (List<String>) s.get("gestarteteAnfragen");

				// Personen auflisten, die noch angefragt haben

				if (anfrageListe.size() >= 1) {

					coll = db.getCollection("user");

					for (int i = 0; i < anfrageListe.size(); i++) {
						query = new BasicDBObject("username",
								anfrageListe.get(i));
						personenfeld = coll.find(query).toArray();

						for (DBObject p : personenfeld) {
							person.add(new Person(s.get("_id").toString(), p
									.get("username").toString(), p.get(
									"vorname").toString(), (int) p.get("alt"),
									p.get("email").toString(), p.get("tel")
											.toString()));
						}

					}

				}

				// Personen auflisten, die an der MFG teilnehmen werden

				erfolgreicheListe = (List<String>) s
						.get("erfolgreicheAnfragen");

				if (erfolgreicheListe.size() >= 1) {

					coll = db.getCollection("user");

					for (int i = 0; i < erfolgreicheListe.size(); i++) {
						query = new BasicDBObject("username",
								erfolgreicheListe.get(i));
						teilnehmerfeld = coll.find(query).toArray();

						for (DBObject p : teilnehmerfeld) {
							teilnehmer.add(new Person(s.get("_id").toString(),
									p.get("username").toString(), p.get(
											"vorname").toString(), (int) p
											.get("alt"), p.get("email")
											.toString(), p.get("tel")
											.toString()));
						}

					}
				}

			}

			// Anzahl der Anfragen zählen

			coll = db.getCollection("user");
			cursor = coll.find();
			query = (BasicDBObject) new BasicDBObject("username", nutzer);
			List<DBObject> feldEmail = coll.find(query).toArray();

			for (DBObject s : feldEmail) {
				email = (String) s.get("email");
			}

			coll = db.getCollection("mfg");
			cursor = coll.find();
			query = (BasicDBObject) new BasicDBObject("email", email);
			cursor = coll.find(query);

			feld = coll.find(query).toArray();

			for (DBObject d : feld) {

				anfrageListe = (List<String>) d.get("gestarteteAnfragen");

				if (anfrageListe.size() >= 1) {
					anfragen += anfrageListe.size();

				}

			}

			// Userstatistiken bearbeiten und lesen

			coll = db.getCollection("user_statistiken");
			query = (BasicDBObject) new BasicDBObject("username", nutzer);
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

		String facebook = "";

		if (session("facebook_logged") != null) {
			facebook = "Ja";
		}

		return ok(views.html.anwendung.mfg_anfragen_details.render(
				"ProTramp Mitfahrgelegenheit", nutzer, "", typ, details,
				zaehler, person, teilnehmer, facebook));
	}

	// Funktion soll eine Anfrage mit Nein ablehnen. Anpassung der Datenbank und
	// MFG
	@SuppressWarnings("unchecked")
	public static Result ablehnen(String id, String user) {

		String nutzer = session("connected");
		String typ = session("typ");
		
		String[] splitResult;
		String usr = "";
		String msg = "";

		Map<String, String[]> parameters = request().body().asFormUrlEncoded();

		String nachricht = parameters.get("nachricht_ablehnen")[0];
		List<String> nachrichtenfeld = new ArrayList<>();

		List<DBObject> feld = new ArrayList<>();

		List<DBObject> personenfeld = new ArrayList<>();
		List<DBObject> teilnehmerfeld = new ArrayList<>();

		List<AnzeigeDetails> details = new ArrayList<>();

		List<Zaehler> zaehler = new ArrayList<>();

		List<String> anfrageListe = new ArrayList<>();
		List<String> erfolgreicheListe = new ArrayList<>();
		List<String> abgelehnteListe = new ArrayList<>();

		List<Person> person = new ArrayList<>();
		List<Person> teilnehmer = new ArrayList<>();

		int anfragen = 0;
		String email = "";

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = db.getCollection("mfg");

			BasicDBObject query = (BasicDBObject) new BasicDBObject("_id",
					new ObjectId(id));

			feld = coll.find(query).toArray();

			for (DBObject s : feld) {

				nachrichtenfeld = (List<String>) s.get("nachrichten");


				if (!nachricht.isEmpty()) {
					nachrichtenfeld.add(user + " _ " + nachricht);

				}

				anfrageListe = (List<String>) s.get("gestarteteAnfragen");

				if (anfrageListe.contains(user)) {
					anfrageListe.remove(user);

				}

				abgelehnteListe = (List<String>) s.get("abgelehnteAnfragen");

				if (!abgelehnteListe.contains(user)) {
					abgelehnteListe.add(user);
				}

				// Datenbank aktualisieren

				BasicDBObject doc = new BasicDBObject();
				doc.put("gestarteteAnfragen", anfrageListe);
				doc.put("abgelehnteAnfragen", abgelehnteListe);
				doc.put("nachrichten", nachrichtenfeld);

				BasicDBObject account = new BasicDBObject();
				account.put("$set", doc);

				coll.update(query, account);

			}

			// Daten für die View holen

			for (DBObject s : feld) {

				StringBuilder sb = new StringBuilder();
				sb.append("");
				sb.append(anfrageListe.size());

				// anfragen += anfrageListe.size();

				details.add(new AnzeigeDetails(s.get("_id").toString(),
						(String) s.get("start"), (String) s.get("ziel"),
						(String) s.get("strecke"), (String) s.get("uhrzeit"),
						(String) s.get("datum"), (String) s.get("fahrer"),
						Integer.parseInt((String) s.get("anzahl_plaetze")),
						(String) s.get("email"), sb.toString(), "", ""));

				anfrageListe = (List<String>) s.get("gestarteteAnfragen");

				// Personen auflisten, die noch angefragt haben

				if (anfrageListe.size() >= 1) {

					coll = db.getCollection("user");

					for (int i = 0; i < anfrageListe.size(); i++) {
						query = new BasicDBObject("username",
								anfrageListe.get(i));
						personenfeld = coll.find(query).toArray();

						for (DBObject p : personenfeld) {
							person.add(new Person(s.get("_id").toString(), p
									.get("username").toString(), p.get(
									"vorname").toString(), (int) p.get("alt"),
									p.get("email").toString(), p.get("tel")
											.toString()));
						}

					}

				}

				erfolgreicheListe = (List<String>) s
						.get("erfolgreicheAnfragen");

				// Personen auflisten, die an der MFG teilnehmen werden

				if (erfolgreicheListe.size() >= 1) {

					coll = db.getCollection("user");

					for (int i = 0; i < erfolgreicheListe.size(); i++) {
						query = new BasicDBObject("username",
								erfolgreicheListe.get(i));
						teilnehmerfeld = coll.find(query).toArray();

						for (DBObject p : teilnehmerfeld) {
							teilnehmer.add(new Person(s.get("_id").toString(),
									p.get("username").toString(), p.get(
											"vorname").toString(), (int) p
											.get("alt"), p.get("email")
											.toString(), p.get("tel")
											.toString()));
						}

					}
				}

			}

			// Anzahl der Anfragen zählen

			coll = db.getCollection("user");
			com.mongodb.DBCursor cursor = coll.find();
			query = (BasicDBObject) new BasicDBObject("username", nutzer);
			List<DBObject> feldEmail = coll.find(query).toArray();

			for (DBObject s : feldEmail) {
				email = (String) s.get("email");
			}

			coll = db.getCollection("mfg");
			cursor = coll.find();
			query = (BasicDBObject) new BasicDBObject("email", email);
			cursor = coll.find(query);

			feld = coll.find(query).toArray();

			for (DBObject d : feld) {

				anfrageListe = (List<String>) d.get("gestarteteAnfragen");

				if (anfrageListe.size() >= 1) {
					anfragen += anfrageListe.size();

				}

			}

			// Userstatistiken bearbeiten und lesen

			coll = db.getCollection("user_statistiken");
			query = (BasicDBObject) new BasicDBObject("username", nutzer);
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

		String facebook = "";

		if (session("facebook_logged") != null) {
			facebook = "Ja";
		}

		return ok(views.html.anwendung.mfg_anfragen_details.render(
				"ProTramp Mitfahrgelegenheit", nutzer, "", typ, details,
				zaehler, person, teilnehmer, facebook));

	}

}
