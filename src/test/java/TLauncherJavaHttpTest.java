import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.security.KeyStore;

public class TLauncherJavaHttpTest {
    public static String cacert_pass = System.getProperty("javax.net.ssl.trustStorePassword") != null
            ? System.getProperty("javax.net.ssl.trustStorePassword")
            : "changeit";

    public static SSLSocketFactory sslSocketFactory;

    public static void main(String[] args) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream in = TLauncherJavaHttpTest.class.getResourceAsStream("cacerts");

        try {
            keyStore.load(in, cacert_pass.toCharArray());
        } finally {
            assert in != null;
            in.close();
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, cacert_pass.toCharArray());
        KeyManager[] keyManagers = kmf.getKeyManagers();

        TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmfactory.init(keyStore);
        TrustManager[] trustManagers = tmfactory.getTrustManagers();

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, trustManagers, null);

        sslSocketFactory = sslContext.getSocketFactory();
        HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);


        connect("https://google.com");

        connect("https://yandex.ru");

        connect("https://gravitycraft.net");

        connect("https://cubixworld.net");

        connect("https://mcskill.net/");

        connect("https://excalibur-craft.ru/");

        connect("https://tlauncher.org/");

        connect("https://obvilion.ru/test");
    }

    public static int i = 1;

    public static void connect(String link) throws IOException {
        URL obj = new URL(link);

        try {
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("GET");

            InputStream is = connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream();

            System.out.println("Test " + i++ + " - PASS  [ " + obj.getHost() + " ] " + obj.getProtocol());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Test " + i++ + " - ERROR [ " + obj.getHost() + " ] " + obj.getProtocol());
        }
    }
}
