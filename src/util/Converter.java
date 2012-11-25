package util;
/**
 * A converter is required by the BPlusTree to provide a layer of abstraction
 * between the memory representation and the disk representation of these values.
 * @author xclite
 *
 * @param <KeyType>
 * @param <RecordType>
 */
public interface Converter<KeyType, RecordType>
{
    /**
     * A converter provides a method of translating a byte array to the
     * specified type.
     * @param b
     * @return the key represented by the given byte array
     */
    public KeyType bytesToKey(byte[] b);
    
    /**
     * A method for converting the key to an array of bytes.
     * @param key
     * @return an array of bytes that represents the key.  The array will be getKeyLength() bytes long.
     */
    public byte[] keyToBytes(KeyType key);
    
    /**
     * Converts a byte array to the RecordType
     * @param b
     * @return the record represented by the given byte array
     */
    public RecordType bytesToRecord(byte[] b);
    
    /**
     * A method for converting the record to an array of bytes.
     * @param record
     * @return an array of bytes that represents the record.  The array will be getRecordLength() bytes long.
     */
    public byte[] recordToBytes(RecordType record);

    /**
     * Getter for the key's guaranteed length.
     * @return the length of the key in bytes
     */
    public int getKeyLength();

    /**
     * Getter for the record's guaranteed length.
     * @return the length of a record in bytes
     */
    public int getRecordLength();
}
