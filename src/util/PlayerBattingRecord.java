package util;

import java.text.DecimalFormat;

/**
 * This class represents a Batting Record as completely parsed from one of the record files.
 * A negative in any integer field or a nullstring in a string field denotes missing
 * information for this record.
 * @author xclite
 *
 */
public class PlayerBattingRecord
{
    private String playerId;
    private int year;
    private int stint;
    private String team;
    private String league; //TODO: check this, seemed weird in the spec.
    private int games;
    private int atBats;
    private int runs;
    private int hits;
    private int singles;
    private int doubles;
    private int triples;
    private int homeRuns;
    private int rbi;
    private int stolenBases;
    private int caughtStealing;
    private int baseOnBalls;
    private int strikeOuts;
    private int intentionalWalks;
    private int hitByPitch;
    private int sacrificeHits;
    private int sacrificeFlies;
    private int groundIntoDP;
    private int gamesPlayedInField;
    
    public PlayerBattingRecord(String playerId, int year, int stint, String team, String league,
        int games, int atBats, int runs, int  hits, int doubles, int triples, int homeRuns, int rbi)
    {
        this.setPlayerId( playerId );
        this.year = year;
        this.stint = stint;
        this.team = team;
        this.league = league;
        this.games = games;
        this.atBats = atBats;
        this.runs = runs;
        this.hits = hits;
        this.singles = hits - (doubles + triples + homeRuns);
        this.doubles = doubles;
        this.triples = triples;
        this.homeRuns = homeRuns;
        this.rbi = rbi;
        
        this.stolenBases = -1;
        this.caughtStealing = -1;
        this.baseOnBalls = -1;
        this.strikeOuts = -1;
        this.intentionalWalks = -1;
        this.hitByPitch = -1;
        this.sacrificeHits = -1;
        this.sacrificeFlies = -1;
        this.groundIntoDP = -1;
        this.gamesPlayedInField = -1;
    }
    
    public PlayerBattingRecord(String playerId, int year, int stint, String team, String league,
        int games, int atBats, int runs, int  hits, int doubles, int triples, int homeRuns, int rbi,
        int stolenBases, int caughtStealing, int baseOnBalls, int strikeOuts, int intentionalWalks,
        int hitByPitch, int sacrificeHits, int sacrificeFlies, int groundIntoDP, int gamesPlayedInField)
    {
        this(playerId, year, stint, team, league, games, atBats, runs, hits, doubles, triples, homeRuns, rbi);
        this.stolenBases = stolenBases;
        this.caughtStealing = caughtStealing;
        this.baseOnBalls = baseOnBalls;
        this.strikeOuts = strikeOuts;
        this.intentionalWalks = intentionalWalks;
        this.hitByPitch = hitByPitch;
        this.sacrificeHits = sacrificeHits;
        this.sacrificeFlies = sacrificeFlies;
        this.groundIntoDP = groundIntoDP;
        this.gamesPlayedInField = gamesPlayedInField;
    }

    public String toString()
    {
        DecimalFormat fmt = new DecimalFormat("0.000");
        return "Year: " + this.year + "\tTeam ID: " + this.team + "\tGames: " + this.games + "\tAt Bats: " + this.atBats +
            "\tSingles: " + this.singles + "\tDoubles: " + this.doubles + "\tTriples: " + this.triples + "\tHome Runs: " + this.homeRuns
            + "\tAverage: " + fmt.format(getBattingAverage()) + "\n";
    }
    
    public double getBattingAverage()
    {
        return atBats == 0? 0 : (singles + doubles + triples + homeRuns) / (double) atBats;
    }
    
    public void setPlayerId( String playerId )
    {
        this.playerId = playerId;
    }

    public int getSingles()
    {
        return this.singles;
    }
    
    public String getPlayerId()
    {
        return playerId;
    }

    public int getYear()
    {
        return year;
    }

    public void setYear( int year )
    {
        this.year = year;
    }

    public int getStint()
    {
        return stint;
    }

    public void setStint( int stint )
    {
        this.stint = stint;
    }

    public String getTeam()
    {
        return team;
    }

    public void setTeam( String team )
    {
        this.team = team;
    }

    public String getLeague()
    {
        return league;
    }

    public void setLeague( String league )
    {
        this.league = league;
    }

    public int getGames()
    {
        return games;
    }

    public void setGames( int games )
    {
        this.games = games;
    }

    public int getAtBats()
    {
        return atBats;
    }

    public void setAtBats( int atBats )
    {
        this.atBats = atBats;
    }

    public int getRuns()
    {
        return runs;
    }

    public void setRuns( int runs )
    {
        this.runs = runs;
    }

    public int getHits()
    {
        return hits;
    }

    public void setHits( int hits )
    {
        this.hits = hits;
    }

    public int getDoubles()
    {
        return doubles;
    }

    public void setDoubles( int doubles )
    {
        this.doubles = doubles;
    }

    public int getTriples()
    {
        return triples;
    }

    public void setTriples( int triples )
    {
        this.triples = triples;
    }

    public int getHomeRuns()
    {
        return homeRuns;
    }

    public void setHomeRuns( int homeRuns )
    {
        this.homeRuns = homeRuns;
    }

    public int getRbi()
    {
        return rbi;
    }

    public void setRbi( int rbi )
    {
        this.rbi = rbi;
    }

    public int getStolenBases()
    {
        return stolenBases;
    }

    public void setStolenBases( int stolenBases )
    {
        this.stolenBases = stolenBases;
    }

    public int getCaughtStealing()
    {
        return caughtStealing;
    }

    public void setCaughtStealing( int caughtStealing )
    {
        this.caughtStealing = caughtStealing;
    }

    public int getBaseOnBalls()
    {
        return baseOnBalls;
    }

    public void setBaseOnBalls( int baseOnBalls )
    {
        this.baseOnBalls = baseOnBalls;
    }

    public int getStrikeOuts()
    {
        return strikeOuts;
    }

    public void setStrikeOuts( int strikeOuts )
    {
        this.strikeOuts = strikeOuts;
    }

    public int getIntentionalWalks()
    {
        return intentionalWalks;
    }

    public void setIntentionalWalks( int intentionalWalks )
    {
        this.intentionalWalks = intentionalWalks;
    }

    public int getHitByPitch()
    {
        return hitByPitch;
    }

    public void setHitByPitch( int hitByPitch )
    {
        this.hitByPitch = hitByPitch;
    }

    public int getSacrificeHits()
    {
        return sacrificeHits;
    }

    public void setSacrificeHits( int sacrificeHits )
    {
        this.sacrificeHits = sacrificeHits;
    }

    public int getSacrificeFlies()
    {
        return sacrificeFlies;
    }

    public void setSacrificeFlies( int sacrificeFlies )
    {
        this.sacrificeFlies = sacrificeFlies;
    }

    public int getGroundIntoDP()
    {
        return groundIntoDP;
    }

    public void setGroundIntoDP( int groundIntoDP )
    {
        this.groundIntoDP = groundIntoDP;
    }

    public int getGamesPlayedInField()
    {
        return gamesPlayedInField;
    }

    public void setGamesPlayedInField( int gamesPlayedInField )
    {
        this.gamesPlayedInField = gamesPlayedInField;
    }
}
