package trees;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import javax.naming.OperationNotSupportedException;
import org.apache.hadoop.conf.Configuration;

import util.BPlusRecord;
import util.Converter;
import trees.HdfsFile;

/**
 * This file handles the writing of nodes to files and the reading of nodes
 * back.
 * 
 * @author xclite
 * 
 * @param <KeyType>
 * @param <ValueType>
 */
public class BPlusTreeFile<KeyType extends Comparable<? super KeyType>, ValueType> {
    private static final String DEFAULT_FILE_NAME = "BPlusTree.bin";

    // This is the calculated length of the longest node - we pad both nodes to
    // the same size so that
    // we can read all of the bytes for a node without having to know what kind
    // of node it is (yet)
    private final int LENGTH_OF_NODE_BYTES;

    // M is the order of the tree that uses this file.
    private final int M;

    // A Converter between the type and its binary format is required.
    private final Converter<KeyType, ValueType> converter;

    private String localFileName;

    private String hdfsFileName;

    private HdfsFile hdfsFile;

    private RandomAccessFile localFile;

    private boolean synced;

    /**
     * General constructor.
     * 
     * @param m
     * @param converter
     * @throws FileNotFoundException
     */
    public BPlusTreeFile(int m, Converter<KeyType, ValueType> converter)
            throws FileNotFoundException, IOException {
        localFileName = DEFAULT_FILE_NAME;
        hdfsFile = null;
        localFile = new RandomAccessFile(localFileName, "rw");
        M = m;
        synced = false;
        this.converter = converter;
        LENGTH_OF_NODE_BYTES = Math.max(
                BPlusTreeFile.calculateLeafSize(m, converter),
                BPlusTreeFile.calculateInternalNodeSize(m, converter));
    }

    public BPlusTreeFile(int m, Converter<KeyType, ValueType> converter, String localfilename, String hdfsfilename, Configuration conf)
            throws FileNotFoundException, IOException {
        localFileName = localfilename;
        hdfsFileName = hdfsfilename;
        M = m;
        if (hdfsfilename != null && conf != null)
        {
            hdfsFile = new HdfsFile(hdfsfilename, conf);
            synced = true;
        } else {
            localFile = new RandomAccessFile(localFileName, "rw");
            synced = false;
        }
        this.converter = converter;
        LENGTH_OF_NODE_BYTES = Math.max(
                BPlusTreeFile.calculateLeafSize(m, converter),
                BPlusTreeFile.calculateInternalNodeSize(m, converter));
    }
    
    public void setupHdfs(String path, Configuration conf) throws IOException {
        if (hdfsFile == null) {
            hdfsFileName = path;
            hdfsFile = new HdfsFile(hdfsFileName, conf);
        }
    }

    public boolean syncToHdfs() throws IOException {
        if (!synced) {
            if (hdfsFile == null) {
                throw new IOException("Please setup hdfs first");
            }
            System.err.println("synchronizing");
            hdfsFile.copyLocalToHdfs(localFileName);
            System.err.println("file synced");
            synced = true;
        }
        return synced;
    }

    /**
     * Calculates the minimum length of the byte array required to represent the
     * BPlusLeaf.
     * 
     * @param m
     * @param converter
     * @return
     */
    private static int calculateLeafSize(int m, Converter converter) {
        return 1 + 4 + (m - 1) * converter.getKeyLength() + (m - 1)
                * converter.getRecordLength() + 16;
    }

    /**
     * Calculates the minimum length of the byte array required to represent the
     * BPlusInternalNode.
     * 
     * @param m
     * @param converter
     * @return
     */
    private static int calculateInternalNodeSize(int m, Converter converter) {
        return 1 + 4 + (m - 1) * converter.getKeyLength() + m * 8 + 4 + 16;
    }

    /**
     * Reads the bytes required to construct a node from the given offset.
     * 
     * @param offset
     * @return
     * @throws IOException
     */
    private byte[] readBytes(long offset) throws IOException {

        byte[] bytes = new byte[LENGTH_OF_NODE_BYTES];
        if (!synced) {
            long oldOffset = localFile.getFilePointer();
            localFile.seek(offset);
            localFile.read(bytes);
            localFile.seek(oldOffset);
        } else {
            hdfsFile.read(offset, bytes, 0, LENGTH_OF_NODE_BYTES);
        }
        return bytes;
    }

    /**
     * public method for reading a node at a given offset. First determines the
     * type of node, then calls the appropriate helper method.
     * 
     * @param offset
     * @return
     * @throws IOException
     */
    public BPlusNode<KeyType> readNode(long offset) throws IOException {
        byte[] bytes = readBytes(offset);
        byte flag = bytes[0];
        BPlusNode<KeyType> toReturn;
        if (flag == 0) {
            toReturn = getLeaf(bytes);
        } else {
            toReturn = getInternalNode(bytes);
        }
        return toReturn;
    }

    /**
     * Method for creating a leaf from the given byte array.
     * 
     * @param bytes
     * @return
     */
    private BPlusLeaf<KeyType, ValueType> getLeaf(byte[] bytes) {
        int numKeys;
        ValueType[] records = (ValueType[]) new Object[M - 1];
        byte[] tempArray;
        KeyType[] keys = (KeyType[]) new Comparable[M - 1];
        int arrayCursor = 5;
        long left, right;

        // Get number of keys
        tempArray = new byte[4];
        System.arraycopy(bytes, 1, tempArray, 0, 4);
        numKeys = BPlusTreeFile.bytesToInt(tempArray);

        // We have numKeys keys to parse first, each key is key.length
        for (int startingCursor = arrayCursor, i = 0; arrayCursor < startingCursor
                + numKeys * converter.getKeyLength(); arrayCursor += converter
                .getKeyLength(), i++) {
            tempArray = new byte[converter.getKeyLength()];
            System.arraycopy(bytes, arrayCursor, tempArray, 0,
                    converter.getKeyLength());
            keys[i] = converter.bytesToKey(tempArray);
        }
        // skip empty cells
        arrayCursor = 5 + (M - 1) * converter.getKeyLength();

        // add all of the internal children.
        int prevCursor = arrayCursor;
        for (int i = 0; i < numKeys; arrayCursor += converter.getRecordLength(), i++) {
            tempArray = new byte[converter.getRecordLength()];
            System.arraycopy(bytes, arrayCursor, tempArray, 0,
                    converter.getRecordLength());
            records[i] = converter.bytesToRecord(tempArray);
        }

        // skip empty records
        arrayCursor = prevCursor + (M - 1) * converter.getRecordLength();

        // Read the left and the right pointers to siblings
        tempArray = new byte[8];
        System.arraycopy(bytes, arrayCursor, tempArray, 0, 8);
        left = BPlusTreeFile.bytesToLong(tempArray);
        arrayCursor += 8;
        System.arraycopy(bytes, arrayCursor, tempArray, 0, 8);
        right = BPlusTreeFile.bytesToLong(tempArray);

        return new BPlusLeaf<KeyType, ValueType>(keys, numKeys, records, left,
                right);
    }

    /**
     * Method for creating an internal node from the given bytes.
     * 
     * @param bytes
     * @return
     */
    private BPlusInternalNode<KeyType> getInternalNode(byte[] bytes) {
        int numKeys, numChildren;
        Long[] children = new Long[M];
        byte[] tempArray;
        KeyType[] keys = (KeyType[]) new Comparable[M - 1];
        int arrayCursor = 5;
        long left, right;

        // Get number of keys
        tempArray = new byte[4];
        System.arraycopy(bytes, 1, tempArray, 0, 4);
        numKeys = BPlusTreeFile.bytesToInt(tempArray);

        // We have numKeys keys to parse first, each key is key.length
        for (int startingCursor = arrayCursor, i = 0; arrayCursor < startingCursor
                + numKeys * converter.getKeyLength(); arrayCursor += converter
                .getKeyLength(), i++) {
            tempArray = new byte[converter.getKeyLength()];
            System.arraycopy(bytes, arrayCursor, tempArray, 0,
                    converter.getKeyLength());
            keys[i] = converter.bytesToKey(tempArray);
        }
        // skip empty cells
        arrayCursor = 5 + (M - 1) * converter.getKeyLength();

        // Parse the number of children
        tempArray = new byte[4];
        System.arraycopy(bytes, arrayCursor, tempArray, 0, 4);
        arrayCursor += 4;
        numChildren = BPlusTreeFile.bytesToInt(tempArray);

        int prevCursor = arrayCursor;
        // add all of the internal children.
        for (int i = 0; i < numChildren; arrayCursor += 8, i++) {
            tempArray = new byte[8];
            System.arraycopy(bytes, arrayCursor, tempArray, 0, 8);
            children[i] = BPlusTreeFile.bytesToLong(tempArray);
        }
        // skip empty children
        // skip empty records
        arrayCursor = prevCursor + (M) * 8;
        System.arraycopy(bytes, arrayCursor, tempArray, 0, 8);
        left = BPlusTreeFile.bytesToLong(tempArray);
        arrayCursor += 8;
        System.arraycopy(bytes, arrayCursor, tempArray, 0, 8);
        right = BPlusTreeFile.bytesToLong(tempArray);
        return new BPlusInternalNode<KeyType>(keys, numKeys, children,
                numChildren, left, right);
    }

    /**
     * Writes a new internal node to the end of the file. Assumes the file
     * pointer is already at the end of the file.
     * 
     * @param toWrite
     * @return
     * @throws IOException
     */
    public long writeNewInternalNode(BPlusInternalNode<KeyType> toWrite)
            throws IOException {
        long pointer = localFile.getFilePointer();
        writeInternalNode(toWrite, localFile.getFilePointer(), false);
        return pointer;
    }

    /**
     * Writes a new leaf to the end of the file. Assumes the file pointer is
     * already at the end of the file.
     * 
     * @param toWrite
     * @return
     * @throws IOException
     */
    public long writeNewLeaf(BPlusLeaf<KeyType, ValueType> toWrite)
            throws IOException {
        long pointer = localFile.getFilePointer();
        writeLeaf(toWrite, localFile.getFilePointer(), false);
        return pointer;
    }

    public void writeInternalNode(BPlusInternalNode<KeyType> toWrite,
            long offset) throws IOException {
        writeInternalNode(toWrite, offset, true);
    }

    public void writeLeaf(BPlusLeaf<KeyType, ValueType> toWrite, long offset)
            throws IOException {
        writeLeaf(toWrite, offset, true);
    }

    /**
     * Method for converting an internal node to bytes and writing it to the
     * file.
     * 
     * @param toWrite
     * @param offset
     * @throws OperationNotSupportedException
     * @throws IOException
     */
    private void writeInternalNode(BPlusInternalNode<KeyType> toWrite,
            long offset, boolean alreadyInFile) throws IOException {
        if (synced)
            throw new IOException("File has been synced to hdfs");
        byte[] bytesToWrite = new byte[LENGTH_OF_NODE_BYTES];
        byte[] tempArray;
        int arrayCursor = 0;

        // Write the flag for determining what sort of node this is.
        bytesToWrite[arrayCursor++] = 1;

        // Write the number of keys
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(toWrite.getNumKeys());
        tempArray = byteBuffer.array();
        System.arraycopy(tempArray, 0, bytesToWrite, arrayCursor, 4);
        arrayCursor += 4;

        // write keys
        KeyType[] keys = toWrite.getKeys();
        for (int i = 0; i < toWrite.getNumKeys(); i++, arrayCursor += converter
                .getKeyLength()) {
            tempArray = converter.keyToBytes(keys[i]);
            System.arraycopy(tempArray, 0, bytesToWrite, arrayCursor,
                    converter.getKeyLength());
        }
        // skip empty cells
        arrayCursor = 5 + (M - 1) * converter.getKeyLength();

        // Write number of children
        byteBuffer = ByteBuffer.allocate(4);
        intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(toWrite.getNumChildren());
        tempArray = byteBuffer.array();
        System.arraycopy(tempArray, 0, bytesToWrite, arrayCursor, 4);
        arrayCursor += 4;

        // write children
        Long[] children = toWrite.getChildren();
        int prevCursor = arrayCursor;

        for (int i = 0; i < toWrite.getNumChildren(); i++, arrayCursor += 8) {
            tempArray = BPlusTreeFile.longToBytes(children[i]);
            System.arraycopy(tempArray, 0, bytesToWrite, arrayCursor, 8);
        }
        // skip empty children
        arrayCursor = prevCursor + (M) * 8;
        tempArray = BPlusTreeFile.longToBytes(toWrite.getLeft());
        System.arraycopy(tempArray, 0, bytesToWrite, arrayCursor, 8);
        arrayCursor += 8;
        tempArray = BPlusTreeFile.longToBytes(toWrite.getRight());
        System.arraycopy(tempArray, 0, bytesToWrite, arrayCursor, 8);
        arrayCursor += 8;
        if (alreadyInFile) {
            long oldOffset = localFile.getFilePointer();
            localFile.seek(offset);
            localFile.write(bytesToWrite);
            localFile.seek(oldOffset);
        } else {
            localFile.seek(offset);
            localFile.write(bytesToWrite);
        }
    }

    /**
     * Method for converting a leaf to bytes and then writing it to this file.
     * 
     * @param toWrite
     * @param offset
     * @throws IOException
     */
    private void writeLeaf(BPlusLeaf<KeyType, ValueType> toWrite, long offset,
            boolean alreadyInFile) throws IOException {
        if (synced)
            throw new IOException("File has been synced to hdfs");
        
        byte[] bytesToWrite = new byte[LENGTH_OF_NODE_BYTES];
        byte[] tempArray;
        int arrayCursor = 0;

        // Write the flag for determining what sort of node this is.
        bytesToWrite[arrayCursor++] = 0;

        // Write the number of keys
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(toWrite.getNumKeys());
        tempArray = byteBuffer.array();
        System.arraycopy(tempArray, 0, bytesToWrite, arrayCursor, 4);
        arrayCursor += 4;

        // write keys
        KeyType[] keys = toWrite.getKeys();
        for (int i = 0; i < toWrite.getNumKeys(); i++, arrayCursor += converter
                .getKeyLength()) {
            tempArray = converter.keyToBytes(keys[i]);
            System.arraycopy(tempArray, 0, bytesToWrite, arrayCursor,
                    converter.getKeyLength());
        }

        // Skip any empty keys
        arrayCursor = 5 + (M - 1) * converter.getKeyLength();
        int prevCursor = arrayCursor;

        // write records
        ValueType[] records = toWrite.getRecords();
        for (int i = 0; i < toWrite.getNumKeys(); i++, arrayCursor += converter
                .getRecordLength()) {
            tempArray = converter.recordToBytes(records[i]);
            System.arraycopy(tempArray, 0, bytesToWrite, arrayCursor,
                    converter.getRecordLength());
        }

        // skip any empty records
        arrayCursor = prevCursor + (M - 1) * converter.getRecordLength();

        tempArray = BPlusTreeFile.longToBytes(toWrite.getLeft());
        System.arraycopy(tempArray, 0, bytesToWrite, arrayCursor, 8);
        arrayCursor += 8;
        tempArray = BPlusTreeFile.longToBytes(toWrite.getRight());
        System.arraycopy(tempArray, 0, bytesToWrite, arrayCursor, 8);
        arrayCursor += 8;
        if (alreadyInFile) {
            long oldOffset = localFile.getFilePointer();
            localFile.seek(offset);
            localFile.write(bytesToWrite);
            localFile.seek(oldOffset);
        } else {
            localFile.seek(offset);
            localFile.write(bytesToWrite);
        }
    }
    
    // method reimplementation
    public long getFilePointer() throws IOException {
        if (synced)
            throw new IOException("File has been synced to hdfs");
        return localFile.getFilePointer();
    }

    private static int bytesToInt(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getInt();
    }

    private static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getLong();
    }

    private static byte[] longToBytes(long value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        LongBuffer longBuffer = byteBuffer.asLongBuffer();
        longBuffer.put(value);
        return byteBuffer.array();
    }

    public long getNodeSize() {
        return this.LENGTH_OF_NODE_BYTES;
    }

}