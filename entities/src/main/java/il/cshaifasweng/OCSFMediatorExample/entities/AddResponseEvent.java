package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class AddResponseEvent implements Serializable {
    private String response;

    public AddResponseEvent(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}
