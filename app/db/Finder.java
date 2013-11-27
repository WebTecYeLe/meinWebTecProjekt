package db;

import org.bson.types.ObjectId;
import org.mongojack.JacksonDBCollection;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

/**
 * Generic Finder for mongoDB to be used for a specific collection
 * 
 * @author mike
 * 
 * @param <T>
 */
public class Finder<T> {
	protected String collectionName;
	protected Class<T> entityType;
	protected DB db;

	public Finder(DB db, String collectionName, Class<T> entityType) {
		this.entityType = entityType;
		this.collectionName = collectionName;
		this.db = db;
	}

	/**
	 * Convenience method for query
	 */
	public static BasicDBObject q(String key, Object value) {
		return new BasicDBObject(key, value);
	}

	public static BasicDBObject near(double longitude, double latitude,
			double maxDistance) {
		return q(
				"$near",
				q("$geometry",
						q("type", "Point").append("coordinates",
								new double[] { longitude, latitude }))).append(
				"$maxDistance", maxDistance);
	}

	// TODO find near and return distance
	// public static BasicDBObject geoNear(double longitude, double latitude,
	// double maxDistance){
	// BasicDBObject myCmd = new BasicDBObject();
	// myCmd.append("geoNear", "mycollection");
	// double[] loc = {-121.97679901123047,37.557369232177734};
	// myCmd.append("near", loc);
	// myCmd.append("spherical", true);
	// myCmd.append("maxDistance", (double)50 / 3959 );
	// System.out.println(myCmd);
	// CommandResult myResults = db.command(myCmd);
	//
	//
	//
	// return q("$near", new double[] { longitude, latitude }).append(
	// "$maxDistance", maxDistance);
	// }

	public T findById(String id) {
		try {
			return getColl().findOneById(id);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * get the ORM mapped collection
	 * 
	 * @return
	 */
	public JacksonDBCollection<T, String> getColl() {
		return JacksonDBCollection.wrap(db.getCollection(collectionName),
				entityType, String.class);
	}

	public DBCollection getMongoCollection() {
		return db.getCollection(collectionName);
	}

	/**
	 * find using native MongoDB query
	 * 
	 * @param nativeQuery
	 * @return
	 */
	public org.mongojack.DBCursor<T> findNative(BasicDBObject nativeQuery) {
		com.mongodb.DBCursor mongoCursor = db.getCollection(collectionName)
				.find(nativeQuery);
		return new org.mongojack.DBCursor<T>(getColl(), mongoCursor);
	}

	public T save(T entity) {
		return getColl().save(entity).getSavedObject();
	}
	public T delete(String id) {
		return getColl().findAndRemove(q("_id", new ObjectId( id) ));
	}

	public long count() {
		return getColl().count();
	}

}
