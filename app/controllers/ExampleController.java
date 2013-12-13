package controllers;

import java.util.ArrayList;
import java.util.Date;



import play.mvc.Controller;
import play.mvc.Http.Cookie;
import play.mvc.Result;

import java.util.List;

import models.Orte;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;


public class ExampleController extends Controller{
	
	/**
	 * weiterleitung zu statischer Seite
	 * @return
	 */
	public static Result index(){
		
		//Hier muss der User überprüft werden ob er bereits schon eingeloggt ist		
			
		String user = session("connected");
		String typ = session("typ");
		
		List <Orte> ortsdetails = new ArrayList<>();
    	List <DBObject> feld;

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");

			DBCollection coll = db.getCollection("mfg");
			BasicDBObject query = new BasicDBObject();

			feld = coll.find(query).toArray();
			
			String vergleicher = "";
			
			
			for (DBObject s : feld) {
				
				vergleicher = s.get("start").toString();
				if(!ortsdetails.toString().contains(vergleicher)) {
					ortsdetails.add(new Orte(s.get("start").toString()));
					
				}
				
				vergleicher = s.get("ziel").toString();
				if(!ortsdetails.toString().contains(vergleicher)) {
					ortsdetails.add(new Orte(s.get("ziel").toString()));
					
				}
				
				vergleicher = s.get("strecke").toString();
				if(!ortsdetails.toString().contains(vergleicher)) {
					ortsdetails.add(new Orte(s.get("strecke").toString()));
					
				}
				
			}
			
			
			mongoClient.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		if(typ != null) {
			if(!typ.equals("Fahrer")) {
				typ = "";
				
			}
			
			//typ = "Fahrer";
			
		} else {
			typ = "";
		}
		
		if(user != null) {
			return ok(views.html.anwendung.anwendung.render("ProTramp Mitfahrgelegenheit", user, "", typ, ortsdetails));
		} else {
			return ok(views.html.anwendung.anwendung.render("ProTramp Mitfahrgelegenheit", "", "", typ, ortsdetails));
			
		}
		
		
	}
	
	/**
	 * Einfache Ausgabe in Response
	 * @return
	 */
	public static Result output(){
		
		//testen:
		// response().setContentType("text/html");
		// return ok("<h1>Hello World!</h1>").as("text/html");
		return ok("Hallo");
	}
	
	/**
	 * Besuch-Zähler mit Cookie
	 * @return
	 */
	public static Result cookiecounter(){
		Cookie counter = request().cookie("counter");
		int count;
		if(null == counter){
			count = 0;
		}else{
			count = Integer.valueOf(counter.value());
		}
		count++;
		response().setCookie("counter", "" +count);
		return ok("Hallo. Dein Besuch Nummer " +count);
	}
	
	/**
	 * einfaches scala template mit dynamischem Inhalt
	 * @return
	 */
	public static Result simpletemplate(){
		String message = "Auf dem Server ist es diese Zeit";
        Date date = new Date();
		return ok(views.html.example.simpletemplate.render("Beispiel", message, date)); 
	}
}
