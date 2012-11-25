package indexes;

import java.util.List;

/**
 * This interface guarantees that a particular relationship between a key and
 * the value has for its value a list rather than a single value.
 * @author bfults (Brian Fults - 905084698)
 * @param <RecordType> The type being stored in the records.
 *
 */
public interface AggregateRecords <RecordType>
{
    /**
     * Any aggregate record must be able to add a value to its records.
     * @param toAdd A record to add.
     */
    public void addValue(RecordType toAdd);
    
    /**
     * All aggregate records must be guaranteed to return records.
     * @return All records for this relationship.
     */
    public List<RecordType> getRecords();
    
    /**
     * An aggregate record being inserted should only have 1 element in the list:
     * @return the first record
     */
    public RecordType getRecord();
}
