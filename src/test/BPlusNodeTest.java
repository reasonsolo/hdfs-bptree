package test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import javax.naming.OperationNotSupportedException;

import org.junit.Before;
import org.junit.Test;

import trees.BPlusInternalNode;
import trees.BPlusLeaf;
import trees.BPlusNode;
import trees.BPlusTreeFile;

public class BPlusNodeTest
{
    BPlusInternalNode<String> testNode, partialNode;
    BPlusLeaf<String, Long> testLeaf;
    public BPlusNodeTest()
    {
        //empty
    }

    @Before
    public void setUp()
    {
        String[] keys = {"b", "c", "d", "f", "g"};
        Long[] records = {new Long(0), new Long(1), new Long(2),
            new Long(3), new Long(4)};
        Long[] offsets = {new Long(0), new Long(1), new Long(2),
         new Long(3), new Long(4), new Long(5)};
        testNode = new BPlusInternalNode<String>(keys, 5, offsets, 6);
        String[] partialKeys = new String[5];
        Long[] partialOffsets = new Long[6];
        partialKeys[0] = "b";
        partialKeys[1] = "c";
        partialKeys[2] = "d";
        partialKeys[3] = "g";
        partialOffsets[0] = new Long(0);
        partialOffsets[1] = new Long(1);
        partialOffsets[2] = new Long(2);
        partialOffsets[3] = new Long(3);
        partialOffsets[4] = new Long(5);
        partialNode = new BPlusInternalNode<String>(partialKeys, 4, partialOffsets, 5);
        testLeaf = new BPlusLeaf<String, Long>(keys, 5, records, -1, -1);
    }
    
    @Test
    public void testPartialAdds()
    {
        partialNode.add("f", new Long(4));
        assertTrue(partialNode.isFull());
        assertEquals(partialNode.search("a").longValue(), 0);
        assertEquals(partialNode.search("b").longValue(), 1);
        assertEquals(partialNode.search("c").longValue(), 2);
        assertEquals(partialNode.search("d").longValue(), 3);
        assertEquals(partialNode.search("e").longValue(), 3);
        assertEquals(partialNode.search("f").longValue(), 4);
        assertEquals(partialNode.search("g").longValue(), 5);
    }
    
    @Test
    public void testSearchInit()
    {
        assertEquals(testNode.search("a").longValue(), 0);
        assertEquals(testNode.search("b").longValue(), 1);
        assertEquals(testNode.search("c").longValue(), 2);
        assertEquals(testNode.search("d").longValue(), 3);
        assertEquals(testNode.search("e").longValue(), 3);
        assertEquals(testNode.search("f").longValue(), 4);
        assertEquals(testNode.search("g").longValue(), 5);
    }
    
    @Test
    public void testReplace()
    {
        testNode.replace("d", "e");
        assertEquals(testNode.search("a").longValue(), 0);
        assertEquals(testNode.search("b").longValue(), 1);
        assertEquals(testNode.search("c").longValue(), 2);
        assertEquals(testNode.search("d").longValue(), 2);
        assertEquals(testNode.search("e").longValue(), 3);
        assertEquals(testNode.search("f").longValue(), 4);
        assertEquals(testNode.search("g").longValue(), 5);
        testNode.replace("e", "d");
        assertEquals(testNode.search("a").longValue(), 0);
        assertEquals(testNode.search("b").longValue(), 1);
        assertEquals(testNode.search("c").longValue(), 2);
        assertEquals(testNode.search("d").longValue(), 3);
        assertEquals(testNode.search("e").longValue(), 3);
        assertEquals(testNode.search("f").longValue(), 4);
        assertEquals(testNode.search("g").longValue(), 5);
    }
    
    @Test
    public void testIsFull()
    {
        assertTrue(testNode.isFull());
        assertFalse(partialNode.isFull());
        assertTrue(testLeaf.isFull());
    }
    
    @Test
    public void testIsLeaf()
    {
        assertFalse(testNode.isLeaf());
        assertFalse(partialNode.isLeaf());
        assertTrue(testLeaf.isLeaf());
    }
    
    @Test
    public void testLeafSearch()
    {
       assertNull(testLeaf.search("a"));
       assertEquals(testLeaf.search("b").longValue(), 0);
       assertEquals(testLeaf.search("c").longValue(), 1);
       assertEquals(testLeaf.search("d").longValue(), 2);
       assertEquals(testLeaf.search("f").longValue(), 3);
       assertEquals(testLeaf.search("g").longValue(), 4);
    }
    
    @Test
    public void testLeafDelete()
    {
        testLeaf.delete("a");
        assertNull(testLeaf.search("a"));
        assertEquals(testLeaf.search("b").longValue(), 0);
        assertEquals(testLeaf.search("c").longValue(), 1);
        assertEquals(testLeaf.search("d").longValue(), 2);
        assertEquals(testLeaf.search("f").longValue(), 3);
        assertEquals(testLeaf.search("g").longValue(), 4);
        assertFalse(testLeaf.underflow());
        assertTrue(testLeaf.isFull());
        
        testLeaf.delete("b");
        assertNull(testLeaf.search("a"));
        assertNull(testLeaf.search("b"));
        assertEquals(testLeaf.search("c").longValue(), 1);
        assertEquals(testLeaf.search("d").longValue(), 2);
        assertEquals(testLeaf.search("f").longValue(), 3);
        assertEquals(testLeaf.search("g").longValue(), 4);
        assertFalse(testLeaf.isFull());
        assertFalse(testLeaf.underflow());
        
        testLeaf.delete("c");
        assertNull(testLeaf.search("a"));
        assertNull(testLeaf.search("b"));
        assertNull(testLeaf.search("c"));
        assertEquals(testLeaf.search("d").longValue(), 2);
        assertEquals(testLeaf.search("f").longValue(), 3);
        assertEquals(testLeaf.search("g").longValue(), 4);
        assertFalse(testLeaf.isFull());
        assertFalse(testLeaf.underflow());
        
        testLeaf.delete("g");
        assertNull(testLeaf.search("a"));
        assertNull(testLeaf.search("b"));
        assertNull(testLeaf.search("c"));
        assertEquals(testLeaf.search("d").longValue(), 2);
        assertEquals(testLeaf.search("f").longValue(), 3);
        assertNull(testLeaf.search("g"));
        assertFalse(testLeaf.isFull());
        assertTrue(testLeaf.underflow());
        
        testLeaf.delete("f");
        assertNull(testLeaf.search("a"));
        assertNull(testLeaf.search("b"));
        assertNull(testLeaf.search("c"));
        assertEquals(testLeaf.search("d").longValue(), 2);
        assertNull(testLeaf.search("f"));
        assertNull(testLeaf.search("g"));
        assertFalse(testLeaf.isFull());
        assertTrue(testLeaf.underflow());
        
        testLeaf.delete("d");
        assertNull(testLeaf.search("a"));
        assertNull(testLeaf.search("b"));
        assertNull(testLeaf.search("c"));
        assertNull(testLeaf.search("d"));
        assertNull(testLeaf.search("f"));
        assertNull(testLeaf.search("g"));
        assertFalse(testLeaf.isFull());
        assertTrue(testLeaf.underflow());
    }
    
    @Test
    public void testLeafInsert()
    {
        //Test when full
        testLeaf.insert("a", new Long(8));
        assertNull(testLeaf.search("a"));
        assertEquals(testLeaf.search("b").longValue(), 0);
        assertEquals(testLeaf.search("c").longValue(), 1);
        assertEquals(testLeaf.search("d").longValue(), 2);
        assertEquals(testLeaf.search("f").longValue(), 3);
        assertEquals(testLeaf.search("g").longValue(), 4);
        assertFalse(testLeaf.underflow());
        assertTrue(testLeaf.isFull());
        
        
        
        testLeaf = new BPlusLeaf<String, Long>(new String[5], 0, new Long[5], -1, -1);
        assertTrue(testLeaf.underflow());
        assertFalse(testLeaf.isFull());
        
        testLeaf.insert("b", new Long(0));
        assertEquals(testLeaf.search("b").longValue(), 0);
        assertTrue(testLeaf.underflow());
        assertFalse(testLeaf.isFull());
        
        testLeaf.insert("c", new Long(1));
        assertNull(testLeaf.search("a"));
        assertEquals(testLeaf.search("b").longValue(), 0);
        assertEquals(testLeaf.search("c").longValue(), 1);
        assertTrue(testLeaf.underflow());
        assertFalse(testLeaf.isFull());
        
        testLeaf.insert("g", new Long(4));
        assertNull(testLeaf.search("a"));
        assertEquals(testLeaf.search("b").longValue(), 0);
        assertEquals(testLeaf.search("c").longValue(), 1);
        assertEquals(testLeaf.search("g").longValue(), 4);
        assertFalse(testLeaf.underflow());
        assertFalse(testLeaf.isFull());
        
        testLeaf.insert("f", new Long(3));
        assertNull(testLeaf.search("a"));
        assertEquals(testLeaf.search("b").longValue(), 0);
        assertEquals(testLeaf.search("c").longValue(), 1);
        assertEquals(testLeaf.search("f").longValue(), 3);
        assertEquals(testLeaf.search("g").longValue(), 4);
        assertFalse(testLeaf.underflow());
        assertFalse(testLeaf.isFull());
        
        testLeaf.insert("d", new Long(2));
        assertNull(testLeaf.search("a"));
        assertEquals(testLeaf.search("b").longValue(), 0);
        assertEquals(testLeaf.search("c").longValue(), 1);
        assertEquals(testLeaf.search("d").longValue(), 2);
        assertEquals(testLeaf.search("f").longValue(), 3);
        assertEquals(testLeaf.search("g").longValue(), 4);
        assertFalse(testLeaf.underflow());
        assertTrue(testLeaf.isFull());
    }
    
    @Test
    public void testLeafUnderflowOdd()
    {
        String[] partialKeys = new String[4];
        Long[] partialOffsets = new Long[6];
        partialKeys[0] = "b";
        partialKeys[1] = "c";
        partialOffsets[0] = new Long(0);
        partialOffsets[1] = new Long(1);
        partialOffsets[2] = new Long(2);
        
        partialNode = new BPlusInternalNode<String>(partialKeys, 2, partialOffsets, 3);
        assertFalse(partialNode.underflow());
        partialKeys[1] = null;
        partialOffsets[2] = null;
        partialNode = new BPlusInternalNode<String>(partialKeys, 1, partialOffsets, 2);
        assertTrue(partialNode.underflow());
    }
    
    @Test
    public void testBTreeFile() throws OperationNotSupportedException, IOException
    {
        BPlusTreeFile<String, Long> bfile = new BPlusTreeFile<String, Long>(6, new BattingBPlusConverter());
        bfile.writeInternalNode(testNode, 0);
        BPlusInternalNode<String> internalNode = (BPlusInternalNode<String>)bfile.readNode(0);
        bfile = new BPlusTreeFile<String, Long>(6, new BattingBPlusConverter());
        bfile.writeLeaf( testLeaf, 0);
        BPlusLeaf<String, Long> leaf = (BPlusLeaf<String, Long>) bfile.readNode(0);
        long newLeaf = bfile.writeNewLeaf(testLeaf);
        long newNode = bfile.writeNewInternalNode(testNode);
        BPlusLeaf<String, Long> leaf1 = (BPlusLeaf<String, Long>)bfile.readNode(newLeaf);
        BPlusInternalNode<String> node = (BPlusInternalNode<String>)bfile.readNode(newNode);
        
        //checking out weird left/right issues
        testNode.setLeft(0);
        testNode.setRight(162);
        BPlusInternalNode<String> read = (BPlusInternalNode<String>)bfile.readNode(bfile.writeNewInternalNode( testNode ));
        
        testLeaf.setLeft(0);
        testLeaf.setRight(162);
        BPlusLeaf<String, Long> readLeaf = (BPlusLeaf<String, Long>) bfile.readNode(bfile.writeNewLeaf( testLeaf));
    }
}