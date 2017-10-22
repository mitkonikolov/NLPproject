import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by Mitko on 10/18/17.
 */
public class LanguageModel {
    HashMap<String, Integer> unigramCount;
    HashMap<String, HashMap<String, Integer>> bigramCount;

    // trigram map "I want" -> ("to" -> 3)
    HashMap<String, HashMap<String, Integer>> trigramCount;

    // map for POS tags
    HashMap<String, HashMap<String, Integer>> posTags;

    HashMap<String, Double> unigramProbs;
    HashMap<String, HashMap<String,Double>> bigramProbs;
    HashMap<String, HashMap<String, Double>> trigramProbs;
    HashMap<String, HashMap<String, Double>> tagProbs;


    public LanguageModel() {
        this.unigramCount = new HashMap<>();
        this.bigramCount = new HashMap<>();
        this.trigramCount = new HashMap<>();
        this.posTags = new HashMap<>();

        this.unigramProbs = new HashMap<>();
        this.bigramProbs = new HashMap<>();
        this.trigramProbs = new HashMap<>();
        this.tagProbs = new HashMap<>();
    }

    public void parse() {
        try {
            File f = new File("./src/ca00");
            Scanner s = new Scanner(f);
            Scanner s2;

            String sentence;
            String word, posTag;
            String previousWord;
            String previousTwoWords;


            while(s.hasNextLine()) {
                previousWord = "<s>";
                previousTwoWords = "<s> <s>";
                sentence = s.nextLine();

                if(sentence.length()!=0) {

                    s2 = new Scanner(sentence);

                    while(s2.hasNext()) {
                        String[] words = s2.next().split("/");

                        word = words[0].toLowerCase();
                        posTag = words[1];

                        if(word.contains("'") && word.length()>2) {
                            int ind = word.indexOf("'");
                            String firstWord = word.substring(0, ind);
                            String secondWord = word.substring(ind);

                            updateNGram("", firstWord, 0);
                            updateNGram(previousWord, firstWord, 1);
                            updateNGram(previousTwoWords, firstWord, 2);
                            updateNGram(word, posTag, 3);

                            // secondWord is "'s"
                            updateNGram("", secondWord, 0);
                            updateNGram(firstWord, secondWord, 1);
                            updateNGram(previousWord + " " + firstWord, secondWord, 2);

                            previousTwoWords = firstWord + " " + secondWord;
                            previousWord = secondWord;
                        }
                        else {
                            // updateUnigram
                            updateNGram("", word, 0);
                            // updateBigram
                            updateNGram(previousWord, word, 1);
                            // updateTrigram
                            updateNGram(previousTwoWords, word, 2);
                            // updatePOS
                            updateNGram(word, posTag, 3);

                            previousTwoWords = previousWord + " " + word;
                            previousWord = word;
                        }
                    }
                }

            }
        }
        catch (FileNotFoundException e) {
            System.out.println("file was not found");
        }
    }


    private void updateNGram (String previous, String word, int mapUpdate) {
        Integer value;
        HashMap<String, Integer> wordAfter;

        Iterator<String> iter;
        switch (mapUpdate) {
            case 0:
                value = this.unigramCount.get(word);
                if(value == null) {
                    value = 0;
                }
                unigramCount.put(word, value + 1);
                break;
            case 1:
                wordAfter = this.bigramCount.get(previous);
                if(wordAfter==null) {
                    wordAfter = new HashMap<>();
                }
                updateInnerMap(wordAfter, word);
                this.bigramCount.put(previous, wordAfter);
                break;
            case 2:
                wordAfter = this.trigramCount.get(previous);
                if(wordAfter==null) {
                    wordAfter = new HashMap<>();
                }
                updateInnerMap(wordAfter, word);
                this.trigramCount.put(previous, wordAfter);
                break;
            default:
                wordAfter = this.posTags.get(previous);
                if(wordAfter==null) {
                    wordAfter = new HashMap<>();
                }
                updateInnerMap(wordAfter, word);
                this.posTags.put(previous, wordAfter);
        }
    }


    private void updateInnerMap(HashMap<String, Integer> wordAfter, String word) {
        Integer value;

        if(wordAfter==null) {
            wordAfter = new HashMap<>();
        }

        value = wordAfter.get(word);
        if(value == null) {
            value = 0;
        }
        value += 1;

        wordAfter.put(word, value);
    }

    public void calcProbs() {
        calcUniProbs();
        /*calcOtherProbs(2);
        calcOtherProbs(3);
        calcOtherProbs(4);*/
        calcBigProbs(2);
        calcBigProbs(3);
        calcBigProbs(4);
    }

    private void calcUniProbs() {
        Iterator<String> iter = this.unigramCount.keySet().iterator();
        int totalWords = 0;
        String word;
        int occurences;

        while(iter.hasNext()) {
            totalWords += this.unigramCount.get(iter.next());
        }

        iter = this.unigramCount.keySet().iterator();
        while(iter.hasNext()) {
            word = iter.next();
            occurences = this.unigramCount.get(word);
            double prob = ((double) occurences) / totalWords;
            this.unigramProbs.put(word, prob);
        }
    }

    private void calcBigProbs(int choice) {
        HashMap<String, HashMap<String, Integer>> countMap;
        HashMap<String, HashMap<String, Double>> probMap;
        switch(choice) {
            case 2:
                countMap = this.bigramCount;
                probMap = this.bigramProbs;
                break;
            case 3:
                countMap = this.trigramCount;
                probMap = this.trigramProbs;
                break;
            case 4:
                countMap = this.posTags;
                probMap = this.tagProbs;
                break;
            default:
                countMap = new HashMap<>();
                probMap = new HashMap<>();
                System.out.println("calcBigProbs is being used with wrong choice");
                throw new InputMismatchException("choice is incorrect");

        }

        Iterator<String> iter = countMap.keySet().iterator();
        Iterator<String> innerIter;
        String wordBefore;
        HashMap<String, Integer> wordsAfter;
        String wordAft;
        int totalOccurences;
        int currOccurences;
        HashMap<String, Double> probInnerMap;

        while(iter.hasNext()) {
            probInnerMap = new HashMap<>();
            totalOccurences = 0;
            wordBefore = iter.next();
            wordsAfter = countMap.get(wordBefore);

            innerIter = wordsAfter.keySet().iterator();
            while(innerIter.hasNext()) {
                wordAft = innerIter.next();
                totalOccurences += wordsAfter.get(wordAft);
            }

            innerIter = wordsAfter.keySet().iterator();
            while(innerIter.hasNext()) {
                wordAft = innerIter.next();
                currOccurences = wordsAfter.get(wordAft);
                double prob = ((double)currOccurences)/totalOccurences;
                probInnerMap.put(wordAft, prob);
            }

            probMap.put(wordBefore, probInnerMap);
        }
    }

/*    private void calcOtherProbs(int choice) {
        Iterator<String> iter;

        switch(choice) {
            case 2:
                iter = this.bigramCount.keySet().iterator();
            case 3:
                iter = this.trigramCount.keySet().iterator();
            case 4:


        }
    }*/


    public HashMap<String, Integer> getUnigramCount() {
        return this.unigramCount;
    }

    public HashMap<String, HashMap<String, Integer>> getBigramCount() {
        return this.bigramCount;
    }

    public HashMap<String, HashMap<String, Integer>> getTrigramCount() {
        return this.trigramCount;
    }

    public HashMap<String, HashMap<String, Integer>> getPosTags() {
        return this.posTags;
    }

    public HashMap<String, Double> getUnigProb() {
        return this.unigramProbs;
    }

    public HashMap<String, HashMap<String, Double>> getBigramProbs() {
        return this.bigramProbs;
    }

    public HashMap<String, HashMap<String, Double>> getTrigramProbs() {
        return this.trigramProbs;
    }

    public HashMap<String, HashMap<String, Double>> getTagProbs() {
        return this.tagProbs;
    }


}
