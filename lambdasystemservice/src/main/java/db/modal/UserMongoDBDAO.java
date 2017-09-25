//package db.modal;
//
//import com.maanadev.mongo.MongodbImplement;
//import launch.Launcher;
//import org.jongo.MongoCursor;
//
//public class UserMongoDBDAO {
//
//    private final static String COLLECTION= Launcher.getString("mongoDB.user-collection");
//    private final static String DB_NAME= Launcher.getString("mongoDB.db-name");
//
//
//    private UserMongoDBDAO(){}
//
//
//    public static void saveUser(User t){
//        MongodbImplement<User> client = getUserMongodbImplement();
//        client.save(t);
//client.close();
//    }
//
//    private static MongodbImplement<User> getUserMongodbImplement() {
//        return new MongodbImplement<>( Launcher.getString("mongoDB.host"), Launcher.getInt("mongoDB.port"),DB_NAME,COLLECTION,User.class);
//    }
//
//
//    public static User getUser(long userId) {
//        MongodbImplement<User> client = getUserMongodbImplement();
//        MongoCursor<User>cursor= client.find("key",String.valueOf(userId));
//        User user=null;
//        while(cursor.hasNext()){
//            user=cursor.next();
//        }
//        client.close();
//        return user;
//    }
//
//}
