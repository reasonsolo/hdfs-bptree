package util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class BattingDatabaseFile
{
    private RandomAccessFile file;
    private BattingCSVParser parser;
    
    public BattingDatabaseFile(String filePath) throws FileNotFoundException
    {
        file = new RandomAccessFile(filePath, "rw");
    }
    
    /**
     * imports records from a given CSV file.
     * @return A list of records, one for each imported
     * record.
     * @throws IOException 
     */
    public List<BattingRecord> importBattingRecords(String csvFilePath) throws IOException
    {
        ArrayList<BattingRecord> battingRecords = new ArrayList<BattingRecord>();
        parser = new BattingCSVParser(csvFilePath);
        while (parser.hasNext())
        {
            BattingRecord parsed = parser.nextRecord();
            addBattingRecord(parsed);
            battingRecords.add(parsed);
        }
        parser.close();
        return battingRecords;
    }

    public PlayerBattingRecord getPlayerBattingRecord(long offset) throws IOException
    {
        long currentOffset = file.getFilePointer();
        file.seek(offset);
        PlayerBattingRecord toReturn = BattingCSVParser.createPlayerBattingRecord(file.readUTF());
        file.seek(currentOffset);
        return toReturn;
    }
    
    public void close() throws IOException
    {
        file.close();
    }
    /**
     * This method adds the BattingRecord to this database file, and 
     * sets the BattingRecord's offset to the file pointer's offset before
     * writing.
     * @param parsed
     * @throws IOException
     */
    private void addBattingRecord(BattingRecord parsed) throws IOException
    {
        parsed.setOffset(file.getFilePointer());
        file.writeUTF(parsed.getLine());
    }

    public String getBattingDetails( Long foundOffset ) throws IOException
    {
        long currentOffset = file.getFilePointer();
        file.seek(foundOffset);
        PlayerBattingRecord record = BattingCSVParser.createPlayerBattingRecord(file.readUTF());
        String thisId = record.getPlayerId();
        String details = "Batting details for: " + thisId + "\n";
        while (file.getFilePointer() < file.length() && record.getPlayerId().equals(thisId))
        {
            details += record.toString();
            record = BattingCSVParser.createPlayerBattingRecord(file.readUTF());
        }
        file.seek(currentOffset);
        return details;
    }

    public String getBattingSummary( Long foundOffset ) throws IOException
    {
        long currentOffset = file.getFilePointer();
        file.seek(foundOffset);
        PlayerBattingRecord record = BattingCSVParser.createPlayerBattingRecord(file.readUTF());
        String thisId = record.getPlayerId();
        String summary = "Batting summary for: " + thisId + "\n";
        int years = 0;
        int games = 0;
        int atBats = 0;
        int singles = 0;
        int doubles = 0;
        int triples = 0;
        int homeRuns = 0;
        while (file.getFilePointer() < file.length() && record.getPlayerId().equals(thisId))
        {
            ++years;
            games += record.getGames();
            atBats += record.getAtBats();
            singles += record.getSingles();
            doubles += record.getDoubles();
            triples += record.getTriples();
            homeRuns += record.getHomeRuns();
            record = BattingCSVParser.createPlayerBattingRecord(file.readUTF());
        }
        DecimalFormat fmt = new DecimalFormat("0.000");
        double average = atBats == 0? 0 : (singles + doubles + triples + homeRuns) / (double) atBats;
        summary += "Number of years played: " + years + "\n" +
            "Total Games: " + games + "\n" + 
            "Total at bats: " + atBats + "\n" + 
            "Total singles: " + singles + "\n" +
            "Total doubles: " + doubles + "\n" + 
            "Total triples: " + triples + "\n" +
            "Total Home runs: " + homeRuns + "\n" + 
            "Overall Batting Average: " + fmt.format(average) + "\n";
        file.seek(currentOffset);
        return summary;
    }

    
    
    
    
}
