package org.linuxmotion.asyncloaders.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AeSimpleSHA1 {

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String SHA1(byte[] stream, int length){


        MessageDigest md = null;

        try{
            md = MessageDigest.getInstance("SHA-1");
            md.update(stream, 0, length);
            byte[] sha1hash = md.digest();
            return convertToHex(sha1hash);

        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "Contact the developer";





    }


    public static String SHA1(String text)
    {
        MessageDigest md = null;

        try{
            md = MessageDigest.getInstance("SHA-1");
        }catch (NoSuchAlgorithmException e) {

        }

        try {
            md.update(text.getBytes("iso-8859-1"), 0, text.length());
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        byte[] sha1hash = md.digest();


        return convertToHex(sha1hash);
    }
}