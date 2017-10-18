import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
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


    public LanguageModel() {
        this.unigramCount = new HashMap<>();
        this.bigramCount = new HashMap<>();
        this.trigramCount = new HashMap<>();
        this.posTags = new HashMap<>();
    }

    public void parse() {
        try {
            File f = new File("./src/ca00");
            Scanner s = new Scanner(f);
            Scanner s2;

            String sentence;
            String word, posTag;
            String previousWord = "<s>";
            String previousTwoWords = "<s> <s>";


            while(s.hasNextLine()) {
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
                        }


                        previousTwoWords = previousWord + " " + word;
                        previousWord = word;


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
                updateInnerMap(wordAfter, word);
                this.bigramCount.put(previous, wordAfter);
                break;
            case 2:
                wordAfter = this.trigramCount.get(previous);
                updateInnerMap(wordAfter, word);
                this.trigramCount.put(previous, wordAfter);
                break;
            default:
                wordAfter = this.posTags.get(previous);
                updateInnerMap(wordAfter, word);
                this.trigramCount.put(previous, wordAfter);
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


    public HashMap<String, Integer> getUnigramCount() {
        return this.unigramCount;
    }



}
