package solutions.ex07_worker_queue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;

public class MandelbrotBuilder {
    public static void main(String args[]) {
        try {
            String workSubject = "ex07_work";
            String serverURL = System.getenv("SP_NATS_SERVER");

            if (serverURL == null) {
                serverURL = "nats://localhost:4222";
            }

            Options options = new Options.Builder().
                                        server(serverURL).
                                        build();
            Connection nc = Nats.connect(options);

            int width = 600;
            int height = 400;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Random random = new Random();
            Color palette[] = new Color[256];
            for (int i=1; i<256; i++) {
                palette[i] = new Color(random.nextInt(256),random.nextInt(256),random.nextInt(256));
            }

            for (int row=0;row<height;row++) {
                for (int col=0;col<width;col++) {
                    String work = String.format("%d,%d", col, row); // x,y

                    try {
                        Message msg = nc.request(workSubject, work.getBytes(StandardCharsets.UTF_8), Duration.ofSeconds(10));

                        if (msg == null) {
                            System.out.print("x");
                            col--;
                            continue;
                        }

                        String strValue = new String(msg.getData(), StandardCharsets.UTF_8);
                        String parts[] = strValue.split(",");
                        int iterations = Integer.parseInt(parts[2]);
                        Color color = palette[iterations];
        
                        image.setRGB(col, row, color.getRGB());
                    } catch (InterruptedException e) {
                        col--;
                        continue;
                    }
                }
                System.out.print(".");
            }
            System.out.println("");

            try {
                System.out.println("Done.");
                ImageIO.write(image, "png", new File("mini_mandelbrot.png"));
            } catch(Exception e){
                e.printStackTrace();
            } finally {
                nc.close();
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}