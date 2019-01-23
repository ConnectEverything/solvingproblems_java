package solutions.ex13_tls;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.time.Duration;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import io.nats.client.Connection;
import io.nats.client.ConnectionListener;
import io.nats.client.Consumer;
import io.nats.client.ErrorListener;
import io.nats.client.Nats;
import io.nats.client.Options;

class SimpleConnectionListener implements ConnectionListener {
    public void connectionEvent(Connection natsConnection, Events event) {
        System.out.println("Event - "+event);

        if (event == Events.DISCOVERED_SERVERS) {
            for (String server : natsConnection.getServers()) {
                System.out.printf("\t%s\n", server);
            }
        } else if (event == Events.CONNECTED || event == Events.RECONNECTED) {
            System.out.printf("\t%s\n", natsConnection.getConnectedUrl());
        } else if (event == Events.CLOSED) {
            System.exit(0);
        }
    }
}

class SimpleErrorListener implements ErrorListener {
    public void errorOccurred(Connection conn, String error)
    {
        System.out.println("Error - "+error);
    }

    public void exceptionOccurred(Connection conn, Exception exp) {
        System.out.println("Exception - "+exp.getLocalizedMessage());
    }

    public void slowConsumerDetected(Connection conn, Consumer consumer) {
        System.out.println("Slow consumer");
    }
}

public class Connecting {
    public static String KEYSTORE_PATH = "resources/tls/certs/keystore.jks";
    public static String TRUSTSTORE_PATH = "resources/tls/certs/cacerts";
    public static String STORE_PASSWORD = "password";
    public static String KEY_PASSWORD = "password";
    public static String ALGORITHM = "SunX509";

    public static KeyStore loadKeystore(String path) throws Exception {
        KeyStore store = KeyStore.getInstance("JKS");
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(path));

        try {
            store.load(in, STORE_PASSWORD.toCharArray());
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return store;
    }

    public static void setKeystoreSystemParameters() {
        System.setProperty("javax.net.ssl.keyStore",KEYSTORE_PATH);
        System.setProperty("javax.net.ssl.keyStorePassword",KEY_PASSWORD);
        System.setProperty("javax.net.ssl.trustStore",TRUSTSTORE_PATH);
        System.setProperty("javax.net.ssl.trustStorePassword",STORE_PASSWORD);
    }

    public static KeyManager[] createTestKeyManagers() throws Exception {
        KeyStore store = loadKeystore(KEYSTORE_PATH);
        KeyManagerFactory factory = KeyManagerFactory.getInstance(ALGORITHM);
        factory.init(store, KEY_PASSWORD.toCharArray());
        return factory.getKeyManagers();
    }

    public static TrustManager[] createTestTrustManagers() throws Exception {
        KeyStore store = loadKeystore(TRUSTSTORE_PATH);
        TrustManagerFactory factory = TrustManagerFactory.getInstance(ALGORITHM);
        factory.init(store);
        return factory.getTrustManagers();
    }

    public static SSLContext createTestSSLContext() throws Exception {
        SSLContext ctx = SSLContext.getInstance(Options.DEFAULT_SSL_PROTOCOL);
        ctx.init(createTestKeyManagers(), createTestTrustManagers(), new SecureRandom());
        return ctx;
    }

    public static void main(String args[]) {
        try {
            String serverURL = "tls://localhost:4443";
            SSLContext ctx = createTestSSLContext();
            Options options = new Options.Builder().
                                        server(serverURL).
                                        connectionTimeout(Duration.ofSeconds(10)). // Set timeout
                                        connectionListener(new SimpleConnectionListener()).
                                        errorListener(new SimpleErrorListener()).
                                        maxReconnects(0).
                                        sslContext(ctx).
                                        userInfo("fancy", "pants").
                                        build();
            Connection nc = Nats.connect(options);

            Thread.sleep(5 * 1000); // sleep 5 seconds to capture events

            nc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}