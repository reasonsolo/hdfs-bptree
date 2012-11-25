package util;

public class Utility
{
    public static int indexOf(Object[] arr, Object key)
    {
        for (int i = 0; i < arr.length; i++)
        {
            if (arr[i] != null && arr[i].equals(key))
            {
                return i;
            }
        }
        return -1;
    }

}
