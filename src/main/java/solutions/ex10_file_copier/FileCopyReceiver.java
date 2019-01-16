package solutions.ex10_file_copier;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.Subscription;

public class FileCopyReceiver {
    public static void main(String args[]) {
        try {
            String subject = "ex10";
            String replayPrefix = "ex10.replay";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4222";
            }

            Options options = new Options.Builder().server(serverURL).build();
            Connection nc = Nats.connect(options);
            Subscription sub = nc.subscribe(subject);
            String[] words = null;

            System.out.println("Listening for file contents...");
            while (true) {
                Message msg = sub.nextMessage(Duration.ZERO);

                if (msg == null) {
                    System.out.println("Got empty message");
                    continue;
                }

                String msgString = new String(msg.getData(), StandardCharsets.UTF_8);
                String[] split = msgString.split("\\|");

                int index = Integer.parseInt(split[0]);
                int max = Integer.parseInt(split[1]);
                String word = (split.length == 3) ? split[2] : "";

                if (words == null) {
                    words = new String[max];
                }

                words[index] = word;

                if (index == max-1) {
                    break;
                }
            }

            // assume we got at least 1 word
            System.out.println("Checking for missing sequence numbers...");
            for (int i=0;i<words.length;i++) {
                if (words[i] == null) {
                    
                    System.out.printf("\tRequesting %d\n", i);
                    Message replay = nc.request(String.format("%s.%d", replayPrefix, i), null, Duration.ofSeconds(1));

                    if (replay == null) {
                        System.out.printf("No response on replay, exiting...");
                        System.exit(-1);
                    }

                    String replayString = new String(replay.getData(), StandardCharsets.UTF_8);
                    String[] replaySplit = replayString.split("\\|");
                    int index = Integer.parseInt(replaySplit[0]);
                    String word = (replaySplit.length == 3) ? replaySplit[2] : "";

                    words[index] = word;
                }
            }

            // Tell the sender we are done
            nc.publish(String.format("%s.%d", replayPrefix, -1), null, null);

            System.out.println("Received file...");
            System.out.println();
            for (int i=0;i<words.length;i++) {
                if (i != 0) {
                    System.out.print(" ");
                }
                System.out.print(words[i]);
            }
            System.out.println();
            nc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}