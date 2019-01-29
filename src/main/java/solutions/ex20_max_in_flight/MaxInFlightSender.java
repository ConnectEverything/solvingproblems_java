package solutions.ex20_max_in_flight;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.nats.streaming.NatsStreaming;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;

public class MaxInFlightSender {
    public static void main(String args[]) {
        try {
            String channel = "ex20";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4223";
            }

            Options opts = new Options.Builder().natsUrl(serverURL).build();
            try (StreamingConnection sc = NatsStreaming.connect("nats_course", "ex20", opts)) {
                for (int i=0;i<1000;i++) {
                    sc.publish(channel, String.format("%d", i).getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                throw (e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}