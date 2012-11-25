package trees;

import java.util.Arrays;

import util.ModifiedBinarySearcher;
import util.Utility;

/**
 * Represents an internal node within the B+ tree.  Holds keys
 * and a reference to children.
 * @author xclite
 *
 * @param <KeyType>
 */
public class BPlusInternalNode<KeyType extends Comparable<? super KeyType>> extends BPlusNode<KeyType>
{
   
    private Long[] children;
    private int numChildren;
    
    /**
     * Constructor: sets instance data.
     * @param keys
     * @param numKeys
     * @param children
     * @param numChildren
     */
    public BPlusInternalNode(KeyType[] keys, int numKeys, 
        Long[] children, int numChildren)
    {
        this(keys, numKeys, children, numChildren, -1, -1);
        
    }
    
    /**
     * A constructor for when the left and right are known.
     * @param keys
     * @param numKeys
     * @param children
     * @param numChildren
     * @param left
     * @param right
     */
    public BPlusInternalNode(
        KeyType[] keys,
        int numKeys,
        Long[] children,
        int numChildren,
        long left,
        long right)
    {
        super(keys, numKeys, left, right);
        this.children = children;
        this.numChildren = numChildren;
    }

    /**
     * 
     * @return number of children contained in this node.
     */
    public int getNumChildren()
    {
        return this.numChildren;
    }
    
    /**
     * 
     * @return an array containing the children of this node
     */
    public Long[] getChildren()
    {
        return children;
    }
    
    /**
     * This method is used when the tree has to delete something: it should have found
     * an immediate predecessor to replace this value.
     * @param keyToReplace
     * @param replacement
     */
    public void replace(KeyType keyToReplace, KeyType replacement)
    {
        int indexToReplace = Arrays.binarySearch(keys, 0, numKeys, keyToReplace);
        keys[indexToReplace] = replacement;
    }
    
    /**
     * Adds a key and its right child to their correct locations
     * within the node.
     * @param keyToAdd 
     * @param child 
     */
    @SuppressWarnings("unchecked")
    public void add(KeyType keyToAdd, long child)
    {
        ModifiedBinarySearcher<KeyType> searcher = new ModifiedBinarySearcher<KeyType>(keys);
        KeyType[] tempKeys = (KeyType[]) new Comparable[keys.length];
        int indexToInsert = searcher.findIndexOfNextGreatest(keyToAdd, 0, numKeys - 1) + 1;
        System.arraycopy(keys, 0, tempKeys, 0, indexToInsert);
        tempKeys[indexToInsert] = keyToAdd;
        System.arraycopy( keys, indexToInsert, tempKeys, indexToInsert + 1, keys.length - indexToInsert - 1);
        keys = tempKeys;
        Long[] tempChildren = new Long[children.length];
        indexToInsert += 1;
        System.arraycopy(children, 0, tempChildren, 0, indexToInsert);
        tempChildren[indexToInsert] = child;
        System.arraycopy(children, indexToInsert, tempChildren, indexToInsert + 1, children.length - indexToInsert - 1);
        children = tempChildren;
        numKeys++;
        numChildren++;
    }
    
    /**
     * Adds the key to the beginning of the keys.
     * Should only be called if you also plan on adding a child.
     * @param keyToAdd
     */
    @SuppressWarnings("unchecked")
    public void addFirstKey(KeyType keyToAdd)
    {
        KeyType[] tempKeys = (KeyType[]) new Comparable[keys.length];
        tempKeys[0] = keyToAdd;
        System.arraycopy(keys, 0, tempKeys, 1, numKeys);
        keys = tempKeys;
        ++numKeys;
    }
    
    /**
     * Adds a child to the beginning of the children (leftmost).
     * Should only be called if you plan on adding a key.
     * @param childToAdd
     */
    public void addFirstChild(Long childToAdd)
    {
        Long[] tempChildren = new Long[children.length];
        tempChildren[0] = childToAdd;
        System.arraycopy(children, 0, tempChildren, 1, numChildren);
        children = tempChildren;
        ++numChildren;
    }
    
    /**
     * This method returns the offset of the child that
     * should contain the key, if it is in the tree.
     * @param key
     * @return the offset where the node that may contain the key
     * is located
     */
    public Long search(KeyType key)
    {
        ModifiedBinarySearcher<KeyType> searcher = new ModifiedBinarySearcher<KeyType>(keys);
        return children[searcher.findIndexOfNextGreatest(key, 0, numKeys - 1) + 1];
    }
    
    @Override
    public boolean isLeaf()
    {
        return false;
    }
    
    /**
     * Reassigns the key at index with newKey.
     * @param index
     * @param newKey
     */
    public void updateKey(int index, KeyType newKey)
    {
        keys[index] = newKey;
    }
    
    /**
     * Deletes the key at the given index.
     * @param keyIndex
     * @param deleteLeft whether we are trying to delete leftmost child with this key.
     */
    public void deleteKey(int keyIndex, boolean deleteLeft)
    {
        delete(keys[keyIndex], deleteLeft);
    }
    
    /**
     * Deletes the key (and the associated record offset)
     * from this node.
     * First, we find the index of the key.
     * Make a temp array containing keys[0, indexToSkip - 1]
     * copy keys[indexToSkip + 1, keys.length - 1] over.
     * Do the same with the records.
     * @param keyToDelete 
     * @param deleteLeft 
     */
    @SuppressWarnings("unchecked")
    public void delete(KeyType keyToDelete, boolean deleteLeft)
    {
        int indexToSkip = Arrays.binarySearch(keys, 0, numKeys, keyToDelete);
        if (indexToSkip >= 0)
        {
            KeyType[] tempKeys = (KeyType[]) new Comparable[keys.length];
            System.arraycopy(keys, 0, tempKeys, 0, indexToSkip);
            System.arraycopy(keys, indexToSkip + 1, tempKeys, indexToSkip, keys.length - indexToSkip - 1);
            keys = tempKeys;
            int childIndexToSkip = indexToSkip == 0 && deleteLeft ? 0 : indexToSkip + 1;
            Long[] tempRecords = new Long[children.length];
            System.arraycopy(children, 0, tempRecords, 0, childIndexToSkip);
            System.arraycopy(children, childIndexToSkip + 1, tempRecords, childIndexToSkip, children.length - childIndexToSkip - 1);
            children = tempRecords;
            --numKeys;
            --numChildren;
        }
    }
    
    /**
     * Deletes the given key from the index.
     * @param keyToDelete
     */
    public void deleteKey(KeyType keyToDelete)
    {
        int indexToSkip = Arrays.binarySearch(keys, 0, numKeys, keyToDelete);
        if (indexToSkip >= 0)
        {
            KeyType[] tempKeys = (KeyType[]) new Comparable[keys.length];
            System.arraycopy(keys, 0, tempKeys, 0, indexToSkip);
            System.arraycopy(keys, indexToSkip + 1, tempKeys, indexToSkip, keys.length - indexToSkip - 1);
            keys = tempKeys;
            --numKeys;
        }
    }
    
    /**
     * Returns a child from the right side.
     * @return the rightmost child
     */
    public long borrowChild()
    {
        return children[numChildren - 1];
    }
    
    public void doneBorrowing()
    {
        keys[numKeys - 1] = null;
        children[numChildren - 1] = null;
        --numKeys;
        --numChildren;
    }
    
    /**
     * Adds a key to the end of the keys.
     * @param keyToAdd
     */
    public void addLastKey(KeyType keyToAdd)
    {
        keys[numKeys++] = keyToAdd;
    }
    
    /**
     * Adds a child to the end of the children.
     * @param childToAdd
     */
    public void addLastChild(Long childToAdd)
    {
        children[numChildren++] = childToAdd;
    }
    
    /**
     * Removes entries.  Call only after
     * borrowing from first key AND first child.
     */
    public void doneBorrowingFirst()
    {
        delete(keys[0], true);        
    }

    /**
     * @return the first child
     */
    public long borrowFirstChild()
    {
        return children[0];
    }

    /**
     * Finds the separating key between left and right.
     * @param left
     * @param right
     * @return the key that separates both chidren.
     */
    public KeyType findSeparatingKey(long left, long right)
    {
        return keys[Utility.indexOf(children, left)];
    }

    /**
     * Removes the first child and shifts.
     */
    public void deleteFirstChild()
    {
        Long[] tempChildren = new Long[children.length];
        System.arraycopy( children, 1, tempChildren, 0, numChildren - 1);
        --numChildren;
    }
}