package de.zalando.sprocwrapper.sharding;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.List;

/**
 * @author  jmussler
 */
public class VirtualShardKeyFromStringUsingMd5 extends VirtualShardKeyStrategy {
    @Override
    public int getShardId(final Object[] objs) {
        if (objs == null || objs.length == 0) {
            return 0;
        }

        String input = null;

        if (objs[0] == null) {
            return 0;
        }

        if (objs[0] instanceof List) {
            List<String> stringList = (List<String>) objs[0];
            if (stringList.isEmpty()) {
                return 0;
            }

            input = stringList.get(0);
        } else {
            input = (String) objs[0];
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException("Unable to use md5 algorithm", nsae);
        }

        final byte[] md5 = digest.digest(input.getBytes());
        return (md5[15] & 0xff) + ((md5[14] & 0xff) << 8) + ((md5[13] & 0xff) << 16);

    }
}
