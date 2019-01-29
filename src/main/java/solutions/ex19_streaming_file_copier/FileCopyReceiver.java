package solutions.ex19_streaming_file_copier;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.nats.streaming.NatsStreaming;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.SubscriptionOptions;

public class FileCopyReceiver {
    public static void main(String args[]) {
        try {
            String channel = "ex19";
            String serverURL = System.getenv("SP_NATS_SERVER");
            int startAt = 1;

            if (serverURL == null) {
                serverURL = "nats://localhost:4223";
            }

            if (args.length > 0) {
                startAt = Integer.parseInt(args[0]);
            }

            CompletableFuture<Boolean> waitForIt = new CompletableFuture<>();
            ConcurrentLinkedQueue<String> words = new ConcurrentLinkedQueue<>();

            Options opts = new Options.Builder().natsUrl(serverURL).build();
            try (StreamingConnection sc = NatsStreaming.connect("nats_course", "ex19", opts)) {

                System.out.println("Listening for file contents...");
                sc.subscribe(channel, (msg) -> {
                    try {
                        String msgString = new String(msg.getData(), StandardCharsets.UTF_8);
                        String[] split = msgString.split("\\|");

                        int index = Integer.parseInt(split[0]);
                        int max = Integer.parseInt(split[1]);
                        String word = (split.length == 3) ? split[2] : "";

                        words.add(word);

                        if (index == max-1) {
                            waitForIt.complete(true);
                        }
                    } catch (Exception exp) {
                        exp.printStackTrace();
                    }
                },new SubscriptionOptions.Builder().startAtSequence(startAt).build());

                waitForIt.get();
            } catch (Exception e) {
                throw (e);
            }

            System.out.println("Received file...");
            System.out.println();

            boolean first = true;
            for (String w : words) {
                if (!first) {
                    System.out.print(" ");
                }
                System.out.print(w);
                first = false;
            }
            System.out.println();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}