package util;

public class ModifiedBinarySearcher<ElementType extends Comparable<? super ElementType>>
{

    private ElementType[] elements;
    public ModifiedBinarySearcher(ElementType[] elements)
    {
        this.elements = elements;
    }
    
    
    public int findIndexOfNextGreatest(ElementType key)
    {
        return findIndexOfNextGreatest(key, 0, elements.length - 1);
    }

    /**
     * The index we're searching for is that of the largest
     * key that is <= the target key, because then the
     * right pointer will point to all values >= to the 
     * target key
     * @param key
     * @param left
     * @param right
     * @return
     */
    public int findIndexOfNextGreatest(ElementType key, int left, int right)
    {
      if (right < 0)
      {
          return -1;
      }
      ElementType median = elements[(left + right) / 2];
      ElementType rangeCheck;
      if (median.compareTo(key) < 0)
      {
          if ((left + right) / 2 + 1 > right) // if the key is greater than anything in the node, we just return the last index.
          {
              return right;
          }
          rangeCheck = elements[(left + right)/2 + 1];
          //If they're equal, the rangeCheck is the best choice
          if (rangeCheck.compareTo(key) == 0)
          {
              return (left + right) / 2 + 1;
          }
          //If the rangeCheck is greater, but the median is less,
          // then the target is between the median and the rangecheck,
          //which means the median is our best bet
          else if (rangeCheck.compareTo(key) > 0)
          {
              return (left + right) / 2;
          }
          //if the comparison is that the rangeCheck < key, the search will work fine
          else
          {
              return findIndexOfNextGreatest(key, (left + right)/ 2 + 1, right);
          }
      }
      else if (median.compareTo(key) == 0)
      {
          return (left + right) / 2;
      }
      else // the median is greater than the key
      {
          if ((left + right) / 2 - 1 < left) // if the key is less than anything in the node, we need to return an index such that the key is less than everything in the list.
          {
              return -1;
          }
          rangeCheck = elements[(left + right)/2 - 1];
          //If the rangecheck is equal, then it's obviously correct
          if (rangeCheck.compareTo(key) == 0)
          {
              return (left + right) / 2 - 1;
          }
          // if the rangeCheck is less than the key, but
          // the median is not, then the rangeCheck is the greatest
          // item that is less than the key
          else if (rangeCheck.compareTo(key) < 0)
          {
              return (left + right) / 2 - 1;
          }
          //If the rangeCheck and the median are greater than the key
          //binary search the left half
          else
          {
              return findIndexOfNextGreatest(key, left, (left + right)/ 2 - 1);
          }
      }
    }
}
