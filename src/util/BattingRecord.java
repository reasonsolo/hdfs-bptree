package util;

public class BattingRecord
{
    private final String playerId, line;
    private long offset;
    
    public BattingRecord(String playerId, String line)
    {
        this.playerId = playerId;
        this.line = line;
    }
    
    public String getId()
    {
        return this.playerId;
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