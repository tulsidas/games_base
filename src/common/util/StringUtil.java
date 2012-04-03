package common.util;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.common.ByteBuffer;

public class StringUtil {
    private static final CharsetDecoder dec = Charset.forName("UTF-8")
            .newDecoder();

    private static final CharsetEncoder enc = Charset.forName("UTF-8")
            .newEncoder();

    public static String truncate(String str, int maxSize) {
        if (str.length() > maxSize) {
            return str.substring(0, maxSize - 3) + "...";
        }
        else {
            return str;
        }
    }

    public static void encode(ByteBuffer buff, String str) {
        try {
            buff.putPrefixedString(str, enc);
        }
        catch (CharacterCodingException e) {
            e.printStackTrace();
        }
    }

    public static String decode(ByteBuffer buff) {
        String username = "?";
        try {
            username = buff.getPrefixedString(dec);
        }
        catch (CharacterCodingException e) {
            e.printStackTrace();
        }

        return username;
    }
}
