package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;






import models.KontoDetails;
import play.Logger;
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

//Die Klasse Konto ermöglicht dem Benutzer, seine Kontodaten zu ändern wie Kennwort oder das Löschen des Benutzerkontos

public class Konto extends Controller {

	//Die Funktion liefert dem Benutzer eine geordnete Übersicht über sein eigenes Konto
	
	public static Result kontoeinstellungen_index() {
		String nutzer = session("connected");
		List<KontoDetails> details = new ArrayList<>();

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			// Die Collection des Nutzers finden
			DBCollection coll = db.getCollection("user");
			com.mongodb.DBCursor cursor = coll.find();
			BasicDBObject query = (BasicDBObject) new BasicDBObject("username",
					nutzer);
			cursor = coll.find(query);

			for (DBObject s : cursor) {
				details.add(new KontoDetails((String) s
						.get("registrierungsdatum"),
						(String) s.get("username"), (String) s.get("email")));
			}

			mongoClient.close();

		} catch (Exception e) {

		}
		
		String facebook = "";
		
		if(session("facebook_logged") != null) {
			facebook = "Ja";
		}
		
		return ok(views.html.konto.kontoeinstellungen.render(
				"ProTramp Mitfahrgelegenheit", nutzer, "", details, facebook));
	}


	//Falls sich der Benutzer dazu entscheiden sollte, sein Kennwort zu ändern dann wird diese Funktion aufgerufen
	
	public static Result kontoeinstellungen_aendern() {
		String nutzer = session("connected");
		
		String facebook = "";
		
		if(session("facebook_logged") != null) {
			facebook = "Ja";
		}

		Map<String, String[]> parameters = request().body().asFormUrlEncoded();

		// Parameteruebergaben werden ueberprueft
		
		  if (!(parameters != null &&
		  parameters.containsKey("kontoeinstellungen_pss") &&
		  parameters.containsKey("kontoeinstellungen_pssneu") &&
		  parameters.containsKey("kontoeinstellungen_psswdh") )) {
		  
			  Logger.warn("bad login request"); 
			  return redirect("/konto/index"); 
		  
		  }
		  
		  
		  List<KontoDetails> details = new ArrayList<>();

		  //Hier wird die Liste ergänzt, die dem Benutzer gehört
			try {
				MongoClient mongoClient = new MongoClient("localhost", 27017);
				DB db = mongoClient.getDB("play_basics");

				// Die Collection des Nutzers finden
				DBCollection coll = db.getCollection("user");
				com.mongodb.DBCursor cursor = coll.find();
				BasicDBObject query = (BasicDBObject) new BasicDBObject("username",
						nutzer);
				cursor = coll.find(query);

				for (DBObject s : cursor) {
					details.add(new KontoDetails((String) s
							.get("registrierungsdatum"),
							(String) s.get("username"), (String) s.get("email")));
				}

				mongoClient.close();

			} catch (Exception e) {

			}  
		  
		  
		 
		String altesPasswort = parameters.get("kontoeinstellungen_pss")[0];
		String neuesPasswort = parameters.get("kontoeinstellungen_pssneu")[0];
		String neuesPasswort_wdh = parameters.get("kontoeinstellungen_psswdh")[0];

		boolean gueltig = false;

		//Hier wird überprüft ob das Kennwort richtig eingegeben wurde
		if (altesPasswort == "") {
			return ok(views.html.konto.kontoeinstellungen.render("ProTramp Mitfahrgelegenheit", nutzer, "Ihre Eingaben waren nicht gültig. Versuchen Sie es erneut.", details, facebook));

		} else {

			//Hier wird überprüft ob das Kennwort korrekt wiederholt wurde
			if ((neuesPasswort.equals(neuesPasswort_wdh) )) {


				try {
					MongoClient mongoClient = new MongoClient("localhost",27017);
					DB db = mongoClient.getDB("play_basics");

					// Die Collection des Nutzers finden
					DBCollection coll = db.getCollection("user");
					com.mongodb.DBCursor cursor = coll.find();
					BasicDBObject query = new BasicDBObject();
					query.put("username", nutzer);
					
					cursor = coll.find(query);					
					String pw = "";
					
					for (DBObject s : cursor) {						
						pw = s.get("password").toString();

					}

					if (altesPasswort.equals(pw)) {
						gueltig = true;
					}

					if (!gueltig) {
						return ok("PW: " + pw + " altesPasswort: "
								+ altesPasswort);
					}

					if (gueltig) {						
						
						// falls der Änderungsprozess bisher korrekt lief, wird das Kennwort in der Datenbank geändert
						BasicDBObject doc = new BasicDBObject();
						doc.put("password", neuesPasswort);
						
						BasicDBObject account = new BasicDBObject();
						account.put("$set", doc);

						coll.update(query, account);

					}

					mongoClient.close();

				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				
				//falls der Benutzer das Kennwort nicht korrekt eingegeben hat wird er darauf mit einem Hinweis benachrichtigt
				
				
				
				return ok(views.html.konto.kontoeinstellungen.render(
						"ProTramp Mitfahrgelegenheit", nutzer, "Das Passwort wurde nicht korrekt wiederholt. Versuchen Sie es erneut.", details, facebook));
			}

		}

		return redirect("/konto/index");

	}
	
	//Diese Funktion entfernt das Konto des Benutzers
	//Die Durchführung erfolgt wenn der Benutzer sein Kennwort korrekt eingibt
	
	public static Result kontoEntfernen() {
		String nutzer = session("connected");
		
		Map<String, String[]> parameters = request().body().asFormUrlEncoded();
		
		// Parameteruebergaben werden ueberprueft
		
		  if (!(parameters != null &&
		  parameters.containsKey("kontoeinstellungen_pssbst") )) {
		  
			  Logger.warn("bad login request"); 
			  return redirect("/konto/index"); 
		  
		  }
		  
		  String pwEingabe = parameters.get("kontoeinstellungen_pssbst")[0];
		  String pwKonto = "";
		  boolean fertig = false;
		  
		  try {
			  MongoClient mongoClient = new MongoClient("localhost",27017);
				DB db = mongoClient.getDB("play_basics");

				// Die Collection des Nutzers finden
				DBCollection coll = db.getCollection("user");
				com.mongodb.DBCursor cursor = coll.find();
				BasicDBObject query = new BasicDBObject();
				query.put("username", nutzer);
				
				cursor = coll.find(query);
				
				
				for(DBObject s : cursor) {
					pwKonto = (String) s.get("password");
					
					
				}
				
				if(pwKonto.equals(pwEingabe)) {
					

					coll.remove(query);					
					fertig = true;	
				}
				
				
				mongoClient.close();
			  
		  } catch(Exception e) {
			  e.printStackTrace();
		  }
		
		
		if(fertig) {
			
			//falls das Entfernen des Kontos funktioniert hat, wird der Benutzer ausgeloggt und auf die Standardhauptseite geladen
			session().clear();
			return redirect("/");
		} else {
			
			List<KontoDetails> details = new ArrayList<>();

			try {
				MongoClient mongoClient = new MongoClient("localhost", 27017);
				DB db = mongoClient.getDB("play_basics");

				// Die Collection des Nutzers finden
				DBCollection coll = db.getCollection("user");
				com.mongodb.DBCursor cursor = coll.find();
				BasicDBObject query = (BasicDBObject) new BasicDBObject("username",
						nutzer);
				cursor = coll.find(query);

				for (DBObject s : cursor) {
					details.add(new KontoDetails((String) s
							.get("registrierungsdatum"),
							(String) s.get("username"), (String) s.get("email")));
				}

				mongoClient.close();

			} catch (Exception e) {

			}
			
			String facebook = "";
			
			if(session("facebook_logged") != null) {
				facebook = "Ja";
			}
			
			return ok(views.html.konto.kontoeinstellungen.render(
					"ProTramp Mitfahrgelegenheit", nutzer, "", details, facebook));
		}
		
	}

}
