package dc.boc.json.GameInfo;

/**
 * Created by eshixiu on 11/6/18.
 */
public class Event {

    private String latitude;
    private String longitude;
    private String event_time;
    private String event_type;
    private String page_no;
    private String widget_no;
    private String object_type;
    private String object_val;

    public String getOperat_id() {
        return operat_id;
    }

    public void setOperat_id(String operat_id) {
        this.operat_id = operat_id;
    }

    private String operat_id ;
    private ObjectProperty object_property;

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getEvent_time() {
        return event_time;
    }

    public void setEvent_time(String event_time) {
        this.event_time = event_time;
    }

    public String getEvent_type() {
        return event_type;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }

    public String getPage_no() {
        return page_no;
    }

    public void setPage_no(String page_no) {
        this.page_no = page_no;
    }

    public String getWidget_no() {
        return widget_no;
    }

    public void setWidget_no(String widget_no) {
        this.widget_no = widget_no;
    }

    public String getObject_type() {
        return object_type;
    }

    public void setObject_type(String object_type) {
        this.object_type = object_type;
    }

    public String getObject_val() {
        return object_val;
    }

    public void setObject_val(String object_val) {
        this.object_val = object_val;
    }

    public ObjectProperty getObject_property() {
        return object_property;
    }

    public void setObject_property(ObjectProperty object_property) {
        this.object_property = object_property;
    }


}
