package solutions.ex04_idempotence;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.Subscription;

public class Idempotence {
    public static void main(String args[]) {
        try {
            String subject = "ex04";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4222";
            }

            Options options = new Options.Builder().
                                        server(serverURL).
                                        build();
            Connection nc = Nats.connect(options);
            Timer timer = new Timer();
            AtomicInteger addOnly = new AtomicInteger();
            AtomicInteger assign = new AtomicInteger();
            int rand = (int) (Math.random() * 10);
            String notification = String.format("%d", rand);
            byte notificationBytes[] = notification.getBytes(StandardCharsets.UTF_8);

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    nc.publish(subject, null, notificationBytes);
                }
            }, 1000L, 5*1000L); // wait a second, then fire every 5 seconds

            Subscription sub = nc.subscribe(subject);
            while (true) {
                Message msg = sub.nextMessage(Duration.ZERO);
                String  received = new String(msg.getData(), StandardCharsets.UTF_8);
                int value = Integer.parseInt(received);

                assign.set(value);
                addOnly.addAndGet(value);

                System.out.printf("Assign Value: %d\n", assign.get());
                System.out.printf("Append Value: %d\n", addOnly.get());
                System.out.println();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}