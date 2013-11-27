package db;

import java.util.Date;

import models.Thing;

import org.mongojack.DBCursor;

import play.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;


public class ThingDB extends Finder<Thing>{
	public ThingDB(DB db) {
		super(db, COLLECTION_THINGS, Thing.class);
	}

	/**
	 * collection name for things
	 */
	public static final String COLLECTION_THINGS = "things";
	private static ThingDB instance = new ThingDB(DBConnect.getDB());
	
	
	public static void init() {
		Logger.info("Initializing DB. Ensuring indexes.");
		/**
		 * geo index
		 */
		get().db.getCollection(COLLECTION_THINGS).ensureIndex(new BasicDBObject("lonLat", "2dsphere"));
	}
	
	public static ThingDB get(){
		return instance;
	}
	
	public Thing create(Thing thing){
		thing.dateCreated = new Date();
		return save(thing);
	}
	/**
	 * @param skip offset 
	 * @param maxNum maximum number to return
	 */
	public DBCursor<Thing> list(int skip, int maxNum){
		DBCursor<Thing> things = getColl().find().skip(skip).limit(maxNum);
		return things;
	}
	
	public org.mongojack.DBCursor<Thing> findThingsNear(double longitude,
			double latitude, double distance) {
		return findNative(q("lonLat", near(longitude, latitude, distance)));

	}
	
}
