package dc.boc.json.GameInfo;

/**
 * Created by eshixiu on 11/5/18.
 */
public class GameRequest {
    private OS os;
    private User user;
    private Event event;
    private Api Api;

    public OS getOs() {
        return os;
    }

    public void setOs(OS os) {
        this.os = os;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Api getApi() {
        return Api;
    }

    public void setApi(Api api) {
        Api = api;
    }
}
