package solutions.ex04_api;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.Subscription;

public class APIServer {
    public static void main(String args[]) {
        try {
            String subject = "ex04";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4222";
            }

            Options options = new Options.Builder().
                                        server(serverURL).
                                        build();
            Connection nc = Nats.connect(options);
            Subscription sub = nc.subscribe(subject);
            while (true) {
                Message msg = sub.nextMessage(Duration.ZERO);
                Date now = new Date();
                byte response[] = now.toString().getBytes(StandardCharsets.UTF_8);
                nc.publish(msg.getReplyTo(), null, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}