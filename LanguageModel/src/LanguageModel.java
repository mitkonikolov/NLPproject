import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Scanner;

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


    /**
     * Parses the input files and updates all count maps.
     */
    public void parse() {
        try {
            File filesDir = new File("../brown corpus/brown");
            // ignore hidden files
            FilenameFilter fileFilter = (dir, name) -> {
                Character firstChar = name.charAt(0);
                return !firstChar.equals('.');
            };
            File[] allFiles = filesDir.listFiles(fileFilter);

            for(int i = 0; i < allFiles.length; i++) {
                Scanner s = new Scanner(allFiles[i]);
                Scanner s2;

                String sentence;
                String word, posTag;
                String previousWord;
                String previousTwoWords;


                while (s.hasNextLine()) {
                    previousWord = "<s>";
                    previousTwoWords = "<s> <s>";
                    sentence = s.nextLine();

                    if (sentence.length() != 0) {

                        s2 = new Scanner(sentence);

                        while (s2.hasNext()) {
                            String[] words = s2.next().split("/");

                            word = words[0].toLowerCase();
                            posTag = words[1];

                            // the word contains an apostrophe and needs to be
                            // parsed in a special way
                            if (word.contains("'") && word.length() > 2) {
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
                            // the words does not contain an apostrophe so the maps
                            // can directly be updated
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Updates the count maps' values for previous -> word by 1.
     * @param previous the expression before {@param word}
     * @param word {@code String} after {@param previous}
     * @param mapUpdate indicates which map needs to be updated
     */
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


    /**
     * Increments by 1 the number in {@param wordAfter} associated with
     * {@param word}.
     *
     * @param wordAfter {@code HashMap} of numbers and strings that needs to
     *                                 be updated
     * @param word {@code String} whose associated number in {@param wordAfter}
     *                           needs to be incremented
     */
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


    /**
     * Invokes methods to update the probability for unigrams, bigrams,
     * trigrams and pos tags.
     */
    public void calcProbs() {
        calcUniProbs();
        calcOtherProbs(2);
        calcOtherProbs(3);
        calcOtherProbs(4);
    }


    /**
     * Calculates the unigram probabilities for all the words
     * that were seen and stores them in this.unigramProbs.
     */
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


    /**
     * Identifies whether the bigram, trigram or pos probabilities need
     * to be calculated based on {@param choice} and then passes references
     * to the maps to the helper {@code calcOthProbsHelper} for it to calculate
     * the values.
     *
     * @param choice
     */
    private void calcOtherProbs(int choice) {
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
                throw new InputMismatchException("choice is incorrect");

        }

        calcOthProbsHelper(countMap, probMap);
    }


    /**
     * Given a {@param countMap} it retrieves how many times is the word Y
     * seen after the expression X and then calculates this number by the total
     * number of times X has been seen. Then it puts this probability into
     * {@param probMap} as X -> (Y -> probability).
     *
     * @param countMap contains the number of times a word has been seen
     *                 after some expression
     * @param probMap contains the probability for each word/tag seen after
     *                each expression
     */
    private void calcOthProbsHelper(HashMap<String, HashMap<String, Integer>> countMap,
                                    HashMap<String, HashMap<String, Double>> probMap) {

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
            // calculate the total number of times wordBefore has been seen
            while(innerIter.hasNext()) {
                wordAft = innerIter.next();
                totalOccurences += wordsAfter.get(wordAft);
            }

            innerIter = wordsAfter.keySet().iterator();
            // calculate the probability of wordAft being seen after wordBefore
            while(innerIter.hasNext()) {
                wordAft = innerIter.next();
                currOccurences = wordsAfter.get(wordAft);
                double prob = ((double)currOccurences)/totalOccurences;
                probInnerMap.put(wordAft, prob);
            }

            probMap.put(wordBefore, probInnerMap);
        }
    }


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
