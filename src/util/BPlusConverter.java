package util;


public class BPlusConverter implements Converter<String, String> {
    @Override
    public String bytesToKey( byte[] b )
    {
        return new String(b).trim();
    }

    @Override
    public String bytesToRecord( byte[] b )
    {
        return new String(b).trim();
    }
    
    @Override
    public byte[] keyToBytes(String key)
    {
        int lengthDiff = getRecordLength() - key.length();
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
    public byte[] recordToBytes(String record)
    {
        int lengthDiff = getKeyLength() - record.length();
        String byteRecord = record;
        if (lengthDiff < 0)
            throw new IllegalArgumentException("Record value: " + record + "is too long!");
        else if (lengthDiff > 0)
        {
            
            for (int i = 0; i <= lengthDiff; i++)
            {
                byteRecord += " ";
            }
        }            
        return byteRecord.getBytes();
    }
    @Override
    public int getKeyLength()
    {
        return 100;
    }

    @Override
    public int getRecordLength()
    {
        return 100;
    }
}
