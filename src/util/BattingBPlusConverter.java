package util;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
/**
 * This is the converter utility for the BattingBPlusRecord key and record types.
 * @author xclite
 *
 */
public class BattingBPlusConverter implements Converter<String, Long>
{

    @Override
    public String bytesToKey( byte[] b )
    {
        return new String(b).trim();
    }

    @Override
    public Long bytesToRecord( byte[] b )
    {
        ByteBuffer buffer = ByteBuffer.wrap(b);
        return buffer.getLong();
    }

    @Override
    public byte[] keyToBytes(String key)
    {
        int lengthDiff = 10 - key.length();
        String byteKey = key;
        if (lengthDiff < 0)
            throw new IllegalArgumentException("Key value: " + key + "is too long!");
        else if (lengthDiff > 0)
        {
            
            for (int i = 0; i <= lengthDiff; i++)
            {
                byteKey += " ";
            }
        }            
        return byteKey.getBytes();
    }


    @Override
    public byte[] recordToBytes(Long record)
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        LongBuffer longBuffer = byteBuffer.asLongBuffer();
        longBuffer.put(record);
        return byteBuffer.array();
    }

    @Override
    public int getKeyLength()
    {
        return 10;
    }

    @Override
    public int getRecordLength()
    {
        return 8;
    }
}