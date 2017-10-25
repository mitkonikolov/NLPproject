import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Mitko on 10/18/17.
 */
public class testLanguageModel {

    @Test
    public void testParsing() {
        LanguageModel lm = new LanguageModel();

        lm.parse();
        lm.calcProbs();

        HashMap<String, Integer> uniCount = lm.getUnigramCount();

        Iterator<String> iter = uniCount.keySet().iterator();
        String w;

        HashMap<String, HashMap<String, Double>> bigramCount = lm.getTrigramProbs();

        iter = bigramCount.keySet().iterator();
        Iterator<String> innerIter;

        HashMap<String, Double> wordsAfter;
        while(iter.hasNext()) {
            w = iter.next();
            System.out.println(w);
            //System.out.println(bigramCount.size());


            wordsAfter = bigramCount.get(w);
            //System.out.println(wordsAfter.size());


            innerIter = wordsAfter.keySet().iterator();

            System.out.println("After:");
            while(innerIter.hasNext()) {
                w = innerIter.next();
                System.out.println(w + " " + wordsAfter.get(w));
            }
            System.out.println("\n\n");

        }






/*        HashMap<String, Double> uniProb = lm.getUnigProb();

        iter = uniProb.keySet().iterator();

        while(iter.hasNext()) {
            w = iter.next();
            System.out.println(uniProb.get(w));
        }*/
    }
}
