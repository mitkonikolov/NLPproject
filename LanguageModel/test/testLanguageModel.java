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

        HashMap<String, Integer> uniCount = lm.getUnigramCount();

        Iterator<String> iter = uniCount.keySet().iterator();
        String w;

        while(iter.hasNext()) {
            w = iter.next();
            System.out.println(w + ": " + uniCount.get(w));
        }
    }
}
