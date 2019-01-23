package solutions.ex14_firehouse;

import java.time.Duration;

import io.nats.client.Connection;
import io.nats.client.ConnectionListener;
import io.nats.client.Consumer;
import io.nats.client.ErrorListener;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.Subscription;

class SimpleConnectionListener implements ConnectionListener {
    public void connectionEvent(Connection natsConnection, Events event) {
        System.out.println();
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
    public void errorOccurred(Connection conn, String error)
    {
        System.out.println();
        System.out.println("Error - "+error);
    }

    public void exceptionOccurred(Connection conn, Exception exp) {
        System.out.println();
        System.out.println("Exception - "+exp.getLocalizedMessage());
    }

    public void slowConsumerDetected(Connection conn, Consumer consumer) {
        System.out.println();
        System.out.println("Slow consumer");
    }
}

public class Subscriber {
    public static void main(String args[]) {
        try {
            String subject = "ex14";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4222";
            }

            Options options = new Options.Builder().
                                        server(serverURL).
                                        connectionListener(new SimpleConnectionListener()).
                                        errorListener(new SimpleErrorListener()).
                                        reconnectWait(Duration.ofSeconds(2)).
                                        maxReconnects(-1).
                                        build();
            Connection nc = Nats.connect(options);

            Subscription sub = nc.subscribe(subject);
            sub.setPendingLimits(100, 1024*8);

            long count = 0;

            System.out.println();
            System.out.print("Listening ");
            while (true) {
                sub.nextMessage(Duration.ZERO);
                count++;

                if (count == 1_000) {
                    System.out.print(".");
                    count = 0;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}