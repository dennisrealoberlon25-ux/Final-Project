
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtil {

    private static final int ITERATIONS = 100000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public static byte[] hashPassword(String password, byte[] salt)
            throws Exception {

        KeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        return factory.generateSecret(spec).getEncoded();
    }

    public static boolean verifyPassword(
            String password, byte[] salt, byte[] storedHash)
            throws Exception {

        byte[] newHash = hashPassword(password, salt);

        return MessageDigest.isEqual(newHash, storedHash);
    }
}

