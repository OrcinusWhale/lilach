package il.cshaifasweng.OCSFMediatorExample.client;

import java.util.List;
import java.util.HashMap;
import java.io.IOException;

import org.greenrobot.eventbus.EventBus;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Item;

public class SimpleClient extends AbstractClient {

    private static SimpleClient client = null;
    private static String host = "localhost";

    private SimpleClient(String host, int port) {
        super(host, port);
    }

    @Override
    protected void handleMessageFromServer(Object msg) {
        System.out.println("Client received message from server: " + msg.getClass().getSimpleName());
        EventBus.getDefault().post(msg);
    }

    public static SimpleClient getClient() {
        if (client == null) {
            client = new SimpleClient(host, 3000);
        }
        return client;
    }

    public static void setHostIp(String host) {
        SimpleClient.host = host;
    }
}