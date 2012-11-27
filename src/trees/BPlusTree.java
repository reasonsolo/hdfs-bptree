package trees;

import java.io.BufferedWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import util.BPlusRecord;
import util.Converter;
import util.ModifiedBinarySearcher;
import util.Utility;

import org.apache.hadoop.conf.Configuration;
/**
 * This class implements a B+ Tree on disk.  The disk IO is handled by
 * the BPlusTreeFile class.
 * 
 * @author xclite
 *
 * @param <KeyType>
 * @param <ValueType>
 * @param <RecordType>
 */
@SuppressWarnings("unchecked")
public class BPlusTree<KeyType extends Comparable<? super KeyType>, ValueType, RecordType extends BPlusRecord<KeyType, ValueType>>
{
    private BPlusNode<KeyType> root;
    private final int M;
    private boolean splitHappened;
    private KeyType upVal; //This is the key of the median of a split leaf.
    private long upRightChildOffset; //This is an offset pointing to the new child after a split occurs.
    private long upLeftChildOffset; //This offset represents the left node after being split.
    private BPlusTreeFile<KeyType, ValueType> treeFile;
    private BufferedWriter logger;
    
    /**
     * Sets m and the logger, initializes the tree file with the converter.
     * @param m
     * @param converter
     * @param logger
     * @throws FileNotFoundException
     */
    public BPlusTree(int m, Converter<KeyType, ValueType> converter, BufferedWriter logger) 
            throws FileNotFoundException, IOException
    {
        this.M = m;
        root = new BPlusLeaf<KeyType, ValueType>(m - 1);
        treeFile = new BPlusTreeFile<KeyType, ValueType>(m, converter);
        this.logger = logger;
        keepRoot();
    }
    
    /**
     * Sets m and the logger, initializes the tree file with the converter. 
     * If both hdfsfilename and conf are not null, btreefile will use assigned hdfs file
     * and ignore local file
     * @param m
     * @param converter
     * @param logger
     * @param localfilename
     * @param hdfsfilename 
     * @param conf
     * @throws FileNotFoundException
     */
    public BPlusTree(int m, Converter<KeyType, ValueType> converter, BufferedWriter logger,
            String localfilename, String hdfsfilename, Configuration conf) 
                    throws FileNotFoundException, IOException
    {
        this.M = m;
        treeFile = new BPlusTreeFile<KeyType, ValueType>(m, converter, localfilename, hdfsfilename, conf);
        try {
            root = treeFile.getRoot();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Array Exception");
            root = new BPlusLeaf<KeyType, ValueType>(m - 1);  
        }
        this.logger = logger;
    }
    
    /**
     * Prints the tree.
     * @throws IOException 
     */
    public void printTree() throws IOException
    {
        {
            printTree(root, 0);
        }
    }

    /**
     * Removes the record from the tree.
     * @param record
     * @throws IOException
     */
    public void remove(RecordType record ) throws IOException
    {
        delete(root, record, -1); 
        if (root.numKeys == 0 && !root.isLeaf())
        {
            root = treeFile.readNode(((BPlusInternalNode<KeyType>)root).getChildren()[0]);
        }
        keepRoot();
    }
    
    public void syncToHdfs(String path, Configuration conf) throws IOException 
    {
        treeFile.setupHdfs(path, conf);
        treeFile.syncToHdfs();
    }
    
    /**
     * Returns all of the records whose keys are within the range
     * (key1, key2) inclusive.
     * @param key1
     * @param key2
     * @return a list of records for the range
     * @throws IOException
     */
    public List<ValueType> getRange(KeyType key1, KeyType key2) throws IOException
    {
        ArrayList<ValueType> recordsInRange = new ArrayList<ValueType>();
        BPlusLeaf<KeyType, ValueType> leaf = findLeaf(root, key1);
        boolean stillInRange = true;
        KeyType[] keys;
        KeyType key;
        do
        {
            keys = leaf.getKeys();
            
            for (int i = 0; i < leaf.getNumKeys(); i++)
            {
                key = keys[i];
                if (key.compareTo(key1) >= 0) //this should only really be false in evaluating the first leaf
                {
                    if (key.compareTo(key2) <= 0)
                    {
                        //TODO: This may be inefficient
                        recordsInRange.add(leaf.search(key));
                    }
                    else
                    {
                        stillInRange = false;
                    }//end check to see if we're still in range
                }//end check to make sure we began in range
            }//end loop over keys in this leaf
            if (leaf.getRight() >= 0)
                leaf = (BPlusLeaf<KeyType, ValueType>)treeFile.readNode(leaf.getRight());
            else //we're out of siblings.
                stillInRange = false;
        }
        while (stillInRange);
        return recordsInRange;
    }
    
   
    private BPlusLeaf<KeyType, ValueType> findLeaf(
        BPlusNode<KeyType> sRoot,
        KeyType key) throws IOException
    {
        if (sRoot.isLeaf())
            return (BPlusLeaf<KeyType, ValueType>) sRoot;
        long childToDescendOffset = ((BPlusInternalNode<KeyType>)sRoot).search(key);
        return findLeaf(treeFile.readNode(childToDescendOffset), key);
    }
    
    /**
     * The root should never be null, but we check anyway.
     * 
     * @return the status of the tree.
     */
    public boolean isEmpty()
    {
        return root == null || root.numKeys == 0;
    }

    /**
     * Writes the tree in a sensical representation to the 
     * logger file supplied.
     * @throws IOException
     */
    public void logTree() throws IOException
    {
        if (logger == null)
        {
            throw new NullPointerException("Logger is not set!");
        }
        if (isEmpty())
        {
            logger.write("Structure is empty.\n");
        }
        else
        {
            logTree(root, 0);
        }
    }
    
    /**
     * Attempts to find the record for the key within the tree.
     * @param key
     * @return the record for the key, or null if not found.
     * @throws IOException
     */
    public ValueType search(KeyType key) throws IOException
    {
        return search(key, root);
    }
    
    /**
     * Delete helper function.
     * @param sRoot
     * @param toDelete
     * @param thisOffset
     * @throws IOException
     */
    private void delete(BPlusNode<KeyType> sRoot, RecordType toDelete, long thisOffset) throws IOException
    {
        if (sRoot.isLeaf())//This only happens when we start at the root.
        {
            ((BPlusLeaf<KeyType, ValueType>)sRoot).delete(toDelete.getKey());
            return;
        }
        
        /*
         * The next part is used to get to a case where sRoot is the parent, and child has the item we're trying to delete.
         */
        long childWithValueOffset = ((BPlusInternalNode<KeyType>) sRoot).search(toDelete.getKey());
        int parentForThisChild = Utility.indexOf(((BPlusInternalNode<KeyType>)sRoot).getChildren(), childWithValueOffset) - 1;
        if (parentForThisChild < 0) parentForThisChild = 0;
        
        BPlusNode<KeyType> child = treeFile.readNode(childWithValueOffset);
        if (!child.isLeaf())//if the child isn't a leaf, we aren't quite at the right parent yet.
        {
            delete(child, toDelete, childWithValueOffset);
            //once we get here, the value has been deleted from the leaf.
            //we could have had internal merges, or this could be the level directly above the parent of the leaf.
            //regardless, the child and its siblings are internal nodes at this point.
            if (child.underflow())
            {
                BPlusInternalNode<KeyType> left = null, right = null;
                Long[] children = ((BPlusInternalNode<KeyType>) sRoot).getChildren();
                if (child.getLeft() >= 0 && Utility.indexOf(children, child.getLeft()) >= 0)
                {
                    left = (BPlusInternalNode<KeyType>) treeFile.readNode(child.getLeft());
                }
                if (child.getRight() >= 0 && Utility.indexOf(children, child.getRight()) >= 0)
                {
                    right = (BPlusInternalNode<KeyType>) treeFile.readNode(child.getRight());
                }
                
                if (left != null && left.canBeBorrowedFrom())
                {
                    KeyType separatingKey = ((BPlusInternalNode<KeyType>)sRoot).findSeparatingKey(child.getLeft(), childWithValueOffset);
                    ((BPlusInternalNode<KeyType>)sRoot).addFirstKey(separatingKey);
                    Long rightChild = left.borrowChild();
                    KeyType replacement = left.borrowKey();
                    ((BPlusInternalNode<KeyType>)sRoot).replace(separatingKey, replacement);
                    ((BPlusInternalNode<KeyType>)child).addFirstChild(rightChild);
                    left.deleteKey(replacement);
                    treeFile.writeInternalNode(left, child.getLeft());
                    treeFile.writeInternalNode((BPlusInternalNode<KeyType>) child, childWithValueOffset);
                }//end borrow from left
                else if (right != null && right.canBeBorrowedFrom())
                {
                    KeyType separatingKey = ((BPlusInternalNode<KeyType>)sRoot).findSeparatingKey(childWithValueOffset, child.getRight());
                    Long leftChild = right.borrowFirstChild();
                    ((BPlusInternalNode<KeyType>) child).add(separatingKey, leftChild);
                    KeyType replacement = right.borrowFirstKey();
                    ((BPlusInternalNode<KeyType>)sRoot).replace(separatingKey, replacement);
                    right.deleteFirstChild();
                    right.deleteKey(replacement);
                    treeFile.writeInternalNode(right, child.getRight());
                    treeFile.writeInternalNode((BPlusInternalNode<KeyType>) child, childWithValueOffset);
                }//end borrowing from right, and borrowing total
                else if (left != null)//merge to the left
                {
                    KeyType separatingKey = ((BPlusInternalNode<KeyType>)sRoot).findSeparatingKey(child.getLeft(), childWithValueOffset);
                    left.addLastKey(separatingKey);
                    Long[] childrenToMove = ((BPlusInternalNode<KeyType>)child).getChildren();
                    for (int i = 0; i < ((BPlusInternalNode<KeyType>)child).getNumChildren(); i++)
                    {
                        left.addLastChild(childrenToMove[i]);
                    }
                    KeyType[] keysToMove = child.getKeys();
                    for (int i = 0; i < child.getNumKeys(); i++)
                    {
                        left.addLastKey(keysToMove[i]);
                    }
                    ((BPlusInternalNode<KeyType>)sRoot).delete(separatingKey, false);
                    left.setRight(child.getRight());
                    treeFile.writeInternalNode(left, child.getLeft());
                }//end merge to left
                else if (right != null)//merge to the right
                {
                    KeyType separatingKey = ((BPlusInternalNode<KeyType>)sRoot).findSeparatingKey(childWithValueOffset, child.getRight());
                    right.addFirstKey(separatingKey);
                    Long[] childrenToMove = ((BPlusInternalNode<KeyType>)child).getChildren();
                    for (int i = ((BPlusInternalNode<KeyType>)child).getNumChildren(); i >= 1; i--)
                    {
                        right.addFirstChild(childrenToMove[i - 1]);
                    }
                    KeyType[] keysToMove = child.getKeys();
                    for (int i = child.getNumKeys(); i >= 1; i--)
                    {
                        right.addFirstKey(keysToMove[i - 1]);
                    }
                    ((BPlusInternalNode<KeyType>)sRoot).delete(separatingKey, true);
                    right.setLeft(child.getLeft());
                    treeFile.writeInternalNode(right, child.getRight());
                }//end merge to the right, and merging total
                else
                {
                    /*
                     * We're screwed.  We can't borrow or merge.  How did things come to this?
                     */
                }
            }
            if(sRoot != root)
            {
                treeFile.writeInternalNode((BPlusInternalNode<KeyType>)sRoot, thisOffset);
            }
            return;
        }//end handling of internal node deletion.
        
        BPlusLeaf<KeyType, ValueType> leaf = (BPlusLeaf<KeyType, ValueType>) child;
        leaf.delete(toDelete.getKey());
        
        if (leaf.underflow())
        {
            BPlusLeaf<KeyType, ValueType> left = null, right = null;
            Long[] children = ((BPlusInternalNode<KeyType>)sRoot).getChildren();
            if (leaf.getLeft() >= 0 && Utility.indexOf(children, leaf.getLeft()) >= 0)
            {
                left = (BPlusLeaf<KeyType, ValueType>)treeFile.readNode(leaf.getLeft());
            }
            if (leaf.getRight() >= 0 && Utility.indexOf(children, leaf.getRight()) >= 0)
            {
                right = (BPlusLeaf<KeyType, ValueType>) treeFile.readNode(leaf.getRight());
            }
            
            if (left != null && left.canBeBorrowedFrom())
            {
                leaf.insert(left.borrowKey(), left.borrowRecord());
                ((BPlusInternalNode<KeyType>)sRoot).updateKey(parentForThisChild, left.borrowKey());
                left.doneBorrowing();
                treeFile.writeLeaf(left, leaf.getLeft());
                treeFile.writeLeaf(leaf, childWithValueOffset);
            }//end borrowing from left
            else if (right != null && right.canBeBorrowedFrom())
            {
                leaf.insert(right.borrowFirstKey(), right.borrowFirstRecord());
                ((BPlusInternalNode<KeyType>)sRoot).updateKey(parentForThisChild + 1, right.borrowFirstKey());
                right.doneBorrowingFirst();
                treeFile.writeLeaf(right, leaf.getRight());
                treeFile.writeLeaf(leaf, childWithValueOffset);
            }//end borrowing from right and from sibling leaves
            else
            {
                if (left != null)
                {
                    for (int i = 0; i < leaf.getNumKeys(); i++)
                    {
                        left.insert(leaf.getKeys()[i], leaf.getRecords()[i]);
                    }
                    left.setRight(child.getRight());
                    treeFile.writeLeaf(left, leaf.getLeft());
                    ((BPlusInternalNode<KeyType>)sRoot).deleteKey(parentForThisChild, false);
                }//end merging to left
                else if (right != null)
                {
                    for (int i = 0; i < leaf.getNumKeys(); i++)
                    {
                        right.insert(leaf.getKeys()[i], leaf.getRecords()[i]);
                    }
                    right.setLeft(child.getLeft());
                    treeFile.writeLeaf(right, leaf.getRight());
                    ((BPlusInternalNode<KeyType>)sRoot).deleteKey(parentForThisChild, true);
                }//end merging to right
                else
                {
                    /*
                     * We're screwed.  We can't borrow or merge.  How did things come to this?
                     */
                }
            }//end merge leaves
            if(sRoot != root)
            {
                treeFile.writeInternalNode((BPlusInternalNode<KeyType>)sRoot, thisOffset);
            }
            return;
        }//end leaf underflows
        else
        {
            treeFile.writeLeaf(leaf, childWithValueOffset);
        }
    }
    
    private ValueType search(KeyType key, BPlusNode<KeyType> sRoot) throws IOException
    {
        if (sRoot.isLeaf())
        {
            return ((BPlusLeaf<KeyType, ValueType>)sRoot).search(key);
        }
        long childToDescendOffset = ((BPlusInternalNode<KeyType>)sRoot).search(key);
        return search(key, treeFile.readNode(childToDescendOffset));
    }

    private void logTree(BPlusNode<KeyType> subRoot, int depth) throws IOException
    {
        if (!subRoot.isLeaf())
        {
            BPlusNode<KeyType> child;
            Long[] childrenOffsets = ((BPlusInternalNode<KeyType>) subRoot).getChildren();
            /*
             * numChildren is the number of elements in the internal node that are pointers to child nodes.
             * i is the index of the current child being inspected.
             * keyIndex is the index of the key to output.  We want to output the key to each
             * child between the correct children.
             */
            for (int numChildren = ((BPlusInternalNode<KeyType>)subRoot).getNumChildren(), i = numChildren - 1, keyIndex = subRoot.getNumKeys() - 1; i >= 0; i--)
            {
                if (keyIndex >= 0 && i < numChildren - 1) // if we have completed a row, start printing the keys in this node.
                {
                    for (int j = 0; j < depth; j++)
                    {
                        logger.write("\t");
                 
                    }
                    logger.write(subRoot.getKeys()[keyIndex--].toString() + "\n\n");
                }
                
                if (i <= numChildren - 1) //if the index is within the range of actual stored offsets
                {
                    child = treeFile.readNode(childrenOffsets[i]);
                    logTree(child, depth + 2);
                    if (child.isLeaf())
                        logger.write("\n");
                }
            }
        }
        else //writing leaves
        {
            KeyType[] keys = subRoot.getKeys();
            int numKeys = subRoot.getNumKeys();
            ValueType[] values = ((BPlusLeaf<KeyType, ValueType>)subRoot).getRecords();
            for (int keyIndex = M - 2; keyIndex >= 0; keyIndex--)
            {
                for (int j = 0; j < depth; j++)
                {
                    logger.write("\t");
                }
                if (keyIndex <= numKeys - 1) // i the index is within the range of valid keys
                {
                    logger.write(keys[keyIndex] + ":" + values[keyIndex] + "\n");
                }
                else
                {
                    logger.write("No entry\n");
                }
            }
        }
    }
    
    private void printTree( BPlusNode<KeyType> subRoot, int depth) throws IOException
    {
        if (!subRoot.isLeaf())
        {
            BPlusNode<KeyType> child;
            Long[] childrenOffsets = ((BPlusInternalNode<KeyType>) subRoot).getChildren();
            /*
             * numChildren is the number of elements in the internal node that are pointers to child nodes.
             * i is the index of the current child being inspected.
             * keyIndex is the index of the key to output.  We want to output the key to each
             * child between the correct children.
             */
            for (int numChildren = ((BPlusInternalNode<KeyType>)subRoot).getNumChildren(), i = numChildren - 1, keyIndex = subRoot.getNumKeys() - 1; i >= 0; i--)
            {
                if (keyIndex >= 0 && i < numChildren - 1) // if we have completed a row, start printing the keys in this node.
                {
                    for (int j = 0; j < depth; j++)
                    {
                        System.out.print("\t");
                 
                    }
                    System.out.println(subRoot.getKeys()[keyIndex--]);
                }
                
                if (i <= numChildren - 1) //if the index is within the range of actual stored offsets
                {
                    child = treeFile.readNode(childrenOffsets[i]);
                    printTree(child, depth + 1);
                    System.out.println();
                }
            }
        }
        else //writing leaves.
        {
            KeyType[] keys = subRoot.getKeys();
            int numKeys = subRoot.getNumKeys();
            ValueType[] values = ((BPlusLeaf<KeyType, ValueType>)subRoot).getRecords();
            for (int keyIndex = M - 2; keyIndex >= 0; keyIndex--)
            {
                for (int j = 0; j < depth; j++)
                {
                    System.out.print("\t");
                }
                if (keyIndex <= numKeys - 1) // i the index is within the range of valid keys
                {
                    System.out.println(keys[keyIndex] + ":" + values[keyIndex]);
                }
                else
                {
                    System.out.println("No entry");
                }
            }
        }
    }

    /**
     * Keep root in disk file
     * @throws IOException
     */
    public void keepRoot() throws IOException {
        System.err.print("keep root");
        if (root.isLeaf()) {
            treeFile.writeLeaf((BPlusLeaf<KeyType, ValueType>)root, -1);
        } else {
            treeFile.writeInternalNode((BPlusInternalNode<KeyType>)root, -1);
        }
    }
    
    /**
     * Public method for inserting a record into the tree.
     * @param record
     * @throws IOException
     */
    public void insert(RecordType record) throws IOException
    {
        insert(record, root, -1);
        if (splitHappened) //if a split happened, the root was split.  All we have to do is make the root a new internal node with one key (upVal) and the left/right pointers.
        {
            KeyType[] newRootKeys = (KeyType[]) new Comparable[M -1];
            Long[] newRootChildren = new Long[M];
            newRootKeys[0] = upVal;
            newRootChildren[0] = upLeftChildOffset;
            newRootChildren[1] = upRightChildOffset;
            root = new BPlusInternalNode<KeyType>(newRootKeys, 1, newRootChildren, 2);
        }
        keepRoot();
    }

    /**
     * Insert helper method.
     * @param record
     * @param sRoot
     * @param rootOffset
     * @throws IOException
     */
    private void insert(RecordType record, BPlusNode<KeyType> sRoot, long rootOffset) throws IOException
    {
        if (sRoot.isLeaf())
        {
            if (!sRoot.isFull())
            {
                ((BPlusLeaf<KeyType, ValueType>) sRoot).insert(record.getKey(), record.getValue());
                splitHappened = false;
                if (sRoot != root)
                {
                    treeFile.writeLeaf((BPlusLeaf<KeyType, ValueType>) sRoot, rootOffset);
                }
            }
            else
            {
                splitLeaf((BPlusLeaf<KeyType, ValueType>) sRoot, rootOffset, record);
            }
            return;
        }
        long childToDescendOffset = ((BPlusInternalNode<KeyType>)sRoot).search(record.getKey());
        insert(record, treeFile.readNode(childToDescendOffset), childToDescendOffset);
        
        if (splitHappened)
        {
            if (!sRoot.isFull())
            {
                ((BPlusInternalNode<KeyType>) sRoot).add(upVal, upRightChildOffset);
                splitHappened = false;
                if (sRoot != root)
                {
                    treeFile.writeInternalNode((BPlusInternalNode<KeyType>)sRoot, rootOffset);
                }
            }
            else
            {
                splitInternalNode(upVal, upRightChildOffset, (BPlusInternalNode<KeyType>)sRoot, rootOffset);
            }
        }
        return;
    }
    

    private void splitInternalNode(KeyType key, Long offset, BPlusInternalNode<KeyType> sRoot, long thisOffset) throws IOException
    {
        KeyType[] keys = sRoot.getKeys();
        Long[] offsets = sRoot.getChildren();
        
        //tempSortedKeys holds the keys before copying them into the new array with the key in the correct location
        KeyType[] tempSortedKeys = (KeyType[]) new Comparable[M];
        Long[] sortedOffsets = new Long[M + 1];
        
        
        /*
         * Find where the key should go, copy elements around it into tempSortedKeys
         */
        ModifiedBinarySearcher<KeyType> searcher = new ModifiedBinarySearcher(keys);
        int keyIndex = searcher.findIndexOfNextGreatest(key, 0, M - 2) + 1;
        System.arraycopy(keys, 0, tempSortedKeys, 0, keyIndex);
        tempSortedKeys[keyIndex] = key;
        System.arraycopy(keys, keyIndex, tempSortedKeys, keyIndex + 1, M - keyIndex - 1);
        
        /*
         * Put the offset in the correct location.
         */
        System.arraycopy(offsets, 0, sortedOffsets, 0, keyIndex + 1);
        sortedOffsets[keyIndex + 1] = offset;
        if (keyIndex + 1 < offsets.length)
        {
            System.arraycopy( offsets, keyIndex + 1, sortedOffsets, keyIndex + 2, M - keyIndex - 1);
        }
        
        int upValIndex = tempSortedKeys.length / 2;
        upVal = tempSortedKeys[upValIndex];
        
        
        KeyType[] leftKeys = (KeyType[]) new Comparable[M - 1];
        KeyType[] rightKeys = (KeyType[]) new Comparable[M - 1];
        
        int rightNumKeys, leftNumKeys;
        
        leftNumKeys = upValIndex;
        rightNumKeys = tempSortedKeys.length - upValIndex - 1;   
        
        System.arraycopy(tempSortedKeys, 0, leftKeys, 0, upValIndex);
        System.arraycopy(tempSortedKeys, upValIndex + 1, rightKeys, 0, tempSortedKeys.length - upValIndex - 1);
        
        Long[] leftChildren = new Long[M];
        Long[] rightChildren = new Long[M];
        
        System.arraycopy(sortedOffsets, 0, leftChildren, 0, upValIndex + 1);
        System.arraycopy(sortedOffsets, upValIndex + 1, rightChildren, 0, sortedOffsets.length - upValIndex - 1);
        
        boolean wasRoot = sRoot == root;
        if (wasRoot)
        {
            thisOffset = treeFile.getFilePointer() + treeFile.getNodeSize();
        }
        
        BPlusInternalNode<KeyType> newNode = new BPlusInternalNode<KeyType>(rightKeys, rightNumKeys, rightChildren, rightNumKeys + 1, thisOffset, sRoot.getRight());
        upRightChildOffset = treeFile.writeNewInternalNode(newNode);
        long rightOffset = sRoot.getRight();
        if (rightOffset >= 0)
        {
            BPlusInternalNode<KeyType> right = (BPlusInternalNode<KeyType>)treeFile.readNode(rightOffset);
            right.setLeft(upRightChildOffset);
            treeFile.writeInternalNode(right, rightOffset);
        }
        sRoot = new BPlusInternalNode<KeyType>(leftKeys, leftNumKeys, leftChildren, leftNumKeys + 1, sRoot.getLeft(), upRightChildOffset);
        
        if (wasRoot)
        {
            upLeftChildOffset = treeFile.writeNewInternalNode(sRoot);
        }
        else
        {
            treeFile.writeInternalNode(sRoot, thisOffset);
            upLeftChildOffset = -1;
        }
        
        splitHappened = true;
        return;
    }

    private void splitLeaf(BPlusLeaf<KeyType, ValueType> sRoot, long thisOffset, RecordType record) throws IOException
    {
        KeyType[] keys = sRoot.getKeys();
        KeyType[] leftKeys = (KeyType[]) new Comparable[M - 1];
        KeyType[] rightKeys = (KeyType[]) new Comparable[M - 1];
                
        //tempSortedKeys holds the keys before copying them into the new array with the key in the correct location
        KeyType[] tempSortedKeys = (KeyType[]) new Comparable[M];
        ValueType[] sortedChildren = (ValueType[])new Object[M];
        
        /*
         * Find where the key should go, copy elements around it into tempSortedKeys
         */
        ModifiedBinarySearcher<KeyType> searcher = new ModifiedBinarySearcher(keys);
        int keyIndex = searcher.findIndexOfNextGreatest(record.getKey(), 0, M - 2) + 1;
        System.arraycopy(keys, 0, tempSortedKeys, 0, keyIndex);
        tempSortedKeys[keyIndex] = record.getKey();
        System.arraycopy(keys, keyIndex, tempSortedKeys, keyIndex + 1, M - keyIndex - 1); 
        
        int upValIndex = tempSortedKeys.length / 2;
        upVal = tempSortedKeys[upValIndex];
        
        System.arraycopy(tempSortedKeys, 0, leftKeys, 0, upValIndex);
        System.arraycopy(tempSortedKeys, upValIndex, rightKeys, 0, M - upValIndex);
        
        /*
         * Copy the records into the sorted array, then copy each segment of that array into the correct node array
         */
        ValueType[] records = sRoot.getRecords();
        System.arraycopy( records, 0, sortedChildren, 0, keyIndex);
        sortedChildren[keyIndex] = record.getValue();
        System.arraycopy(records, keyIndex, sortedChildren, keyIndex + 1, M - keyIndex - 1); 
        ValueType[] leftRecords = (ValueType[]) new Object[M -1];
        ValueType[] rightRecords = (ValueType[]) new Object[M - 1];
        
        System.arraycopy(sortedChildren, 0, leftRecords, 0, upValIndex);
        System.arraycopy(sortedChildren, upValIndex, rightRecords, 0, M - upValIndex); 
        
        if (sRoot == root)
        {
            //get what the offset will be after this one.
            thisOffset = treeFile.getFilePointer() + treeFile.getNodeSize();
        }
        BPlusLeaf<KeyType, ValueType> newNode = new BPlusLeaf<KeyType, ValueType>(rightKeys, M - upValIndex, rightRecords, thisOffset, sRoot.getRight());
        upRightChildOffset = treeFile.writeNewLeaf(newNode);
        boolean wasRoot = sRoot == root;
        
        //in addition to changing references within the node being split and the split off node, the former right node of the node being split must point to the new node as its left
        long rightOffset = sRoot.getRight();
        if (rightOffset >= 0)
        {
            BPlusLeaf<KeyType, ValueType> right = (BPlusLeaf<KeyType, ValueType>)treeFile.readNode(rightOffset);
            right.setLeft(upRightChildOffset);
            treeFile.writeLeaf(right, rightOffset);
        }
        
        sRoot = new BPlusLeaf<KeyType, ValueType>(leftKeys, upValIndex, leftRecords, sRoot.getLeft(), upRightChildOffset);
        if (wasRoot)
        {
            upLeftChildOffset = treeFile.writeNewLeaf(sRoot);
        }
        else
        {
            treeFile.writeLeaf(sRoot, thisOffset);
        }
        splitHappened = true;
        return;
    }   
}