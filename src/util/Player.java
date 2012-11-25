package util;
/**
 * A complete player, with all information from a parsed record.
 * TODO: Maybe store Date objects rather than strings.  Can we count
 * on any sort of consistency?
 * @author bfults (Brian Fults - 905084698)
 *
 */
public class Player
{
    private final String playerId;
    
    private final String birthYear;
    private final String birthPlace;
    private final String birthCountry;
    private final String deathYear;
    private final String deathPlace;
    private final String deathCountry;
    
    private final String firstName;
    private final String lastName;
    
    private final int weight;
    private final int height;
    private final char bats;
    private final char throwing;
    
    private final String beginDate;
    private final String endDate;
    
    public Player(String playerId, String birthYear, String birthPlace,
        String birthCountry, String deathYear, String deathPlace,
        String deathCountry, String firstName, String lastName, 
        int weight, int height, char bats, char throwing,
        String beginDate, String endDate)
    {
        this.playerId = playerId;
        this.birthYear = birthYear;
        this.birthPlace = birthPlace;
        this.birthCountry = birthCountry;
        this.deathYear = deathYear;
        this.deathPlace = deathPlace;
        this.deathCountry = deathCountry;
        this.firstName = firstName;
        this.lastName = lastName;
        this.weight = weight;
        this.height = height;
        this.bats = bats;
        this.throwing = throwing;      
        this.beginDate = beginDate;
        this.endDate = endDate;
    }

    public String toString()
    {
        String toReturn = "";
        toReturn += "Player ID: " + getPlayerId() + "\n";
        toReturn += "Name: " + getName() + "\n";
        if (!birthYear.isEmpty())
        {
            toReturn += "Birth Date: " + getBirthYear() + "\n";
        }
        if (!birthPlace.isEmpty())
        {
            toReturn += "Birth Place: ";
            toReturn += getBirthPlace();
        
            if (!birthCountry.isEmpty())
            {
                toReturn += ", " + 
                    getBirthCountry();
            }
            toReturn += "\n";
        }
        else if (!birthCountry.isEmpty())
        {
            toReturn += "Birth Place: ";
            toReturn += getBirthCountry();
            toReturn += "\n";
        }
        if (!birthYear.isEmpty())
        {
            toReturn += "Death Date: " + getDeathYear() + "\n";
        }
        if (!deathPlace.isEmpty())
        {
            toReturn += "Death Place: ";
            toReturn += getDeathPlace();
        
            if (!deathCountry.isEmpty())
            {
                toReturn += ", " + 
                    getDeathCountry();
            }
            toReturn += "\n";
        }
        else if (!deathCountry.isEmpty())
        {
            toReturn += "Death Place: ";
            toReturn += getBirthCountry();
            toReturn += "\n";
        }
        if (weight > 0)
        {
            toReturn += "Weight: " + weight + "\n";
        }
        if (height > 0)
        {
            toReturn += "Height: " + height + "\n";
        }
        if (bats != 'Z')
        {
            toReturn += "Bats: " + bats + "\n";
        }
        if (throwing != 'Z')
        {
            toReturn += "Throws: " + throwing + "\n";
        }
        if (!beginDate.isEmpty())
        {
            toReturn += "First game: " + beginDate + "\n";
        }
        if (!endDate.isEmpty())
        {
            toReturn += "Last game: " + endDate + "\n";
        }
        return toReturn;
    }
    public String getPlayerId()
    {
        return playerId;
    }

    public String getBirthPlace()
    {
        return birthPlace;
    }

    public String getDeathYear()
    {
        return deathYear;
    }

    public String getBirthYear()
    {
        return birthYear;
    }

    public String getBirthCountry()
    {
        return birthCountry;
    }

    public String getDeathPlace()
    {
        return deathPlace;
    }

    public String getDeathCountry()
    {
        return deathCountry;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public int getHeight()
    {
        return height;
    }

    public int getWeight()
    {
        return weight;
    }

    public char getBats()
    {
        return bats;
    }

    public char getThrowing()
    {
        return throwing;
    }
    
    public String getName()
    {
        return getLastName() + ", " + getFirstName();
    }

    public String getBeginDate()
    {
        return beginDate;
    }

    public String getEndDate()
    {
        return endDate;
    }
    
}
