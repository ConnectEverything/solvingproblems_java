# Solving Problems using NATS (Java Edition)

This repository holds the Java templates/solutions for the _Solving Problems using NATS_ courseware.

This project depends on Gradle, the gradlew executable can be used to get the latest version.

The solutions are in `main/java/solutions`. Your version of the exercise code should go in `main/java/exercises`.

To compile, run `./gradlew fatjar`. This will build a single jar with all of the code and dependencies at
`build/libs/solvingproblems_java-1.0-fat.jar`. To run a solution or your own version, you simply need to include 
this jar in the class path. For example:

```bash
java -cp build/libs/solvingproblems_java-1.0-fat.jar solutions.ex01_connecting.Connecting
```

## Exercise Hints/Helpers

### General

* The Java Doc for the NATS client library is available at [http://javadoc.io/doc/io.nats/jnats/2.4.1](http://javadoc.io/doc/io.nats/jnats/2.4.1)
* The Java doc for NATS streaming is available at [http://javadoc.io/doc/io.nats/java-nats-streaming/2.1.3](http://javadoc.io/doc/io.nats/java-nats-streaming/2.1.3)
* Most of the exercises can be implemented by 1 or two classes with a main method
* You can create a UTF-8 string in Java from bytes using `new String(theBytes, StandardCharsets.UTF_8)`
* You can get UTF-8 bytes from a string using `theString.getBytes(StandardCharsets.UTF_8)`
* `Math.random()` returns a random float from 0 to 1
* The solutions allow you to put the server URL in the environment

```java
String serverURL = System.getenv("SP_NATS_SERVER");

if (serverURL == null) {
    serverURL = "nats://localhost:4222";
}
```

### Exercise 1 - Connecting

* Check out the Java doc for ConnectionListener and ErrorListener

### Exercise 3 - Notifications

* You can create a timer in Java with:

```java
Timer timer = new Timer();

timer.schedule(new TimerTask() {
    @Override
    public void run() {
        // Your code here
    }
}, 1000L, 5*1000L);
```

* The second argument for schedule is an initial delay, the third is the repeat interval

* You can loop over a subscription to get all the messages using:

```java
while (true) {
    Message msg = sub.nextMessage(Duration.ZERO);
    // Your code here
}
```

* Duration.ZERO means wait forever, and can be dangerous in production code

### Exercise 5 & 6 - Mandelbrot

* Use a width of 800 and height of 600 to get a reasonable amount of work but not too much
* Create an image with `BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);`
* You can create RGB Colors with `new Color(255,255,255);`
* Set a pixel with `image.setRGB(col, row, color.getRGB());`
* Save the image to PNG with `ImageIO.write(image, "png", new File("mandelbrot.png"));`
* Each pixel in the Mandelbrot set can be calculated using, with a max iterations of 255:

```java
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
```

### Exercise 10 - File Copier

* You can read a full file using:

```java
byte[] encoded = Files.readAllBytes(Paths.get(filePath));
String data = new String(encoded, StandardCharsets.UTF_8);
String[] words = data.split(" ");
```

### Exercise 15 - Word List

* You can read a file line by line using:

```java
try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
{
    stream.forEach(s -> working.add(s));
}
catch (IOException e)
{
    e.printStackTrace();
    System.exit(0);
}
```


Exercise 19 & 20 - Streaming

* You may want to use completable futures `CompletableFuture<Boolean> waitForIt = new CompletableFuture<>();` to block program exit