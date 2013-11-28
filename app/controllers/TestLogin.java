package controllers;

import java.net.UnknownHostException;

import java.util.Map;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class TestLogin extends Controller {

	public static Result showLoginForm() {

		return redirect("/assets/html/loginform.html");
	}

	public static Result test() {

		Map<String, String[]> parameters = request().body().asFormUrlEncoded();
		// String requestBody = request().body().asText();

		if (!(parameters != null && parameters.containsKey("testEingabe"))) {
			// return badRequest("go away");
			Logger.warn("bad login request");
			return showLoginForm();
		}

		String testEingabe = parameters.get("testEingabe")[0];

		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("play_basics");
			DBCollection coll = db.getCollection("test");
			BasicDBObject doc = new BasicDBObject("eingabe", testEingabe);
			coll.insert(doc);
			mongoClient.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		return redirect("/assets/html/testVerbindung.html");

		// return ok("Hello. Dein Request war: " + parameters.keySet()
		// + " testEingabe:" + testEingabe);

	}

}
