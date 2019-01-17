
package solutions.ex18_autocomplete;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.Subscription;

public class Autocomplete {
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

            String replyTo = "solution.autosuggest";
            ArrayList<String> responses = new ArrayList<>();
            Subscription sub = nc.subscribe(replyTo);

            // Send the request
            nc.publish(subject, replyTo, input.getBytes(StandardCharsets.UTF_8));

            // Wait for a single response
            long max = 100 * 1_000_000; // 100ms in nanos
            long start = System.nanoTime();
            while (System.nanoTime() - start < max) {
                Message msg = sub.nextMessage(Duration.ofMillis(1));

                if (msg == null) {
                    continue;
                }

                responses.add(new String(msg.getData(), StandardCharsets.UTF_8));
            }
            sub.unsubscribe();

            if (responses.size() == 0) {
                System.out.println("No prefixes found");
                nc.close();
                System.exit(-1);
            }

            HashSet<String> suggestions = new HashSet<>();

            for (String response : responses) {
                String[] lines = response.split("\n");

                for (int i=1; i<lines.length; i++) {
                    suggestions.add(lines[i]);
                }
            }

            System.out.printf("Received suggestions from %d servers\n", responses.size());
            System.out.println();

            for (String s : suggestions) {
                System.out.println(s);
            }

            nc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}