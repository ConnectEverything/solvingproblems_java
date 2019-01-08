package solutions.ex06_worker_pull;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;

public class MandelbrotPullServer {
    public static void main(String args[]) {
        try {
            String workSubject = "ex06_work";
            String completeSubject = "ex06_complete";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4222";
            }

            Options options = new Options.Builder().
                                        server(serverURL).
                                        build();
            Connection nc = Nats.connect(options);

            int width = 1920;
            int height = 1080;
            AtomicInteger remaining = new AtomicInteger(width * height);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            ConcurrentLinkedQueue<String> workQueue = new ConcurrentLinkedQueue<>();
            ConcurrentLinkedQueue<String> outForWorkQueue = new ConcurrentLinkedQueue<>();
            Random random = new Random();
            Color palette[] = new Color[256];
            for (int i=1; i<256; i++) {
                palette[i] = new Color(random.nextInt(256),random.nextInt(256),random.nextInt(256));
            }

            // Add all the work
            for (int row=0;row<height;row++) {
                for (int col=0;col<width;col++) {
                    String work = String.format("%d,%d", col, row); // x,y
                    workQueue.add(work);
                }
            }

            Dispatcher d = nc.createDispatcher((msg)-> {
                String work = workQueue.poll();
                if (work == null) {
                    work = outForWorkQueue.poll(); // try to get an old one that didn't come in yet
                } else {
                    outForWorkQueue.add(work);
                }
                nc.publish(msg.getReplyTo(), null, work.getBytes(StandardCharsets.UTF_8));
            });
            d.subscribe(workSubject);

            Dispatcher d2 = nc.createDispatcher((msg)-> {
                if (remaining.get() == 0) {
                    return;
                }

                String strValue = new String(msg.getData(), StandardCharsets.UTF_8);
                String parts[] = strValue.split(",");
                int col = Integer.parseInt(parts[0]);
                int row = Integer.parseInt(parts[1]);
                int iterations = Integer.parseInt(parts[2]);
                Color color = palette[iterations];

                image.setRGB(col, row, color.getRGB());

                if (remaining.get()%100_000 == 0) {
                    System.out.print(".");
                }

                String work = String.format("%d,%d", col, row);
                outForWorkQueue.remove(work);
                if (remaining.decrementAndGet() == 0) {
                    System.out.println(" done");
                    try {
                        ImageIO.write(image, "png", new File("mandelbrot.png"));
                    } catch(Exception e){
                        e.printStackTrace();
                    } finally {
                        nc.close();
                        System.exit(0);
                    }
                }
            });
            d2.subscribe(completeSubject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}