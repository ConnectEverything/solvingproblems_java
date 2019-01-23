package solutions.ex17_autofill;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.Subscription;

public class Autofill {
    public static void main(String args[]) {
        try {
            String subject = "prefix";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4222";
            }

            Options options = new Options.Builder().
                                        server(serverURL).
                                        build();
            Connection nc = Nats.connect(options);

            String input = args[0];

            String replyTo = "solution.autofill";
            String response = null;
            Subscription sub = nc.subscribe(replyTo);

            // Send the request
            nc.publish(subject, replyTo, input.getBytes(StandardCharsets.UTF_8));

            // Wait for a single response
            while (true) {
                Message msg = sub.nextMessage(Duration.ofSeconds(1));

                if (msg == null) {
                    break;
                }

                response =  new String(msg.getData(), StandardCharsets.UTF_8);
                break;
            }
            sub.unsubscribe();

            if (response == null) {
                System.out.println("No prefixes found");
                nc.close();
                System.exit(-1);
            }

            String[] lines = response.split("\n");

            System.out.printf("Prefixes from %s\n", lines[0]);

            for (int i=1; i<lines.length; i++) {
                System.out.println(lines[i]);
            }

            nc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}