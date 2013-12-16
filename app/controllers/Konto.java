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
						&& parameters.containsKey("username")
						&& parameters.containsKey("email")
						)) {

					Logger.warn("bad login request");
					return redirect("/konto/index");
				}
		
		
				String altesPasswort = parameters.get("kontoeinstellungen_pss")[0];
				String neuesPasswort = parameters.get("username")[0];
				String neuesPasswort_wdh = parameters.get("email")[0];
				
				if(altesPasswort == "") {
					
					
				} else {
					
				}
				

		return ok();
	}

}
