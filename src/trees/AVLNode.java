package trees;

import javax.swing.JOptionPane;

/**
 * This class models a BinaryNode within a BinarySearchTree
 * @author bfults (Brian Fults - 905084698)
 * @param <Type>  A generic type - this node may hold any sort of object.
 */
public class AVLNode<Type>
{
    /**
     * The balance factor represents the state of the 
     * tree.
     * @author bfults (Brian Fults - 905084698)
     *
     */
    public enum BalanceFactor
    {
        EQUAL(0),
        LEFT_HIGH(-1),
        DOUBLE_LEFT_HIGH(-2),
        RIGHT_HIGH(1),
        DOUBLE_RIGHT_HIGH(2);

        private final int value; //internal representation of a balance factor
        private BalanceFactor(int value)
        {
            this.value = value;
        }
        
        public static BalanceFactor toFactor(int balanceValue)
        {
            for (BalanceFactor factor: BalanceFactor.values())
            {
                if (factor.value == balanceValue)
                    return factor;
            }
            return null;
        }
       
        public static BalanceFactor increaseLeft( BalanceFactor balanceFactor )
        {
            return toFactor(balanceFactor.value - 1);
        }

        public static BalanceFactor increaseRight( BalanceFactor balanceFactor )
        {
            return toFactor(balanceFactor.value + 1);
        }
    }
    
    Type element;
    AVLNode<Type> left;
    AVLNode<Type> right;
    BalanceFactor balanceFactor;
    boolean changed;
    
    
    /**
     * This constructor is used to create a node with no children.
     * @param element new item to be stored
     */
    public AVLNode(Type element)
    {
        this(element, null, null);
    }
    
    /**
     * This constructor creates a node with links to the given children.
     * @param element
     * @param left
     * @param right
     */
    public AVLNode(Type element, AVLNode<Type> left, AVLNode<Type> right)
    {
        this.element = element;
        this.left = left;
        this.right = right;
        this.balanceFactor = BalanceFactor.EQUAL;
        this.changed = false;
    }
    
    /**
     * Prints the node, with a tabbed depth.
     * @param depth number of tabs to indent.
     */
    public void printElement(int depth)
    {
        for (int i = 0; i < depth; i++)
        {
            System.out.print("\t");
        }
        System.out.println(element);
    }
    
    /**
     * Prints the node as well as information about
     * the balance factor.
     * @param depth number of tabs to indent.
     */
    public void printNode(int depth)
    {
        for (int i = 0; i < depth; i++)
        {
            System.out.print("\t");
        }
        System.out.print(balanceFactor + ":" + element + "\n");
    }
    
    /**
     * Creates a string representation of only this node.
     */
    public String toString()
    {
        return element.toString();
    }
    /**
     * Creates a string representation of the tree.
     * @param depth
     * @return a string describing the tree
     */
    public String toString (int depth)
    {
        String toReturn = "";
        for (int i = 0; i < depth; i ++)
        {
            toReturn += "\t";
        }
        toReturn += balanceFactor.toString() + ":" + element.toString() + "\n";
        return toReturn;
    }
    
    /**
     * Returns the maximum height of this node.
     * @return the height of the tree
     */
    public int height()
    {
        if (left != null)
        {
            if (right != null)
            {
                return Math.max(left.height(), right.height()) + 1;
            }
            return left.height() + 1;
        }
        else if (right != null)
        {
            return right.height() + 1;
        }
        return 0;
    }
}