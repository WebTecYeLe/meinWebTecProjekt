package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import models.KontoDetails;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http.Session;
import play.mvc.Result;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class Konto extends Controller {

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
		return ok(views.html.konto.kontoeinstellungen.render(
				"ProTramp Mitfahrgelegenheit", nutzer, "", details));
	}

	public static Result kontoeinstellungen_aendern() {
		String nutzer = session("connected");

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

		if (altesPasswort == "") {
			return ok(views.html.konto.kontoeinstellungen.render("ProTramp Mitfahrgelegenheit", nutzer, "Ihre Eingaben waren nicht gültig. Versuchen Sie es erneut.", details));

		} else {

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
				return ok(views.html.konto.kontoeinstellungen.render(
						"ProTramp Mitfahrgelegenheit", nutzer, "Das Passwort wurde nicht korrekt wiederholt. Versuchen Sie es erneut.", details));
			}

		}

		return redirect("/konto/index");

	}
	
	
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
			
			return ok(views.html.konto.kontoeinstellungen.render(
					"ProTramp Mitfahrgelegenheit", nutzer, "", details));
		}
		
	}

}
