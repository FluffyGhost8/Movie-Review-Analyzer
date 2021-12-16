
public class WordDataStorage
{
    private double sentiment;
    private int numOccurrences;
    private double avgSentiment;

    public WordDataStorage()
    {
        System.out.println("Use the other constructor");
        System.exit(0);
    }
    
    public WordDataStorage(double sentiment, int occurrences)
    {
        this.sentiment = sentiment;
        this.numOccurrences = occurrences;
    }

    public void increaseSentiment(double num)
    {
        this.sentiment += num;
    }

    public void incrementOccurrences()
    {
        this.numOccurrences++;
    }

    public void updateAvgSentiment()
    {
        this.avgSentiment = ((double)this.sentiment)/this.numOccurrences;
    }

    public double getAvgSentiment()
    {
        return this.avgSentiment;
    }
}
