import java.util.Scanner;

public class MoviesMain
{
    public static void main(String[] args)
    {
        Scanner keyboard = new Scanner(System.in);
        
        WordSentimentLearner learner = new WordSentimentLearner();
        learner.learn();

        char predictAgain = 'y';

        while(predictAgain == 'y')
        {
            System.out.println("Enter a review for the program to predict its sentiment: ");
            String review = keyboard.nextLine();

            String finalReview = "";
            review = review.toLowerCase();
            for(String w : review.split("\\s+"))
            {
                finalReview = finalReview + " " + w.replaceAll("[^a-zA-Z]", "");
            }
            while(finalReview.length() < 10)
            {
                System.out.println("Please enter a longer review for more accurate results");
                review = keyboard.nextLine();

                finalReview = "";
                review = review.toLowerCase();
                for(String w : review.split("\\s+"))
                {
                    finalReview = finalReview + " " + w.replaceAll("[^a-zA-Z]", "");
                }
            }
            finalReview = finalReview.substring(1);

            //System.out.println(finalReview);
            double reviewRating = learner.predict(review);
            //System.out.println(reviewRating);
            reviewRating *= 4;
            if(reviewRating >= 0)
            {
                reviewRating = Math.max(reviewRating, 1.0);
                reviewRating = Math.min(reviewRating, 4.0);
                System.out.printf("On a scale of 1 to 4, your review was rated as: %.2f", reviewRating);
                System.out.println();
            }

            System.out.println("Would you like to predict the sentiment of another review? Enter y/Y for yes, and anything else for no: ");
            String userInput = keyboard.nextLine();
            if(userInput.length() < 1)
            {
                break;
            } else {
                predictAgain = userInput.charAt(0);
            }
        }

        keyboard.close();
    }
}
