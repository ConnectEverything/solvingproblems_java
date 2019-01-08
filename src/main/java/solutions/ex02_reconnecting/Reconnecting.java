package solutions.ex02_reconnecting;

import java.time.Duration;

import io.nats.client.Connection;
import io.nats.client.ConnectionListener;
import io.nats.client.Consumer;
import io.nats.client.ErrorListener;
import io.nats.client.Nats;
import io.nats.client.Options;

class SimpleConnectionListener implements ConnectionListener {
    public void connectionEvent(Connection natsConnection, Events event) {
        System.out.println("Event - "+event);

        if (event == Events.DISCOVERED_SERVERS) {
            for (String server : natsConnection.getServers()) {
                System.out.printf("\t%s\n", server);
            }
        } else if (event == Events.CONNECTED || event == Events.RECONNECTED) {
            System.out.printf("\t%s\n", natsConnection.getConnectedUrl());
        } else if (event == Events.CLOSED) {
            System.exit(0);
        }
    }
}

class SimpleErrorListener implements ErrorListener {
    public void errorOccurred(Connection conn, String error) {
        System.out.println("Error - "+error);
    }

    public void exceptionOccurred(Connection conn, Exception exp) {
        System.out.println("Exception - "+exp.getLocalizedMessage());
    }

    public void slowConsumerDetected(Connection conn, Consumer consumer) {
        System.out.println("Slow consumer");
    }
}

public class Reconnecting {
    public static void main(String args[]) {
        try {
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4222";
            }

            Options options = new Options.Builder().
                                        server(serverURL).
                                        connectionTimeout(Duration.ofSeconds(10)). // Set timeout
                                        connectionListener(new SimpleConnectionListener()).
                                        errorListener(new SimpleErrorListener()).
                                        reconnectWait(Duration.ofSeconds(5)).
                                        maxReconnects(5).
                                        build();
            Connection nc = Nats.connect(options);

            Thread.sleep(10 * 60 * 1000); // sleep 10 minutes to play with reconnect

            nc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}