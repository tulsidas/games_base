package common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Randomin {
    private static SecureRandom r = new SecureRandom();

    private Randomin() {
    }

    public static void reseed(String s) {
        if (s != null && s.length() > 0) {
            try {
                byte[] textBytes = s.getBytes();
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(textBytes);
                r.setSeed(md.digest());
            }
            catch (NoSuchAlgorithmException e) {
            }
        }
    }
}

// TODO usar