package solutions.ex12_file_copy_monitor;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.Subscription;

public class FileCopyMonitor {
    public static void main(String args[]) {
        try {
            String subject = ">";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4222";
            }

            Options options = new Options.Builder().server(serverURL).build();
            Connection nc = Nats.connect(options);
            Subscription sub = nc.subscribe(subject);

            System.out.println("Monitoring...");
            while (true) {
                Message msg = sub.nextMessage(Duration.ZERO);

                if (msg == null) {
                    System.out.println("<empty message>");
                    continue;
                }

                String msgString = new String(msg.getData(), StandardCharsets.UTF_8);
                System.out.printf("%s: %s\n", msg.getSubject(), msgString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}