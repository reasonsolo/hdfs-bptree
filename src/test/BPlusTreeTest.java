package test;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import trees.BPlusTree;
import util.BattingBPlusConverter;
import util.BattingBPlusRecord;

import static org.junit.Assert.*;

public class BPlusTreeTest
{
    BPlusTree<String, Long, BattingBPlusRecord> tree;
    
    @Before
    public void setUp() throws FileNotFoundException, IOException
    {
        //tree = new BPlusTree<String, Long, BattingBPlusRecord>(4, new BattingBPlusConverter(), null);
        tree = new BPlusTree<String, Long, BattingBPlusRecord>(4, 
                new BattingBPlusConverter(), null, "BPlusTree.bin", null, null);
    }
    
    @Test
    public void testInsert() throws IOException
    {
        tree.insert( new BattingBPlusRecord("a", new Long(0)));
        tree.printTree();
        System.out.println();
        System.out.println();
        int i = 1;
        for (char ch = 'h'; ch > 'a'; ch--) //this loop causes some splits of leaf nodes.
        {
            tree.insert(new BattingBPlusRecord("" + ch, new Long(i++)));
            tree.printTree();
            System.out.println();
        }
        tree.printTree();
        for (char ch = 'i'; ch < 'o'; ch++)
        {
            System.out.println("BEFORE INSERTING: " + ch);
            tree.insert(new BattingBPlusRecord("" + ch, new Long(i++)));
            tree.printTree();
        }
        for (int j = 0; j < 7; j++)
        {
            System.out.println("BEFORE INSERTING: a" + j);
            tree.insert(new BattingBPlusRecord("a" + j, new Long(i++)));
            tree.printTree();
        }
        assertNull(tree.search( "a7" ));
        assertEquals(tree.search("a1"), new Long(15));
        assertEquals(tree.search("c"), new Long(6));
    }
    
    @Test
    public void testDelete() throws IOException
    {
        testInsert();
        tree.remove(new BattingBPlusRecord("a", null));
        tree.printTree();
        System.out.println("DELETING A4\n\n");
        tree.remove(new BattingBPlusRecord("a4", null));
        tree.printTree();
        System.out.println("DELETING A2\n\n");
        tree.remove(new BattingBPlusRecord("a2", null));
        tree.printTree();
        System.out.println("DELETING B\n\n");
        tree.remove(new BattingBPlusRecord("b", null));
        tree.printTree();
    }
    
    @Test
    public void testDeleteRoot() throws IOException
    {
        System.out.println("DELETING ROOT\n\n");
        tree.insert( new BattingBPlusRecord("a", new Long(0)));
        int i = 1;
        for (char ch = 'h'; ch > 'a'; ch--) //this loop causes some splits of leaf nodes.
        {
            tree.insert(new BattingBPlusRecord("" + ch, new Long(i++)));
        }
        tree.printTree();
        for (char ch = 'h'; ch >= 'a'; ch--)
        {
            System.out.println("\nDeleting: " + ch + "\n");
            tree.remove(new BattingBPlusRecord(ch + "", null));
            tree.printTree();
        }
    }
    
    @Test
    public void testSync() throws IOException 
    {
       
    }
}
