package solutions.ex08_kv_store;

import java.time.Duration;
import java.util.HashMap;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.Subscription;

public class KVServer {
    public static void main(String args[]) {
        try {
            String subject = "ex08.*";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4222";
            }

            Options options = new Options.Builder().server(serverURL).build();
            Connection nc = Nats.connect(options);
            HashMap<String,byte[]> database = new HashMap<>();

            Subscription sub = nc.subscribe(subject);

            // Single threaded for simplicity
            while (true) {
                Message msg = sub.nextMessage(Duration.ZERO);

                if (msg == null) {
                    System.out.println("Got empty message");
                    continue;
                }

                String[] split = msg.getSubject().split("\\.");
                
                if (split.length != 2) {
                    System.out.printf("Got subject with wrong number of tokens %s (%d)\n", msg.getSubject(), split.length);
                    continue;
                }

                String key = split[1];
                String replyTo = msg.getReplyTo();
                byte[] value = msg.getData();

                // Check for set/delete
                if (replyTo == null || replyTo.length()==0) {
                    if (value == null || value.length == 0) {
                        System.out.printf("Deleting %s\n", key);
                        database.remove(key);
                    } else {
                        System.out.printf("Setting %s\n", key);
                        database.put(key, value);
                    }
                    continue;
                }

                if (value != null && value.length > 0) {
                    System.out.printf("Received get request with data, ignoring, %s\n", msg.getSubject());
                    continue;
                }

                System.out.printf("Returning %s\n", key);
                value = database.get(key);
                nc.publish(replyTo, null, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}