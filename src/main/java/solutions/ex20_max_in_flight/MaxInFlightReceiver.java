package solutions.ex20_max_in_flight;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import io.nats.streaming.NatsStreaming;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.SubscriptionOptions;

public class MaxInFlightReceiver {
    public static void main(String args[]) {
        try {
            String channel = "ex20";
            String serverURL = System.getenv("SP_NATS_SERVER");
            int maxInFlight = 1;

            if (serverURL == null) {
                serverURL = "nats://localhost:4223";
            }

            if (args.length > 0) {
                maxInFlight = Integer.parseInt(args[0]);
            }

            CompletableFuture<Boolean> waitForIt = new CompletableFuture<>();
            AtomicInteger count = new AtomicInteger();

            Options opts = new Options.Builder().natsUrl(serverURL).build();
            try (StreamingConnection sc = NatsStreaming.connect("nats_course", "ex19", opts)) {
                sc.subscribe(channel, (msg) -> {
                    try {
                        String msgString = new String(msg.getData(), StandardCharsets.UTF_8);
                        System.out.println(msgString);

                        if (count.get() % 2 == 0) {
                            msg.ack();
                        }

                        count.incrementAndGet();
                    } catch (Exception exp) {
                        exp.printStackTrace();
                    }
                },new SubscriptionOptions.Builder().
                                        deliverAllAvailable().
                                        maxInFlight(maxInFlight).
                                        manualAcks().
                                        ackWait(Duration.ofSeconds(1)).
                                        build());

                waitForIt.get();
            } catch (Exception e) {
                throw (e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}