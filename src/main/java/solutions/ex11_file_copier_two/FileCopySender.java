package solutions.ex11_file_copier_two;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;

public class FileCopySender {
    public static void main(String args[]) {
        try {
            String subject = "ex11";
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

            System.out.printf("Sending %d words\n", words.length);

            for (int i=0;i<words.length;i++) {
                byte[] wordToSend = String.format("%d|%d|%s", i,words.length, words[i]).getBytes(StandardCharsets.UTF_8);
                Message ack = nc.request(subject, wordToSend, Duration.ofMillis(50));

                if (ack == null) {
                    System.out.printf("Retrying %d\n", i);
                    i--; // repeat this one
                }
            }

            System.out.println("File Sent.");
            System.out.println();

            nc.close();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}