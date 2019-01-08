package solutions.ex03_notifications;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.Subscription;

public class Notifications {
    public static void main(String args[]) {
        try {
            String subject = "ex03";
            String prefix = "solution";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4222";
            }

            Options options = new Options.Builder().
                                        server(serverURL).
                                        build();
            Connection nc = Nats.connect(options);

            Timer timer = new Timer();

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    int rand = (int) (Math.random() * 10_000);
                    String notification = String.format("%s.%d", prefix, rand);
                    byte notificationBytes[] = notification.getBytes(StandardCharsets.UTF_8);
                    nc.publish(subject, null, notificationBytes);
                }
            }, 1000L, 5*1000L); // wait a second, then fire every 5 seconds

            Subscription sub = nc.subscribe(subject);
            while (true) {
                Message msg = sub.nextMessage(Duration.ZERO);
                String notification = new String(msg.getData(), StandardCharsets.UTF_8);
                System.out.println(notification);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}