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
				"ProTramp Mitfahrgelegenheit", nutzer, details));
	}

	
	
	public static Result kontoeinstellungen_aendern() {
		String nutzer = session("connected");
		
		Map<String, String[]> parameters = request().body().asFormUrlEncoded();
		
		// Parameteruebergaben werden ueberprueft
				if (!(parameters != null
						&& parameters.containsKey("kontoeinstellungen_pss")
						&& parameters.containsKey("kontoeinstellungen_pssneu")
						&& parameters.containsKey("kontoeinstellungen_psswdh")
						)) {

					Logger.warn("bad login request");
					return ok("Fehler beim Senden.");
					//return redirect("/konto/index");
				}
		
		
				String altesPasswort = parameters.get("kontoeinstellungen_pss")[0];
				String neuesPasswort = parameters.get("kontoeinstellungen_pssneu")[0];
				String neuesPasswort_wdh = parameters.get("kontoeinstellungen_psswdh")[0];
				
				
				
				boolean gueltig = false;
				
				if(altesPasswort == "") {
					return redirect("/konto/index");
					
				} else {
					if(neuesPasswort.equals(neuesPasswort_wdh) && neuesPasswort != "" && neuesPasswort_wdh != "") {
						
						
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
								if(s.get("password").equals(altesPasswort)) {
									gueltig = true;
									
								}
								
							}
							
							
							
							if(gueltig) {
								BasicDBObject doc = new BasicDBObject();
								
								if (cursor.count() != 0) {
									for (DBObject s : cursor) {
										
										doc.put("password", neuesPasswort);

									}

									
								}

								coll.update(query, doc);
								
								
							}
							

							mongoClient.close();

							
						} catch(Exception e) {
							e.printStackTrace();
						}
						
						
					} else {
						return redirect("/konto/index");
					}
					
					
				}
				

		return redirect("/");
	}

}
