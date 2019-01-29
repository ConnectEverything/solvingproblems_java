package solutions.ex15_wordlist;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;

public class AltWordServer {
    public static void main(String args[]) {
        try {
            final String ldap = (args.length > 0) ? args[0] : "solution";
            String matchSubject = "match";
            String prefixSubject = "prefix";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4222";
            }

            Options options = new Options.Builder().
                                        server(serverURL).
                                        build();
            Connection nc = Nats.connect(options);
            ArrayList<String> working = new ArrayList<>();

            try (Stream<String> stream = Files.lines( Paths.get("resources/words_alpha.txt"), StandardCharsets.UTF_8))
            {
                stream.forEach(s -> working.add(s));
            }
            catch (IOException e)
            {
                e.printStackTrace();
                System.exit(0);
            }

            Collections.reverse(working);
            final String[] wordList = working.toArray(new String[0]);

            // Handle Match requests
            Dispatcher d = nc.createDispatcher((msg)-> {
                String toMatch = new String(msg.getData(), StandardCharsets.UTF_8);

                ArrayList<String> response = new ArrayList<>();
                response.add(ldap);
                for (String word : wordList) {

                    if (word.contains(toMatch)) {
                        response.add(word);
                    }

                    if (response.size() > 10) {
                        break;
                    }
                }

                System.out.printf("Returning %d matches for %s\n", response.size()-1, toMatch);
                nc.publish(msg.getReplyTo(), null, String.join("\n",response).getBytes(StandardCharsets.UTF_8));
            });
            d.subscribe(matchSubject);

            // Handle Prefix requests
            Dispatcher d2 = nc.createDispatcher((msg)-> {
                String toMatch = new String(msg.getData(), StandardCharsets.UTF_8);

                ArrayList<String> response = new ArrayList<>();
                response.add(ldap);
                for (String word : wordList) {

                    if (word.startsWith(toMatch)) {
                        response.add(word);
                    }

                    if (response.size() > 10) {
                        break;
                    }
                }

                System.out.printf("Returning %d prefixes for %s\n", response.size()-1, toMatch);
                nc.publish(msg.getReplyTo(), null, String.join("\n",response).getBytes(StandardCharsets.UTF_8));
            });
            d2.subscribe(prefixSubject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}