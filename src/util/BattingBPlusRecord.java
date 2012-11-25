package util;

import javax.naming.OperationNotSupportedException;

/**
 * This class represents a Batting Record to be stored within the B+
 * tree.
 * @author xclite
 *
 */
public class BattingBPlusRecord extends BPlusRecord<String, Long>
{
    
    /**
     * A general constructor
     * @param key
     * @param value
     */
    public BattingBPlusRecord(String key, Long value)
    {
        super(key, value);
    }
    
}