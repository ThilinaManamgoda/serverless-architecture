package db.modal;


/**
 * Created by deshan on 9/24/17.
 */
public class Function {
    String domainName;
    String name;
    String bucket;
    String event;
    FileType type;




    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getName() {
        return name;
    }

    public void setName(String objName) {
        this.name = objName;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }


    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }


}
