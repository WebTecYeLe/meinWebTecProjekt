package controllers;

import models.Thing;

import org.mongojack.DBCursor;

import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import db.ThingDB;

public class Things extends Controller {
	/**
	 * display list of things
	 * 
	 * @return
	 */
	public static Result list() {
		// things von DB lesen
		// render view

		DBCursor<Thing> things = ThingDB.get().list(0, 20);
		int count = things.count();
		String message = flash().get("message");
		return ok(views.html.things.things.render("Things", things.iterator(),
				count, message));
	}

	public static Result showNew() {
		Thing thing = new Thing();
		thing.what = "POI";
		thing.specifyLocation(9.12, 47.672446);
		Form<Thing> thingForm = fillForm(thing);

		return ok(views.html.things.newthing.render("Neues Thing anlegen",
				thingForm));
	}

	private static Form<Thing> fillForm(Thing thing) {
		Form<Thing> thingForm = Form.form(Thing.class);
		thingForm = thingForm.fill(thing);

		if (thing.hasLocation()) {
			thingForm.data().put("longitude", "" + thing.longitude());
			thingForm.data().put("latitude", "" + thing.latitude());
		}
		return thingForm;
	}

	public static Result show(String id) {
		Thing thing = ThingDB.get().findById(id);
		
		if (null == thing) {
			flash("message", "Thing nicht gefunden");
			return redirect(routes.Things.list());
		} else {
			Form<Thing> form = fillForm(thing);
			return ok(views.html.things.editthing.render("Bearbeiten", form,
					thing._id));
		}

	}
	
	public static Result delete(String id) {
		Thing thing = ThingDB.get().findById(id);
		
		if (null == thing) {
			flash("message", "Thing nicht gefunden");
			return redirect(routes.Things.list());
		} else {
			ThingDB.get().delete(id);
			flash("message", "Thing gelöscht: " +thing.name);
			return redirect(routes.Things.list());
		}

	}

	public static Result create() {

		Form<Thing> form = Form.form(Thing.class).bindFromRequest();
		if (form.hasErrors()) {
			return badRequest(views.html.things.newthing.render(
					"Fehler Thing anlegen", form));
		} else {
			Thing thing = form.get();

			String longitude = form.data().get("longitude");
			String latitude = form.data().get("latitude");

			thing.specifyLocation(Double.valueOf(longitude),
					Double.valueOf(latitude));

			ThingDB.get().create(thing);
			flash("message", "Neues Thing '" + thing.name + "' wurde angelegt");
			return redirect(routes.Things.list());

		}
	}

	public static Result save(String id) {
		//search for thing
		
		Thing saved = ThingDB.get().findById(id);
		if(saved == ThingDB.get().findById(id)){
			flash("message", "Nicht gefunden");
			return redirect(routes.Things.list());
		}
		
		//validate form
		Form<Thing> form = Form.form(Thing.class).bindFromRequest();
		if (form.hasErrors()) {
			return badRequest(views.html.things.editthing.render(
					"Fehler Thing bearbeiten", form, id));
		} else {
			//save thing
			Thing update = form.get();
			
			String longitude = form.data().get("longitude");
			String latitude = form.data().get("latitude");

			if (null != latitude && null != longitude) {
				saved.specifyLocation(Double.valueOf(longitude),
						Double.valueOf(latitude));
			}
			saved.name = update.name;
			saved.what = update.what;
			ThingDB.get().save(saved);
			flash("message", "Gespeichert");
			return redirect(routes.Things.list());
		}
	}
	
	
	/**
	 * 
	 * @param distance max distance n meters
	 * @param longitude
	 * @param latitude
	 * @return
	 */
	public static Result near(Integer distance, Double longitude, Double latitude) {
		Logger.info("search near. distance:" +distance +" longitude:" +longitude +" latitude:" +latitude) ;

		//NOTE: the default values are not sent with the request. I.e. they do not appear in the form
		//when binding from request. Better to take them form the object instead reading dynamic form 
		NearSearchParams searchParams = new NearSearchParams(distance, longitude, latitude);
		Form<NearSearchParams> searchForm = Form.form(NearSearchParams.class).fill(searchParams);
		    
		DBCursor<Thing> things = ThingDB.get().findThingsNear(longitude, latitude, distance);
		
		int count = things.count();
		return ok(views.html.things.thingsnear.render("Things in der Nähe", things.iterator(),
				count, searchForm));
	}
}
