package db.modal;

import lambda.netty.loadbalancer.core.loadbalance.JsonHelper;

public class UserJsonHelp extends JsonHelper<User> {
    private static UserJsonHelp instance = new UserJsonHelp(User.class);

    protected UserJsonHelp(Class<User> t) {
        super(t);
    }



    public static String ToJson(User user){
        return instance.ObjToJson(user);
    }

    public static User JsonToUser(String user){
        return instance.jsonToObj(user);
    }


}
