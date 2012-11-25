package test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import trees.AVLTree;

public class TreeTest
{

    AVLTree<String> tree;
    AVLTree<Integer> intTree;
    @Before
    public void setUp()
    {
        tree = new AVLTree<String>();
        intTree = new AVLTree<Integer>();
    }
    
    @Test
    public void testInsert()
    {
        tree.insert( "B" );
        tree.insert( "A" );
        tree.insert("C");
        tree.insert("F");
        tree.insert("H");
        tree.printTree();
        System.out.println("\n\n");
    }
    
    @Test
    public void testRemove()
    {
        tree.remove("Z");
        tree.printTree();
        tree.insert( "B" );
        tree.insert( "A" );
        tree.insert("C");
        tree.insert("F");
        tree.insert("H");
        tree.printTree();
        tree.insert( "D" );
        tree.printTree();
        tree.insert("E");
        tree.printTree();
        tree.remove("D");
        tree.printTreeNodes();
        System.out.println("\n\n");
    }
    
    @Test
    public void testRemoveRoot()
    {
        tree.insert( "B" );
        tree.insert( "A" );
        tree.insert("C");
        tree.insert("F");
        tree.insert("H");
        tree.printTree();
        tree.remove("B");
        System.out.println("After removing");
        tree.printTreeNodes();
        System.out.println("\n\n");
    }
    
    @Test
    public void testSingleLeftRotation()
    {
        tree.insert("5");
        tree.insert("6");
        tree.insert("3");
        tree.insert("2");
        tree.insert("4");
        tree.printTreeNodes();
        tree.insert("1"); //should cause autobalance issues
        tree.printTreeNodes();
        System.out.println("\n\n");
    }
    
    @Test
    public void testSingleRightRotation()
    {
        tree.insert("2");
        tree.insert("1");
        tree.insert("3");
        tree.insert("4");
        tree.insert("5");
        tree.printTreeNodes();
        System.out.println("\nBefore inserting");
        tree.insert("6");
        tree.printTreeNodes();
        System.out.println("\n\n");
    }
    
    @Test
    public void testDoubleLeftRotation()
    {
        intTree.insert(4);
        intTree.insert(2);
        intTree.insert(6);
        intTree.insert(1);
        intTree.insert(3);
        intTree.insert(5);
        intTree.insert(7);
        intTree.insert(16);
        intTree.printTreeNodes();
        System.out.println("\nBefore inserting");
        intTree.insert(15);
        intTree.printTreeNodes();
        System.out.println("\n\n");
    }
    
    @Test
    public void testDoubleRightRotation()
    {
        intTree.insert(7);
        intTree.insert(4);
        intTree.insert(13);
        intTree.insert(2);
        intTree.insert(6);
        intTree.insert(11);
        intTree.insert(15);
        intTree.insert(1);
        intTree.insert(3);
        intTree.insert(5);
        intTree.insert(10);
        intTree.insert(12);
        intTree.insert(14);
        intTree.insert(16);
        intTree.insert(8);
        System.out.println("Before inserting 9: ");
        intTree.printTreeNodes();
        System.out.println("\nAfter inserting 9: ");
        intTree.insert(9);
        intTree.printTreeNodes();
        System.out.println("\n\n\n\n\n\n");
    }
    
    @Test
    public void testToString()
    {
        tree.insert( "B" );
        tree.insert( "A" );
        tree.insert("C");
        tree.insert("F");
        tree.insert("H");
        System.out.println(tree.toString());
    }
    
    @Test public void testLargeAscending()
    {
        for (Integer toIn = 1; toIn <= 40; toIn++)
        {
            System.out.println("\n\n");
            System.out.println("Inserting: " + toIn.toString());
            intTree.insert(toIn);
            intTree.printTreeNodes();
        }
        
    }
    
    @Test public void testLargeDescending()
    {
        for (Integer toIn = 40; toIn >= 1; toIn--)
        {
            intTree.insert(toIn);
            System.out.println("\n\n");
            System.out.println("Inserting: " + toIn.toString());
            intTree.printTreeNodes();
        }
    }
    
    @Test public void testMixUp()
    {
        testLargeDescending();
        intTree.printTreeNodes();
        for (Integer n = 100; n < 500; n++)
        {
            System.out.println("\n\nInserting: " + n + "\n");
            intTree.insert(n);
            intTree.printTreeNodes();
        }
        for (Integer n = 99; n > 40; n--)
        {
            System.out.println("\n\nInserting: " + n + "\n");
            intTree.insert(n);
            intTree.printTreeNodes();
        }
        intTree.printTreeNodes();
    }
    
    @Test
    public void testRemoves()
    {
        for (Integer n = 0; n < 20; n++)
        {
            intTree.insert(n);
        }
        System.out.println("\n\nAfter insertions:\n");
        intTree.printTreeNodes();
        System.out.println("\nRemoving root.\n");
        intTree.remove(7);
        intTree.printTreeNodes();
        System.out.println("Removing 11: \n");
        intTree.remove(11);
        intTree.printTreeNodes();
        System.out.println("Removing 16: \n");
        intTree.remove(16);
        intTree.printTreeNodes();
        System.out.println("Removing 18: \n");
        intTree.remove(18);
        intTree.printTreeNodes();
        System.out.println("Removing 17: \n");
        intTree.remove(17);
        intTree.printTreeNodes();
        System.out.println("Removing 1: \n");
        intTree.remove(1);
        intTree.printTreeNodes();
        System.out.println("Removing 4: \n");
        intTree.remove(4);
        intTree.printTreeNodes();
        System.out.println("Removing 6: \n");
        intTree.remove(6);
        intTree.printTreeNodes();
        System.out.println("Removing 2: \n");
        intTree.remove(2);
        intTree.printTreeNodes();
        System.out.println("Removing 19: \n");
        intTree.remove(19);
        intTree.printTreeNodes();
    }
}

    
