package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

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
		/*
		 * if (!(parameters != null &&
		 * parameters.containsKey("kontoeinstellungen_pss") &&
		 * parameters.containsKey("kontoeinstellungen_pssneu") &&
		 * parameters.containsKey("kontoeinstellungen_psswdh") )) {
		 * 
		 * Logger.warn("bad login request"); return ok("Fehler beim Senden.");
		 * //return redirect("/konto/index"); }
		 */
		String altesPasswort = parameters.get("kontoeinstellungen_pss")[0];
		String neuesPasswort = parameters.get("kontoeinstellungen_pssneu")[0];
		String neuesPasswort_wdh = parameters.get("kontoeinstellungen_psswdh")[0];

//		int altesPasswortInt = Integer.parseInt(altesPasswort);
//		int neuesPasswortInt = Integer.parseInt(neuesPasswort);
//		int neuesPasswort_wdhInt = Integer.parseInt(neuesPasswort_wdh);

//		return ok("altesPasswort:" + altesPasswortInt + "neuesPasswort:" +
//		neuesPasswortInt + "neuesPasswort_wdh:"+neuesPasswort_wdhInt);

		boolean gueltig = false;

		if (altesPasswort == "") {

			return redirect("/konto/index");

		} else {

			// return ok("altesPasswort: "+altesPasswort);

			if ((neuesPasswort.equals(neuesPasswort_wdh) )) {


				try {
					MongoClient mongoClient = new MongoClient("localhost",27017);
					DB db = mongoClient.getDB("play_basics");

					// Die Collection des Nutzers finden
					DBCollection coll = db.getCollection("user");
					com.mongodb.DBCursor cursor = coll.find();
					BasicDBObject query = (BasicDBObject) new BasicDBObject("username", nutzer);
					
					cursor = coll.find(query);
					
					
					
					String pw = "";
					String email = "";
					String titel = "";
					String fahrertyp = "";
					String geburtsdatum = "";
					int alt = 0 ;
					String vorname = "";
					String name = "";
					String tel = "";
					String registrierungsdatum = "";
					
					
					
					for (DBObject s : cursor) {
						
						pw = s.get("password").toString();
						email = (String) s.get("email");
						titel = (String) s.get("titel");	
						fahrertyp = (String) s.get("fahrertyp");
						geburtsdatum = (String) s.get("geburtsdatum");
						alt =  (int) s.get("alt");
						vorname = (String) s.get("vorname");
						name = (String) s.get("name");
						tel = (String) s.get("tel");
						registrierungsdatum = (String) s
								.get("registrierungsdatum");
					}
					
					
					
//					return ok("Ich war da");
						
								
					
					if (altesPasswort.equals(pw)) {
						gueltig = true;
					}

					if (!gueltig) {
						return ok("PW: " + pw + " altesPasswort: "
								+ altesPasswort);
					}

					if (gueltig) {
						BasicDBObject doc = new BasicDBObject();
						
						
						for (DBObject s : cursor) {
						
						doc.put("username", nutzer);
						doc.put("password", pw);
						doc.put("email", email);
						doc.put("titel", titel);
						doc.put("fahrertyp", fahrertyp);
						doc.put("geburtsdatum", geburtsdatum);
						doc.put("alt", alt);
						doc.put("vorname", vorname);
						doc.put("name", name);
						doc.put("tel", tel);
						doc.put("registrierungsdatum",
								registrierungsdatum);
							}

						coll.update(query, doc);

					}

					mongoClient.close();

				} catch (Exception e) {
					e.printStackTrace();
					return ok("Fehler beim Verarbeiten.");
				}

			} else {
				return redirect("/konto/index");
			}

		}

		return redirect("/");

	}

}
