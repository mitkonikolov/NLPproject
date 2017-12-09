/**
 * Created by Mitko on 12/9/17.
 */
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.Tree;

class Parser {

    private final static String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";

    private final TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "invertible=true");

    private final LexicalizedParser parser = LexicalizedParser.loadModel(PCG_MODEL);

    private List<String> infoToLookFor;

    public Tree parse(String str) {
        this.infoToLookFor = new ArrayList<>();
        List<CoreLabel> tokens = tokenize(str);
        Tree tree = parser.apply(tokens);
        return tree;
    }

    private List<CoreLabel> tokenize(String str) {
        Tokenizer<CoreLabel> tokenizer =
                tokenizerFactory.getTokenizer(
                        new StringReader(str));
        return tokenizer.tokenize();
    }

    public static void main(String[] args) {
        List<String> infoToLookFor = new ArrayList<>();
        String str = "Is this my rEd shirt?";
        str = str.toLowerCase();
        Parser parser = new Parser();
        Tree tree = parser.parse(str);

        System.out.println(tree.pennString());

        int ind = 0;
        String questionWord="";
        String action = "";
        List<Tree> leaves = tree.getLeaves();
        // Print words and Pos Tags
        for (Tree leaf : leaves) {
            Tree parent = leaf.parent(tree);

            // set the first word
            if(ind==0) {
                ind = 1;
                questionWord = leaf.label().value();
                System.out.println(leaf.label().value() + "-" +
                        parent.label().value() + " ");
            }
            // store any NN or JJ if question starts with is
            else if(questionWord.equals("is")) {
                if(parent.label().value().equals("NN") ||
                        parent.label().value().equals("JJ")) {
                    infoToLookFor.add(leaf.label().value());
                }
            }
        }

    }
}