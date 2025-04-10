import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.util.Base64;

public class ImportSecretKey {
    public static void main(String[] args) throws Exception {
        // === CONFIGURATION ===
        String base64Key = "MDEyMzQ1Njc4OWFiY2RlZg=="; // your base64-encoded key
        String keyAlg = "AES"; // key algorithm (AES, DES, etc.)
        String alias = "mysecret";
        String keyPassword = "keypass";
        String keystorePassword = "storepass";
        String keystorePath = "mykeystore.jceks";

        // === DECODE THE KEY ===
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        SecretKey secretKey = new SecretKeySpec(decodedKey, keyAlg);

        // === CREATE NEW KEYSTORE ===
        KeyStore ks = KeyStore.getInstance("JCEKS");
        ks.load(null, keystorePassword.toCharArray()); // new keystore

        // === SET ENTRY ===
        KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKey);
        KeyStore.ProtectionParameter protParam =
                new KeyStore.PasswordProtection(keyPassword.toCharArray());
        ks.setEntry(alias, entry, protParam);

        // === STORE TO DISK ===
        try (FileOutputStream fos = new FileOutputStream(keystorePath)) {
            ks.store(fos, keystorePassword.toCharArray());
        }

        System.out.println("Secret key imported into JCEKS keystore successfully.");
    }
}
