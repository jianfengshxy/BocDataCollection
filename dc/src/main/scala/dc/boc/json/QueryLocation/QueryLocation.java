package dc.boc.json.QueryLocation;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eshixiu on 11/6/18.
 */
public class QueryLocation {
    private String distance;

    private List<Address> addrList = new ArrayList<Address>();


    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public List<Address> getAddrList() {
        return addrList;
    }

    public void setAddrList(List<Address> addrList) {
        this.addrList = addrList;
    }
}
