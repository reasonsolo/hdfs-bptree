package util;

/**
 * A record is an id, a name, and the line from which
 * they were parsed.
 * @author bfults (Brian Fults - 905084698)
 *
 */
public class Record
{
    private final String id, name, line;
    private long offset;
    
    public Record(String id, String name, String line)
    {
        this.id = id;
        this.name = name;
        this.line = line;
    }
    
    public String getId()
    {
        return this.id;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public String getLine()
    {
        return this.line;
    }

    public void setOffset(long offset )
    {
        this.offset = offset;
    }

    public long getOffset()
    {
        return offset;
    }
  
}
