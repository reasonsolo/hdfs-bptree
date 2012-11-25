package util;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

/**
 * This class represents a record that can be stored within a
 * B+ Tree.  The methods this interface enforces help output and
 * read.  The value for any B+ record is always a file offset.
 * @author xclite
 * @param <KeyType> Type of key this record uses.
 *
 */
public abstract class BPlusRecord<KeyType extends Comparable <? super KeyType>, ValueType>
{
    private KeyType key;
    private ValueType value;
    
    
    
    /**
     * A constructor that allows variable key length
     * @param key
     * @param value
     * @param keyLength
     */
    public BPlusRecord(KeyType key, ValueType value)
    {
        this.setKey( key );
        this.setValue( value );
    }

    public void setKey( KeyType key )
    {
        this.key = key;
    }

    public KeyType getKey()
    {
        return key;
    }

    public void setValue(ValueType value)
    {
        this.value = value;
    }

    public ValueType getValue()
    {
        return value;
    }
}