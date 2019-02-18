import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;

class PasswordHashGenerator {

    public static String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            return String.format("%064x", new BigInteger(1, digest));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}