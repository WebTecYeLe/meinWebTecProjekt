package controllers;

import java.util.Date;

import play.mvc.Controller;
import play.mvc.Http.Cookie;
import play.mvc.Result;

/**
 * Einfache Beispiele
 * @author mike
 *
 */
public class ExampleController extends Controller{
	
	/**
	 * weiterleitung zu statischer Seite
	 * @return
	 */
	public static Result index(){
		return redirect("/assets/html/start.html");
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
