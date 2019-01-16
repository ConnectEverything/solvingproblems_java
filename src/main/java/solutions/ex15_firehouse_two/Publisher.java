package solutions.ex15_firehouse_two;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;

public class Publisher {
    public static void main(String args[]) {
        try {
            String subject = "ex15";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4222";
            }

            Options options = new Options.Builder().
                                        server(serverURL).
                                        reconnectBufferSize(1024).
                                        reconnectWait(Duration.ofSeconds(2)).
                                        maxReconnects(-1).
                                        build();
            Connection nc = Nats.connect(options);
            long counter = 0;

            System.out.print("Sending ");
            while (true) {
                try {
                    counter++;
                    if (counter % 1_000 == 0) {
                        System.out.print(".");
                    }
                    nc.publish(subject, null, String.format("%d", counter).getBytes(StandardCharsets.UTF_8));
                    Thread.sleep(1);
                } catch (Exception ex) {
                    System.out.println();
                    System.out.printf("Exception - %s\n", ex.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}