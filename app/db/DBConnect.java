package db;

import java.net.UnknownHostException;

import play.Logger;

import com.mongodb.DB;
import com.mongodb.MongoClient;



/**
 * Connection for MongoDB
 * @author mike
 *
 */
public class DBConnect {
	/**
	 * name of the mongodb collection
	 */
	public static final String COLLECTION_THINGS = "things";

	private static MongoClient mongo;
	private static DB db;
	
	/**
	 * get mondo DB 
	 * @return
	 */
	public static DB getDB(){
		if(null == db){
			try {
				// client is thread safe - use the same client for all requests 
				// see http://docs.mongodb.org/ecosystem/drivers/java-concurrency/#java-driver-concurrency
				mongo = new MongoClient( "localhost" , 27017 );
				Logger.info("MongoClient created");
			} catch (UnknownHostException e) {
				throw new RuntimeException("Failed to connect to db", e);
			}
			db = mongo.getDB("play_basics");
		}
		
		return db;
	}
	
	public static void dispose(){
		Logger.info("releasing MongoClient");
		if(null != mongo){
			mongo.close();
			mongo = null;
			db = null;
		}
	}
	
}
