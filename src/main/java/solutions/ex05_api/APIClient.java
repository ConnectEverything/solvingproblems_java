package solutions.ex05_api;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;

public class APIClient {
    public static void main(String args[]) {
        try {
            String subject = "ex05";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4222";
            }

            Options options = new Options.Builder().
                                        server(serverURL).
                                        build();
            Connection nc = Nats.connect(options);

            Message msg = nc.request(subject, null, Duration.ofSeconds(10));
            String response = new String(msg.getData(), StandardCharsets.UTF_8);
            System.out.println(response);
            nc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}