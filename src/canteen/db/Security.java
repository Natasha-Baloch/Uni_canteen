package canteen.db;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Password hashing. The database stores only the SHA-256 hash (lower-case hex). */
public class Security {
    public static String sha256(String text) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
