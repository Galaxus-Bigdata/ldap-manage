import java.io.FileInputStream;
import java.security.KeyStore;

public class KeyStoreReader {
    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("mykeystore.jceks");
        KeyStore keystore = KeyStore.getInstance("JCEKS");
        keystore.load(fis, "keystorePassword".toCharArray());

        // Example: Get an alias's entry
        KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection("keyPassword".toCharArray());
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keystore.getEntry("aliasName", protParam);
        System.out.println(privateKeyEntry.getPrivateKey());
    }
}
