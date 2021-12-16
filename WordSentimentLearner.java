import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.ArrayList;
import java.util.HashMap;  // Import to store word data in HashMap
import java.util.Collections;

public class WordSentimentLearner
{
    private File file;
    private Scanner fileReader;
    private HashMap<String, WordDataStorage> words = new HashMap<String, WordDataStorage>();
    private NeuralNetwork nn = new NeuralNetwork(5, 10, 1);
    private double[][] X;
    private double[][] y;
    public double top25thPercentile;
    public double bottom25thPercentile;
    public double midPercentile;

    public WordSentimentLearner()
    {
        
    }

    private void setPercentiles()
    {
        ArrayList<Double> scores = new ArrayList<Double>();
        for(WordDataStorage value : words.values())
        {
            scores.add(value.getAvgSentiment());
        }
        Collections.sort(scores);
        
        top25thPercentile = scores.get((3*(scores.size()/4)));
        bottom25thPercentile = scores.get((scores.size()/4));
        midPercentile = scores.get((scores.size()/2));
    }

    private double average(String review)
    {
        int numWords = 0;
        double totalSentiment = 0;

        for(String w : review.split("\\s+"))
        {
            if(words.get(w) != null)
            {
                numWords++;
                totalSentiment += words.get(w).getAvgSentiment();
            }
        }
        
        if(numWords < 1)
        {
            return -1;
        } else {
            return totalSentiment/numWords;
        }
    }

    private int numNegativeWords(String review)
    {
        int num = 0;
        for(String w : review.split("\\s+"))
        {
            if(words.get(w) == null)
            {
                continue;
            }

            if(words.get(w).getAvgSentiment() < midPercentile)
            {
                num++;
            }
        }
        return num;
    }

    private int numPositiveWords(String review)
    {
        int num = 0;
        for(String w : review.split("\\s+"))
        {
            if(words.get(w) == null)
            {
                continue;
            }

            if(words.get(w).getAvgSentiment() >= midPercentile)
            {
                num++;
            }
        }
        return num;
    }

    private int numVeryNegativeWords(String review, double bottom25th)
    {
        int num = 0;
        for(String w : review.split("\\s+"))
        {
            if(words.get(w) == null)
            {
                continue;
            }
            
            if(words.get(w).getAvgSentiment() <= bottom25th)
            {
                num++;
            }
        }
        return num;
    }

    private int numVeryPositiveWords(String review, double top25th)
    {
        int num = 0;
        for(String w : review.split("\\s+"))
        {
            if(words.get(w) == null)
            {
                continue;
            }

            if(words.get(w).getAvgSentiment() >= top25th)
            {
                num++;
            }
        }
        return num;
    }

    public void learn()
    {
        try
        {
            file = new File("movieReviews.txt");
            fileReader = new Scanner(file);
            Scanner anotherReader = new Scanner(file);

            int numLines = 8529;

            X = new double[numLines][5];
            y = new double[numLines][1];
            
            int pos = 0;

            while(fileReader.hasNextLine())
            {
                int rat = fileReader.nextInt();
                double rating = ((double)rat)/4;
                String review = fileReader.nextLine();

                review.replaceAll("[^a-zA-Z]", "");
                review.toLowerCase();
                String finalReview = "";
                review = review.toLowerCase();
                for(String w : review.split("\\s+"))
                {
                    finalReview = finalReview + " " + w.replaceAll("[^a-zA-Z]", "");
                }

                if(finalReview.length() < 2)
                {
                    continue;
                }

                finalReview = finalReview.substring(1);

                for(String w : finalReview.split("\\s+"))
                {
                    if(words.get(w) == null)
                    {
                        words.put(w, new WordDataStorage(rating, 1));
                        words.get(w).updateAvgSentiment();
                    } else {
                        words.get(w).increaseSentiment(rating);
                        words.get(w).incrementOccurrences();
                        words.get(w).updateAvgSentiment();
                    }
                }

                y[pos][0] = rating;
                pos++;
            }
            fileReader.close();
            
            pos = 0;

            setPercentiles();

            while(anotherReader.hasNextLine())
            {
                String review = anotherReader.nextLine();

                review.replaceAll("[^a-zA-Z]", "");
                review.toLowerCase();
                String finalReview = "";
                review = review.toLowerCase();
                for(String w : review.split("\\s+"))
                {
                    finalReview = finalReview + " " + w.replaceAll("[^a-zA-Z]", "");
                }

                if(finalReview.length() < 2)
                {
                    continue;
                }

                finalReview = finalReview.substring(1);

                X[pos][0] = average(finalReview);
                X[pos][1] = (double)numNegativeWords(finalReview);
                X[pos][2] = (double)numPositiveWords(finalReview);
                X[pos][3] = (double)numVeryNegativeWords(finalReview, bottom25thPercentile);
                X[pos][4] = (double)numVeryPositiveWords(finalReview, top25thPercentile);
                pos++;
            }
            anotherReader.close();

            nn.fit(X, y, 10000000);
        } catch(FileNotFoundException e) {
            System.out.println("File not found exception");
            e.printStackTrace();
        }
    }

    public double predict(String review)
    {
        double reviewRating = average(review);

        // features of nn: {avavg, num neg, num pos, num lot neg, num lot pos}
        if(reviewRating < 0)
        {
            System.out.println("None of the words in this review are recognized");
            return -1;
        }

        /*for(int i = 0; i < 100; i++)
        {
            System.out.println("Average: " + X[i][0] + ", num negative words: " + X[i][1] + ", num positive words: " + X[i][2] + ", num very negative words: " + X[i][3] + ", num very positive words: " + X[i][4] + ", predicted rating: " + nn.predict(X[i]).get(0) + ", actual rating: " + y[i][0]);
        }*/

        /*System.out.println("top25th: " + top25thPercentile);
        System.out.println("midPercentile: " + midPercentile);
        System.out.println("bottom25th: " + bottom25thPercentile);
        System.out.println("avg: " + reviewRating);
        System.out.println("numNegativeWords: " + numNegativeWords(review));
        System.out.println("numPositiveWords: " + numPositiveWords(review));
        System.out.println("numVeryNegativeWords: " + (double)numVeryNegativeWords(review, bottom25thPercentile));
        System.out.println("numVeryPositiveWords: " + (double)numVeryPositiveWords(review, top25thPercentile));*/
        
        double[][] input = {{reviewRating, (double)numNegativeWords(review), (double)numPositiveWords(review), (double)numVeryNegativeWords(review, bottom25thPercentile), (double)numVeryPositiveWords(review, top25thPercentile)}};

        return (nn.predict(input[0])).get(0);
    }
}
