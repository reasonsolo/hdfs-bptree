package trees;

/**
 * This class represents a node within a B+ tree, stored
 * on disk.
 * @author xclite
 *
 */
public abstract class BPlusNode<KeyType extends Comparable<? super KeyType>>
{
    protected KeyType[] keys;
    protected int numKeys;
    protected long left, right;
    /**
     * Constructor for shared data.
     * @param keys
     * @param numKeys
     */
    public BPlusNode(KeyType[] keys, int numKeys, long left, long right)
    {
        this.keys = keys;
        this.numKeys = numKeys;
        this.left = left;
        this.right = right;
    }
    /**
     * A quick checker to see if the node is a leaf-
     * if so, we have found the final result of the search.
     * @return true if this is a leaf
     */
    public abstract boolean isLeaf();
   
    /**
     * Returns the array of keys
     * @return the keys
     */
    public KeyType[] getKeys()
    {
        return this.keys;
    }
    
    /**
     * Returns the number of keys in keys
     * @return number of keys present
     */
    public int getNumKeys()
    {
        return this.numKeys;
    }
    
    /**
     * 
     * @return true if the node is full
     */
    public boolean isFull()
    {
        return numKeys == keys.length;
    }
    
    /**
     * @return true if we have < cieling(m/2) -1 keys
     */
    public boolean underflow()
    {
        return numKeys < Math.ceil(keys.length / 2.0) - 1; 
    }
    
    /**
     * 
     * @return the offset of the right sibling of this node.
     */
    public long getRight()
    {
        return this.right;
    }
    
    /**
     * 
     * @return the offset of the left sibling of this node.
     */
    public long getLeft()
    {
        return this.left;
    }
    
    /**
     * Answers the question: Do we have keys to spare?
     * @return true if we can, false otherwise.
     */
    public boolean canBeBorrowedFrom()
    {
        return (numKeys - 1) >= Math.ceil(keys.length / 2.0) - 1; 
    }

    /**
     * 
     * @return the last key
     */
    public KeyType borrowKey()
    {
        return keys[numKeys - 1];
    }
    
    /**
     * 
     * @return the first key
     */
    public KeyType borrowFirstKey()
    {
        return keys[0];
    }
    
    
    /**
     * @param left
     */
    public final void setLeft(long left)
    {
        this.left = left;
    }
    
    /**
     * @param right
     */
    public final void setRight(long right)
    {
        this.right = right;
    }
    
    /**
     * performs several cleaning up actions after borrowing from the right.
     */
    public abstract void doneBorrowing();
    /**
     * performs several cleanign up actions after borrowing from the left.
     */
    public abstract void doneBorrowingFirst();
}