package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * This parser is used to get batting records
 * from a comma-separated file following
 * the format given in the project 4 specification.
 * @author xclite
 *
 */
public class BattingCSVParser
{
    //begin column order data
    final private static short NUM_COLUMNS = 23;
    final private static short PLAYER_ID = 0;
    final private static short YEAR = 1;
    final private static short STINT = 2;
    final private static short TEAM = 3;
    final private static short LEAGUE = 4;
    final private static short GAMES = 5;
    final private static short AT_BATS = 6;
    final private static short RUNS = 7;
    final private static short HITS = 8;
    final private static short DOUBLES = 9;
    final private static short TRIPLES = 10;
    final private static short HOME_RUNS = 11;
    final private static short RBI = 12;
    final private static short STOLEN_BASES = 13;
    final private static short CAUGHT_STEALING = 14;
    final private static short BASE_ON_BALLS = 15;
    final private static short STRIKE_OUTS = 16;
    final private static short INTENTIONAL_WALKS = 17;
    final private static short HIT_BY_PITCH = 18;
    final private static short SACRIFICE_HITS = 19;
    final private static short SACRIFICE_FLIES = 20;
    final private static short GROUND_INTO_DOUBLE_PLAY = 21;
    final private static short GAMES_PLAYED_IN_FIELD = 22;
    //end column order data
    
    private Scanner lineScanner;
    
    /**
     * Lines are delimited by a newline.
     */
    private static final String LINE_DELIMETER = "\n";
    /**
     * Tokens are delimited by a tab.
     */
    private static final Pattern TOKEN_DELIMETER = Pattern.compile(",");
    
    /**
     * Constructor:
     * create a scanner for the file,
     * set the line delimeter.
     * @param fileName
     * @throws FileNotFoundException 
     */
    public BattingCSVParser(String fileName) throws FileNotFoundException
    {
        lineScanner = new Scanner(new File(fileName));
        lineScanner.useDelimiter(LINE_DELIMETER);
    }
    
    public void close()
    {
        lineScanner.close();
    }
    
    /**
     * Checks for more records (lines)
     * @return true if more lines exist.
     */
    public boolean hasNext()
    {
        return lineScanner.hasNext();
    }
    
    /**
     * Parses the next line and returns a record.
     * @return A record with the name, id, and line text.
     */
    public BattingRecord nextRecord()
    {
        String line = lineScanner.next();
        String[] values = parseColumns(line);
        String id = values[PLAYER_ID];
        return new BattingRecord(id, line);
    }
    
    /**
     * Parses a line into an array of values corresponding
     * to their order in the record.
     * @param line line to parse
     * @return an array of values
     */
    private static String[] parseColumns(String line)
    {
        Scanner tokenScanner = new Scanner(line);
        tokenScanner.useDelimiter(TOKEN_DELIMETER);
        String[] values = new String[NUM_COLUMNS];
        for (int i = 0; i < NUM_COLUMNS; i++)
        {
            values[i] = tokenScanner.next();
        }
        return values;
    }
    
    /**
     * Creates a player batting record object from a line of text
     * @param line
     * @return
     */
    public static PlayerBattingRecord createPlayerBattingRecord(String line)
    {
        return createPlayerBattingRecord(parseColumns(line));
    }
    
    /**
     * Creates a player batting record from the given values.
     * @param values array of values to create batting info from should
     * correspond to the column numbers.
     * @return a player batting record object encapsulating these values.
     */
    public static PlayerBattingRecord createPlayerBattingRecord(String[] values)
    {
        //These values are guaranteed to be present
        String playerId = values[PLAYER_ID];
        int year = Integer.parseInt(values[YEAR]);
        int stint = Integer.parseInt(values[STINT]);        
        String team = values[TEAM];
        String league = values[LEAGUE];
        int games = Integer.parseInt(values[GAMES]);
        int atBats = Integer.parseInt(values[AT_BATS]);
        int runs = Integer.parseInt(values[RUNS]);
        int hits = Integer.parseInt(values[HITS]);
        int doubles = Integer.parseInt(values[DOUBLES]);
        int triples = Integer.parseInt(values[TRIPLES]);
        int homeRuns = Integer.parseInt(values[HOME_RUNS]);
        int rbi = Integer.parseInt(values[RBI]);
        
        //These values might not be present
        int stolenBases = parseWithPossibleEmpty(STOLEN_BASES, values);
        int caughtStealing = parseWithPossibleEmpty(CAUGHT_STEALING, values);
        int baseOnBalls = parseWithPossibleEmpty(BASE_ON_BALLS, values);
        int strikeOuts = parseWithPossibleEmpty(STRIKE_OUTS, values);
        int intentionalWalks = parseWithPossibleEmpty(INTENTIONAL_WALKS, values);
        int hitByPitch = parseWithPossibleEmpty(HIT_BY_PITCH, values);
        int sacrificeHits = parseWithPossibleEmpty(SACRIFICE_HITS, values);
        int sacrificeFlies = parseWithPossibleEmpty(SACRIFICE_FLIES, values);
        int groundIntoDP = parseWithPossibleEmpty(GROUND_INTO_DOUBLE_PLAY, values);
        int gamesPlayedInField = parseWithPossibleEmpty(GAMES_PLAYED_IN_FIELD, values);
        return new PlayerBattingRecord(playerId , year, stint, team, league, games, atBats, runs, hits, doubles, triples, homeRuns, rbi, stolenBases, caughtStealing, baseOnBalls, strikeOuts, intentionalWalks, hitByPitch, sacrificeHits, sacrificeFlies, groundIntoDP, gamesPlayedInField );
    }

    /**
     * This logic is used to generate the appropriate
     * value for any column which may hold an int,
     * but may not necessarily have a value at all.
     * @param columnValue
     * @param values
     * @return
     */
    private static int parseWithPossibleEmpty(
        short columnValue,
        String[] values )
    {
        if (values[columnValue].isEmpty())
            return -1;
        return Integer.parseInt(values[columnValue].trim());
    }
}
