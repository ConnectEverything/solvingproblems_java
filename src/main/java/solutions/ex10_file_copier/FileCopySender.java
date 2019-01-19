package solutions.ex10_file_copier;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;

public class FileCopySender {
    public static void main(String args[]) {
        try {
            String subject = "ex10";
            String replaySubject = "ex10.replay.*";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4222";
            }

            Options options = new Options.Builder().
                                        server(serverURL).
                                        build();
            Connection nc = Nats.connect(options);

            String filePath = args[0];
            byte[] encoded = Files.readAllBytes(Paths.get(filePath));
            String data = new String(encoded, StandardCharsets.UTF_8);
            String[] words = data.split(" ");

            Dispatcher d = nc.createDispatcher((msg)-> {
                
                String[] split = msg.getSubject().split("\\.");
                if (split.length != 3) {
                    System.out.printf("Got subject with wrong number of tokens %s (%d)\n", msg.getSubject(), split.length);
                    return;
                }

                // Fast and loose error checking for solution
                int index = Integer.parseInt(split[2]);

                if (index < 0 || index >= words.length) {
                    System.out.println("Copy complete, exiting...");
                    nc.close();
                    System.exit(0);
                }

                String response = String.format("%d|%d|%s", index, words.length, words[index]);
                System.out.printf("Replaying %d %d %s\n", index, words.length, words[index]);
                nc.publish(msg.getReplyTo(), null, response.getBytes(StandardCharsets.UTF_8));
            });
            d.subscribe(replaySubject);

            for (int i=0;i<words.length;i++) {
                if (Math.random() < 0.05) { // skip some words
                    System.out.printf("Skipping %d\n", i);
                    continue;
                }
                nc.publish(subject, null, String.format("%d|%d|%s", i,words.length, words[i]).getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}