package trees;

import java.util.Arrays;
import util.BPlusRecord;
import util.ModifiedBinarySearcher;
/**
 * A leaf is the node in a B+ tree containing records.
 * Although we want them to be generic, most B+ trees are specialized.
 * As a result, the only thing generic about the tree is the key - 
 * the records are offsets in this usage.
 * @author xclite
 *
 * @param <KeyType>
 * @param <ValueType> 
 */
@SuppressWarnings("unchecked")
public class BPlusLeaf<KeyType extends Comparable<? super KeyType>, ValueType>
    extends BPlusNode<KeyType>
{
   
    private ValueType[] records;

    /** 
     * @param m
     */
    public BPlusLeaf(int m)
    {
        super ((KeyType[])new Comparable[m], 0, -1, -1);
        this.records = (ValueType[]) new Object[m];
        left = right = -1;
    }
    
    /**
     * Initializes leaf.
     * @param keys
     * @param numKeys
     * @param records
     * @param left
     * @param right
     */
    public BPlusLeaf(KeyType[] keys, int numKeys, ValueType[] records, long left, long right)
    {
        super(keys, numKeys, left, right);
        this.records = records;
    }
    

    /**
     * Deletes the key (and the associated record offset)
     * from this node.
     * First, we find the index of the key.
     * Make a temp array containing keys[0, indexToSkip - 1]
     * copy keys[indexToSkip + 1, keys.length - 1] over.
     * Do the same with the records.
     * @param keyToDelete 
     */
    public void delete(KeyType keyToDelete)
    {
        int indexToSkip = Arrays.binarySearch(keys, 0, numKeys, keyToDelete);
        if (indexToSkip >= 0)
        {
            KeyType[] tempKeys = (KeyType[]) new Comparable[keys.length];
            System.arraycopy(keys, 0, tempKeys, 0, indexToSkip);
            System.arraycopy(keys, indexToSkip + 1, tempKeys, indexToSkip, keys.length - indexToSkip - 1);
            keys = tempKeys;
            ValueType[] tempRecords = (ValueType[]) new Object[records.length];
            System.arraycopy(records, 0, tempRecords, 0, indexToSkip);
            System.arraycopy(records, indexToSkip + 1, tempRecords, indexToSkip, records.length - indexToSkip - 1);
            keys = tempKeys;
            records = tempRecords;
            --numKeys;
        }
    }
    
    /**
     * Inserts a key and the record offset into
     * the node.
     * First, we find the index where these items
     * need to be.
     * Then copy all elements up to that point into a temp array.
     * Shove the new key in, copy the rest of the elements.
     * use the same index and do the same with the records.
     * @param key 
     * @param record 
     */
    public void insert(KeyType key, ValueType record)
    {
        if (!isFull())
        {
            KeyType[] tempKeys = (KeyType[]) new Comparable[keys.length];
            ModifiedBinarySearcher<KeyType> searcher = new ModifiedBinarySearcher<KeyType>(keys);
            int indexToInsert = searcher.findIndexOfNextGreatest(key, 0, numKeys - 1) + 1;
            System.arraycopy(keys, 0, tempKeys, 0, indexToInsert);
            tempKeys[indexToInsert] = key;
            System.arraycopy(keys, indexToInsert, tempKeys, indexToInsert + 1, keys.length - indexToInsert - 1);
            keys = tempKeys;
            ValueType[] tempRecords = (ValueType[]) new Object[records.length];
            System.arraycopy(records, 0, tempRecords, 0, indexToInsert);
            tempRecords[indexToInsert] = record;
            System.arraycopy(records, indexToInsert, tempRecords, indexToInsert + 1, tempRecords.length - indexToInsert - 1);
            records = tempRecords;
            numKeys ++;
        }
    }

    @Override
    public boolean isLeaf()
    {
        return true;
    }

    /**
     * Searches for the given value.
     * @param key
     * @return the value for the key, or null if not found.
     */
    public ValueType search( KeyType key )
    {
        int foundIndex = Arrays.binarySearch(keys, 0, numKeys, key);
        return foundIndex < 0 ? null : records[foundIndex];
    }

    /**
     * 
     * @return array of records
     */
    public ValueType[] getRecords()
    {
        return records;
    }
    
    /**
     * borrows the rightmost record
     * @return the rightmost record
     */
    public ValueType borrowRecord()
    {
        return records[numKeys - 1];
    }
    
    public void doneBorrowing()
    {
        keys[numKeys - 1] = null;
        records[numKeys - 1] = null;
        --numKeys;
    }

    /**
     * Borrow the first record
     * @return the first record
     */
    public ValueType borrowFirstRecord()
    {
        return records[numKeys - 1];
    }

    public void doneBorrowingFirst()
    {
        delete(keys[0]);
    }
}