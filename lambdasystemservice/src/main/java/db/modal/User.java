package db.modal;


import java.util.ArrayList;

public class User {
    // this is little bit confusing key = id Mongo DB requirement
    private long key;

    String domain;
    String functionName;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public ArrayList<Function> getFunctions() {
        return functions;
    }

    public void setFunctions(ArrayList<Function> functions) {
        this.functions = functions;
    }

    ArrayList<Function> functions=new ArrayList<>();
    public void addFunction(Function function){
        functions.add(function);
    }

    public long getId() {
        return key;
    }

    public void setId(long id) {
        this.key = id;
    }
}
