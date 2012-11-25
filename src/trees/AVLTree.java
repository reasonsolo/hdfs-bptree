package trees;

import java.io.BufferedWriter;
import java.io.IOException;

import trees.AVLNode.BalanceFactor;

/**
 * This is currently a BST.  The implementation will change to become an AVL Tree.
 * @author bfults (Brian Fults - 905084698)
 * @param <Type> Any comparable item can be inserted into a BST.
 *
 */
public class AVLTree <Type extends Comparable<? super Type>>
{
    
    private AVLNode<Type> root;
    private BufferedWriter logger = null;
    
    /**
     * We may declare a logger for an index.
     * @param logger
     */
    public AVLTree(BufferedWriter logger)
    {
        this.setLogger( logger );
    }
    
    /**
     * Creates an empty tree.
     */
    public AVLTree()
    {
        root = null;
    }
    
    /**
     * 
     */
    
    /**
     * Determines whether the tree contains any nodes.
     * @return true if the tree does not have nodes
     */
    public boolean isEmpty()
    {
        return root == null;
    }
    
    /**
     * Searches the tree for a particular element
     * @param toFind
     * @return true if the element is found
     */
    public boolean contains(Type toFind)
    {
        return contains(toFind, root);
    }

    /**
     * Returns a reference to an object based on its key.
     * The difference between this and find is that it does not
     * log visited nodes on insertion.
     * @param toFind
     * @return the object, null if not found.
     * @throws IOException 
     */
    public Type silentFind(Type toFind)
    {
        return silentFind(toFind, root);
    }
    
    /**
     * Returns a reference to an object based on its key.
     * The difference between this and insertFind is that it
     * logs nodes visited on insertion.
     * @param toFind
     * @return the object, null if not found.
     * @throws IOException 
     */
    public Type find(Type toFind) throws IOException
    {
        /*
         * If we have no logger, we can only do a silent find.
         */
        if (logger == null)
        {
            return silentFind(toFind, root);
        }
        logger.write("Searching for: " + toFind.toString() + "\n\n");
        return find(toFind, root);
    }
    
    
    
    /**
     * Returns a reference to the element of the minimum node. 
     * @return the smallest node's value
     */
    public Type findMin()
    {
        if (isEmpty())
            throw new RuntimeException("Tree is empty, cannot search for a minimum.");
        
        return findMin(root).element;
    }
    
    /**
     * Adds an item to the tree
     * @param toInsert the item to be inserted
     */
    public void insert(Type toInsert)
    {
        root = insert(toInsert, root);
    }
    
    /**
     * Deletes an item from the tree
     * @param toRemove the item to be deleted
     */
    public void remove(Type toRemove)
    {
        root = remove(toRemove, root);
    }
    
    /**
     * Empties the tree.
     */
    public void clear()
    {
        root = null;
    }
    
    
    /**
     * Prints information about each node's position in the tree.
     */
    public void printTreeNodes()
    {
        if (isEmpty())
        {
            System.out.println("Tree is empty");
        }
        else
        {
            printNodes(root, 0);
        }
    }
    
    /**
     * Prints the tree.
     */
    public void printTree()
    {
        if(isEmpty())
        {
            System.out.println("Tree is empty.");
        }
        else
        {
            printTree(root, 0);
        }
    }
    
    /**
     * Logs information about the tree in the given log file.
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
            logger.write("Structure is empty.");
        }
        logTree(root, 0);
    }
    
    /**
     * Returns the maximum value node in the tree.
     * @return the largest node's value
     */
    public Type findMax()
    {
        if (isEmpty())
            throw new RuntimeException("Tree is empty, cannot search for a minimum.");
        
        return findMax(root).element;
    }
    
    
    //Begin private methods
    /**
     * Helper method to print the tree.  Should print
     * each node with a tab preceding for each level down it is.
     */
    private void printTree(AVLNode<Type> subRoot, int depth)
    {
        if (subRoot != null)
        {
            printTree(subRoot.right, depth + 1);
            subRoot.printElement( depth );
            printTree(subRoot.left, depth + 1);
        }
    }
    
    /**
     * Creates a string representation of the structure.
     * @param subRoot
     * @param depth
     * @param toReturn
     * @throws IOException 
     */
    private void logTree(AVLNode<Type> subRoot, int depth) throws IOException
    {
        if (subRoot != null)
        {
            logTree(subRoot.right, depth + 1);
            logger.write(subRoot.toString(depth));
            logTree(subRoot.left, depth + 1);
        }
    }
    
    //Begin private methods
    /**
     * Helper method to print the tree.  Should print
     * each node with a tab preceding for each level down it is,
     * as well as information about the node's balance factor.
     */
    private void printNodes(AVLNode<Type> subRoot, int depth)
    {
        if (subRoot != null)
        {
            printNodes(subRoot.right, depth + 1);
            subRoot.printNode( depth );
            printNodes(subRoot.left, depth + 1);
        }
    }
    
    
  
    /**
     * Helper method for insert
     * @param toInsert the item to be inserted
     * @param subRoot the subtree where the item should be inserted
     * @return
     */
    private AVLNode<Type> insert(Type toInsert, AVLNode<Type> subRoot)
    {
        if (subRoot == null)
        {
            AVLNode<Type> newNode = new AVLNode<Type>(toInsert);
            newNode.changed = true;
            return newNode;
        }
        int comparison = toInsert.compareTo( subRoot.element );
        
        if (comparison < 0)
        {
           subRoot.left = insert(toInsert, subRoot.left);
           
           if (subRoot.left.changed)
           {
               subRoot.balanceFactor = BalanceFactor.increaseLeft(subRoot.balanceFactor);
               subRoot.left.changed = false;
               if (subRoot.balanceFactor != BalanceFactor.EQUAL)
                   subRoot.changed = true;
           }
           if (subRoot.balanceFactor == BalanceFactor.DOUBLE_LEFT_HIGH)
           {
               if (toInsert.compareTo( subRoot.left.element ) < 0)
                   subRoot = singleLeftRotate(subRoot);
               else
                   subRoot = doubleLeftInsert(subRoot);
           }

        }
        else if (comparison > 0)
        {
            subRoot.right = insert(toInsert, subRoot.right);
            if (subRoot.right.changed)
            {
                subRoot.balanceFactor = BalanceFactor.increaseRight(subRoot.balanceFactor);
                subRoot.right.changed = false;
                if (subRoot.balanceFactor != BalanceFactor.EQUAL)
                    subRoot.changed = true;
            }
            if (subRoot.balanceFactor == BalanceFactor.DOUBLE_RIGHT_HIGH)
            {
                if (toInsert.compareTo(subRoot.right.element) > 0)
                    subRoot = singleRightRotate(subRoot);
                else
                    subRoot = doubleRightInsert(subRoot);
            }
            
        }
        else
        {
            duplicate(toInsert, subRoot);
        }
        
        return subRoot;
    }
    
    /**
     * This helper method should handle the rotations and balance factors in the case
     * of a Double Right High tree after a left deletion.
     * @param formerRoot
     * @return
     */
    private AVLNode<Type> rotateAfterLeftDeletion(AVLNode<Type> formerRoot)
    {
        AVLNode<Type> toReturn;
        BalanceFactor problemFactor;
        if (formerRoot.right.balanceFactor != BalanceFactor.LEFT_HIGH)
        {
            problemFactor = formerRoot.right.balanceFactor;
            toReturn = rotateWithRightChild(formerRoot);
            if (problemFactor == BalanceFactor.RIGHT_HIGH)
            {
                toReturn.balanceFactor = BalanceFactor.EQUAL;
                toReturn.left.balanceFactor = BalanceFactor.EQUAL;
            }
            else //balance factor is equal
            {
                toReturn.balanceFactor = BalanceFactor.LEFT_HIGH;
                toReturn.left.balanceFactor = BalanceFactor.RIGHT_HIGH;
            }
        }
        else //root.right is left high
        {
            problemFactor = formerRoot.right.left.balanceFactor;
            toReturn = doubleWithRightChild(formerRoot);
            if (problemFactor == BalanceFactor.LEFT_HIGH)
            {
                toReturn.left.balanceFactor = BalanceFactor.EQUAL;
                toReturn.balanceFactor = BalanceFactor.EQUAL;
                toReturn.right.balanceFactor = BalanceFactor.RIGHT_HIGH;
            }
            else if (problemFactor == BalanceFactor.RIGHT_HIGH)
            {
                toReturn.left.balanceFactor = BalanceFactor.LEFT_HIGH;
                toReturn.balanceFactor = BalanceFactor.EQUAL;
                toReturn.right.balanceFactor = BalanceFactor.EQUAL;
            }
            else //problem factor is equal
            {
                toReturn.balanceFactor = BalanceFactor.EQUAL;
                toReturn.left.balanceFactor = BalanceFactor.EQUAL;
                toReturn.right.balanceFactor = BalanceFactor.EQUAL;
            }
        }
        toReturn.changed = true;
        return toReturn;
    }
    
    /**
     * This helper method should handle the rotations and balance factors in the case
     * of a Double Left High Tree after a right deletion.
     * @param formerRoot
     * @return
     */
    private AVLNode<Type> rotateAfterRightDeletion(AVLNode<Type> formerRoot)
    {
        AVLNode<Type> toReturn;
        BalanceFactor problemFactor;
        if (formerRoot.left.balanceFactor != BalanceFactor.RIGHT_HIGH)
        {
            problemFactor = formerRoot.left.balanceFactor;
            toReturn = rotateWithLeftChild(formerRoot);
            if (problemFactor == BalanceFactor.LEFT_HIGH)
            {
                toReturn.balanceFactor = BalanceFactor.EQUAL;
                toReturn.right.balanceFactor = BalanceFactor.EQUAL;
            }
            else // balance factor of left is equal
            {
                toReturn.balanceFactor = BalanceFactor.RIGHT_HIGH;
                toReturn.right.balanceFactor = BalanceFactor.LEFT_HIGH;
            }

        }
        else
        {
            problemFactor = formerRoot.left.right.balanceFactor;
            toReturn = doubleWithLeftChild(formerRoot);
            if (problemFactor == BalanceFactor.LEFT_HIGH)
            {
                toReturn.left.balanceFactor = BalanceFactor.EQUAL;
                toReturn.balanceFactor = BalanceFactor.EQUAL;
                toReturn.right.balanceFactor = BalanceFactor.RIGHT_HIGH;
            }
            else if (problemFactor == BalanceFactor.RIGHT_HIGH)
            {
                toReturn.left.balanceFactor = BalanceFactor.LEFT_HIGH;
                toReturn.balanceFactor = BalanceFactor.EQUAL;
                toReturn.right.balanceFactor = BalanceFactor.EQUAL;
            }
            else //problem factor is equal
            {
                toReturn.balanceFactor = toReturn.left.balanceFactor = toReturn.right.balanceFactor = BalanceFactor.EQUAL;
            }
        }
        toReturn.changed = true;
        return toReturn;
    }
    
    /**
     * Performs a rotation when the left is too high.
     * @param formerRoot
     * @return
     */
    private AVLNode<Type> rotateWithLeftChild(AVLNode<Type> formerRoot)
    {
        AVLNode<Type> newRoot = formerRoot.left;
        formerRoot.left = newRoot.right;
        newRoot.right = formerRoot;
        formerRoot.changed = false;
        newRoot.changed = false;
        return newRoot;
    }
    
    /**
     * Called when an insert requires a single left rotate.
     * Updates balance factors for this situation.
     * @param formerRoot
     * @return
     */
    private AVLNode<Type> singleLeftRotate(AVLNode<Type> formerRoot)
    {
        AVLNode<Type> toReturn = rotateWithLeftChild(formerRoot);
        toReturn.balanceFactor = BalanceFactor.EQUAL;
        toReturn.right.balanceFactor = BalanceFactor.EQUAL;
        return toReturn;
    }
    
    
    /**
     * A helper method - calls rotations for a doubleWithLeftChild,
     * then updates balance factors based on the state of the nodes before the rotations.
     * @param formerRoot
     * @return
     */
    private AVLNode<Type> doubleLeftInsert(AVLNode<Type> formerRoot)
    {
        BalanceFactor problemChildBalance = formerRoot.left.right.balanceFactor;
        AVLNode<Type> toReturn = doubleWithLeftChild(formerRoot);
        if (problemChildBalance == BalanceFactor.RIGHT_HIGH)
        {
            toReturn.balanceFactor = BalanceFactor.EQUAL; //added
            toReturn.right.balanceFactor = BalanceFactor.EQUAL;
            toReturn.left.balanceFactor = BalanceFactor.LEFT_HIGH;
        }
        else if (problemChildBalance == BalanceFactor.LEFT_HIGH)
        {
            //already swapped these - if we get new mismatches, it seems we can't assume this.
            toReturn.balanceFactor = BalanceFactor.EQUAL; //added
            toReturn.right.balanceFactor = BalanceFactor.RIGHT_HIGH;
            toReturn.left.balanceFactor = BalanceFactor.EQUAL;
        }
        else
        {
            toReturn.balanceFactor = BalanceFactor.EQUAL;
            toReturn.left.balanceFactor = BalanceFactor.EQUAL;
            toReturn.right.balanceFactor = BalanceFactor.EQUAL;
        }
        return toReturn;
    }
    
    /**
     * Performs a double rotation when the left is too high.
     */
    private AVLNode<Type> doubleWithLeftChild(AVLNode<Type> formerRoot)
    {
        formerRoot.left = rotateWithRightChild(formerRoot.left);
        return rotateWithLeftChild(formerRoot);
        
    }
    
    /**
     * Performs a rotation when the left is too high.
     * @param formerRoot
     * @return
     */
    private AVLNode<Type> rotateWithRightChild(AVLNode<Type> formerRoot)
    {
               
        AVLNode<Type> newRoot = formerRoot.right;
        formerRoot.right = newRoot.left;
        newRoot.left = formerRoot;
  
        
        /*formerRoot.balanceFactor =  BalanceFactor.EQUAL;
        newRoot.balanceFactor = BalanceFactor.EQUAL; */
        formerRoot.changed = false;
        newRoot.changed = false;
        return newRoot;
    }
    
    /**
     * Performs the rotation and updates balance factors
     * when an insert requires.
     * @param formerRoot
     * @return
     */
    private AVLNode<Type> singleRightRotate(AVLNode<Type> formerRoot)
    {
        AVLNode<Type> toReturn = rotateWithRightChild(formerRoot);
        toReturn.balanceFactor = BalanceFactor.EQUAL;
        toReturn.left.balanceFactor = BalanceFactor.EQUAL;
        return toReturn;
    }
    
    /**
     * A helper method - calls the appropriate rotations, then 
     * applies balance factors based on the state of the nodes before
     * the rotations.
     * @param formerRoot
     * @return
     */
    private AVLNode<Type> doubleRightInsert(AVLNode<Type> formerRoot)
    {
        BalanceFactor problemChildBalance = formerRoot.right.left.balanceFactor;
        AVLNode<Type> toReturn = doubleWithRightChild(formerRoot);
        if (problemChildBalance == BalanceFactor.RIGHT_HIGH)
        {
            toReturn.balanceFactor = BalanceFactor.EQUAL; //added
            toReturn.left.balanceFactor = BalanceFactor.LEFT_HIGH;
            toReturn.right.balanceFactor = BalanceFactor.EQUAL;
        }
        else if (problemChildBalance == BalanceFactor.LEFT_HIGH)
        {
            toReturn.balanceFactor = BalanceFactor.EQUAL; //added
            toReturn.left.balanceFactor = BalanceFactor.EQUAL;
            toReturn.right.balanceFactor = BalanceFactor.RIGHT_HIGH;
        }
        else
        {
            toReturn.balanceFactor = BalanceFactor.EQUAL; //added
            toReturn.left.balanceFactor = BalanceFactor.EQUAL;
            toReturn.right.balanceFactor = BalanceFactor.EQUAL;
        }
        return toReturn;
    }
    
    /**
     * Performs a double rotation when the left is too high.
     */
    private AVLNode<Type> doubleWithRightChild(AVLNode<Type> formerRoot)
    {

        formerRoot.right = rotateWithLeftChild( formerRoot.right );
        return rotateWithRightChild(formerRoot);
    }
    
    /**
     * This method is used to perform particular actions if an equal item is found.
     * Override with desired behavior.
     * @param toInsert
     * @param subRoot
     */
    protected void duplicate(Type toInsert, AVLNode<Type> subRoot)
    {
        //nothing to do for a regular AVL
    }
    
    /**
     * Helper method for deletion.
     * @param toRemove the item to delete
     * @param subRoot the subroot from which the item should be deleted
     * @return the new subroot
     */
    private AVLNode<Type> remove(Type toRemove, AVLNode<Type> subRoot)
    {
        if (subRoot == null)
        {
            return subRoot;
        }
        
        int comparison = toRemove.compareTo(subRoot.element);
        if (comparison < 0)
        {
            boolean nullBefore = subRoot.left == null;
            subRoot.left = remove(toRemove, subRoot.left);
            if (subRoot.left == null && !nullBefore || (subRoot.left != null && subRoot.left.changed))
            {
                if (subRoot.left != null) 
                    subRoot.left.changed = false;
                if (subRoot.left == null || subRoot.left.balanceFactor == BalanceFactor.EQUAL)
                {
                    subRoot.balanceFactor = BalanceFactor.increaseRight(subRoot.balanceFactor);
                    subRoot.changed = subRoot.balanceFactor == BalanceFactor.EQUAL;
                }
                if (subRoot.balanceFactor == BalanceFactor.DOUBLE_RIGHT_HIGH)
                {
                    subRoot = rotateAfterLeftDeletion(subRoot);
                }
            }
        }
        else if (comparison > 0)
        {
            boolean nullBefore = subRoot.right == null;
            subRoot.right = remove(toRemove, subRoot.right);
            if (subRoot.right == null && !nullBefore || (subRoot.right != null && subRoot.right.changed))
            {
                if (subRoot.right != null)
                    subRoot.right.changed = false;
                if (subRoot.right == null || subRoot.right.balanceFactor == BalanceFactor.EQUAL)
                {
                    subRoot.balanceFactor = BalanceFactor.increaseLeft(subRoot.balanceFactor); // changed from left to right
                    subRoot.changed = subRoot.balanceFactor == BalanceFactor.EQUAL;
                }
                if (subRoot.balanceFactor == BalanceFactor.DOUBLE_LEFT_HIGH)
                {
                    subRoot = rotateAfterRightDeletion(subRoot);
                }
                
            }
        }
        else if(subRoot.left != null && subRoot.right != null)
        {
            subRoot.element = findMin(subRoot.right).element;
            subRoot.right = this.remove(subRoot.element, subRoot.right);
            subRoot.changed = false; // necessary?
            if (subRoot.right == null || subRoot.right.changed && subRoot.right.balanceFactor == BalanceFactor.EQUAL)
            {
                subRoot.balanceFactor = BalanceFactor.increaseLeft(subRoot.balanceFactor);
                subRoot.changed = subRoot.balanceFactor == BalanceFactor.EQUAL;//has to do with this
            }
            
            
        }
        else if(subRoot.left != null)
        {
            subRoot = subRoot.left;
            subRoot.changed = true;
        }
        else if(subRoot.right != null)
        {
            subRoot = subRoot.right;
            subRoot.changed = true;
        }
        else
        {
            subRoot = null;
        }
        return subRoot;
    }
    
    /**
     * Helper method for contains
     * @param toFind item to search for
     * @param subRoot subtree in which toFind should reside, if it exists in the tree at all
     * @return
     */
    private boolean contains(Type toFind, AVLNode<Type> subRoot)
    {
        if (subRoot == null)
        {
            return false;
        }
        
        int comparison = toFind.compareTo( subRoot.element );
        
        if (comparison < 0)
        {
            return contains(toFind, subRoot.left);
        }
        else if (comparison > 0)
        {
            return contains(toFind, subRoot.right);
        }
        return true;
    }
    
    /**
     * Private method for find.  Searches for the element (compares using
     * compareTo for equality) and returns it.
     * @param toFind
     * @param subRoot
     * @return the element within the index.
     * @throws IOException 
     */
    private Type find(Type toFind, AVLNode<Type> subRoot) throws IOException
    {
        if (subRoot == null)
        {
            logger.write(toFind.toString() + " not found!\n\n");
            return null;
        }
        logger.write("Visiting node:" + subRoot.toString() + "\n");
        int comparison = toFind.compareTo(subRoot.element);
        if (comparison < 0)
        {
            return find(toFind, subRoot.left);
        }
        else if (comparison > 0)
        {
            return find(toFind, subRoot.right);
        }
        logger.write("Item found!\n\n");
        return subRoot.element;   
    }
    
    /**
     * Private method for find.  Searches for the element (compares using
     * compareTo for equality) and returns it.  No logging.
     * @param toFind
     * @param subRoot
     * @return the element within the index.
     */
    private Type silentFind(Type toFind, AVLNode<Type> subRoot)
    {
        if (subRoot == null)
        {
            return null;
        }
        
        int comparison = toFind.compareTo(subRoot.element);
        if (comparison < 0)
        {
            return silentFind(toFind, subRoot.left);
        }
        else if (comparison > 0)
        {
            return silentFind(toFind, subRoot.right);
        }
        return subRoot.element;   
    }
    
    /**
     * Helper method to find the minimum
     * @param subRoot subTree that holds the smallest value
     * @return
     */
    private AVLNode<Type> findMin(AVLNode<Type> subRoot)
    {
        if (subRoot.left != null)
            return findMin(subRoot.left);
        return subRoot;
    }
    
    /**
     * Helper method to find the maximum
     * @param subRoot subTree that holds the largest value
     * @return the maximum value in this subtree
     */
    protected AVLNode<Type> findMax(AVLNode<Type> subRoot)
    {
        if (subRoot.right != null)
            return findMax(subRoot.right);
        return subRoot;
    }

    /**
     * Setter for the logger - the tree uses this
     * to record information about searches and to
     * print its structure.
     * @param logger a BufferedWriter to write to
     */
    public void setLogger( BufferedWriter logger )
    {
        this.logger = logger;
    }
}
