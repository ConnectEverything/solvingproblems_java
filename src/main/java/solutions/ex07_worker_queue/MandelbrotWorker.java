package solutions.ex07_worker_queue;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.Subscription;

public class MandelbrotWorker {
    public static void main(String args[]) {
        try {
            String workSubject = "ex07_work";
            String workQueue = "ex07_queue";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4222";
            }

            Options options = new Options.Builder().server(serverURL).build();
            Connection nc = Nats.connect(options);
            int max = 255;
            double width = 600;
            double height = 400;
            int count = 0;

            Subscription sub = nc.subscribe(workSubject, workQueue);

            while (true) {
                Message msg = sub.nextMessage(Duration.ZERO);

                if (msg == null || msg.getData() == null) {
                    System.out.println("No work, exiting...");
                    break;
                }

                String strValue = new String(msg.getData(), StandardCharsets.UTF_8);
                String parts[] = strValue.split(",");
                double col = Double.parseDouble(parts[0]);
                double row = Double.parseDouble(parts[1]);
                double x0 = (col - width / 2.0) * 4.0 / width;
                double y0 = (row - height / 2.0) * 4.0 / height;

                double x = 0, y = 0;
                int iterations = 0;
                while ((x * x + y * y) < 4 && (iterations < max)) {
                    double temp = (x * x - y * y) + x0;
                    y = (2 * x * y) + y0;
                    x = temp;
                    iterations++;
                }

                String response = String.format("%d,%d,%d", (int)col, (int)row, iterations);
                nc.publish(msg.getReplyTo(), null, response.getBytes(StandardCharsets.UTF_8));

                count++;

                if (count % 1000 == 0) {
                    System.out.print(".");
                }
            }
            System.out.println();
            nc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}