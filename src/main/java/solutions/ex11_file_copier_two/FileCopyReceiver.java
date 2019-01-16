package solutions.ex11_file_copier_two;

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
            String subject = "ex11";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4222";
            }

            Options options = new Options.Builder().server(serverURL).build();
            Connection nc = Nats.connect(options);
            Subscription sub = nc.subscribe(subject);
            String[] words = null;
            int count = 0;

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

                // fail sometimes
                if (Math.random() < 0.05) {
                    System.out.printf("Skipping %d\n", index);
                    continue;
                }

                words[index] = word;
                count++;

                // send the ack
                nc.publish(msg.getReplyTo(), null, null);

                if (count == max) {
                    break;
                }
            }

            System.out.println("Received file...");
            System.out.println();
            for (int i=0;i<words.length;i++) {
                if (i != 0) {
                    System.out.print(" ");
                }
                System.out.print(words[i]);
            }
            System.out.println();
            nc.flush(Duration.ZERO); // get all the acks out there
            nc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}