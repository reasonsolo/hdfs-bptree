package util;

import javax.naming.OperationNotSupportedException;

/**
 * A convertible key can be converted from a binary format to a particular type.
 * @author xclite
 *
 */
public abstract class ConvertibleKey<KeyType>
{
    /**
     * A convertible record provides a Converter object
     * which does the work of converting the byte array
     * to the desired type.
     * @return
     * @throws OperationNotSupportedException 
     */
    public static Converter converter() throws OperationNotSupportedException
    {
        throw new OperationNotSupportedException("OPERATION NOT IMPLEMENTED");
    }
    
}
