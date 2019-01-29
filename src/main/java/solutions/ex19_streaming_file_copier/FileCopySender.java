package solutions.ex19_streaming_file_copier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.nats.streaming.NatsStreaming;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;

public class FileCopySender {
    public static void main(String args[]) {
        try {
            String channel = "ex19";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4223";
            }

            String filePath = args[0];
            byte[] encoded = Files.readAllBytes(Paths.get(filePath));
            String data = new String(encoded, StandardCharsets.UTF_8);
            String[] words = data.split(" ");

            Options opts = new Options.Builder().natsUrl(serverURL).build();
            try (StreamingConnection sc = NatsStreaming.connect("nats_course", "ex19", opts)) {
                for (int i=0;i<words.length;i++) {
                    sc.publish(channel, String.format("%d|%d|%s", i,words.length, words[i]).getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                throw (e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}