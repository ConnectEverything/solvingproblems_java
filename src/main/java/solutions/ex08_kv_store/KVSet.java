package solutions.ex08_kv_store;

import java.nio.charset.StandardCharsets;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;

public class KVSet {
    public static void main(String args[]) {
        try {
            String subjectPrefix = "ex08";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4222";
            }

            Options options = new Options.Builder().
                                        server(serverURL).
                                        build();
            Connection nc = Nats.connect(options);

            String key = args[0];
            String value = args[1];

            nc.publish(String.format("%s.%s", subjectPrefix, key), value.getBytes(StandardCharsets.UTF_8));

            System.out.printf("Set %s to \"%s\"\n", key, value);
            nc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}