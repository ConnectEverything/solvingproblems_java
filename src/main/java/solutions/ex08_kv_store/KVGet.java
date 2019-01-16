package solutions.ex08_kv_store;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;

public class KVGet {
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

            Message msg = nc.request(String.format("%s.%s", subjectPrefix, key), null, Duration.ofSeconds(1));
            byte[] data = null;

            if (msg != null) {
                data = msg.getData();
            }
            
            if (data == null) {
                data = new byte[0];
            }

            String response = new String(data, StandardCharsets.UTF_8);

            System.out.printf("%s = \"%s\"\n", key, response);
            nc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}