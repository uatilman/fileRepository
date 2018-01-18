import org.apache.commons.codec.digest.DigestUtils;
import sun.misc.BASE64Encoder;
import sun.security.util.Password;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Main {
    public static void main(String[] args) throws NoSuchAlgorithmException {
//        testSHA256Variants();
        String data = "hello";

//        byte salt[] = Passwords.getNextSalt();
//        byte hash[] = Passwords.hash(data.toCharArray(), salt);
//        String hashStr = bytesToHex(hash);
//        String saltStr = bytesToHex(hash);


//        for (int i = 0; i < 5; i++) {
//            System.out.println(bytesToHex(Passwords.getNextSalt()));
//        }
//        System.out.println(hashStr);
//        System.out.println(Passwords.isExpectedPassword(data.toCharArray(), salt, hash));
    }

    private static void testSHA256Variants() throws NoSuchAlgorithmException {
        String text = "hello";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
//        digest.update();

        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
//        new Base64.Decoder().
        System.out.println(new String(hash));
        String encoded = Base64.getMimeEncoder().encodeToString(hash);
        String encoded1 = Base64.getEncoder().encodeToString(hash);
        System.out.println(new BASE64Encoder().encode(hash));
        System.out.println(encoded);
        System.out.println(encoded1);
        System.out.println(bytesToHex(hash));

        System.out.println(DigestUtils.sha256Hex(text));
    }


    private static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte b : bytes) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }
}
