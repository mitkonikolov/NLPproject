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
        /*
        questions that can be identified:
        is this a red shirt?
         */
        String str = "What is the information presented here?";
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
            // if the requested started with is/are, get all verbs and nouns
            // and use them in order to know how to analyze the result of the
            // picture and generate a response
            else if(questionWord.equals("is") || questionWord.equals("are")) {
                if(parent.label().value().equals("NN") ||
                        parent.label().value().equals("JJ")) {
                    infoToLookFor.add(leaf.label().value());
                }
            }
            else if(questionWord.equals("what")) {
                if(parent.label().value().equals("VBZ") ||
                        parent.label().value().equals("VBN") ||
                        parent.label().value().equals("VB")) {
                    infoToLookFor.add(leaf.label().value());
                }
            }
        }

        // choose queryType
        int queryType;
        // it needs to be checked if something is on the picture
        if(questionWord.equals("is") || questionWord.equals("are")) {
            queryType = 1;
        }
/*        // the color of something needs to be identified
        else if(questionWord.equals("what") &&
                infoToLookFor.contains("is") &&
                infoToLookFor.contains("color")) {
            queryType = 3;
        }*/
        // handwritten text needs to be read
        else if(questionWord.equals("what") &&
                ((infoToLookFor.contains("is") && infoToLookFor.contains("written")) ||
                        (infoToLookFor.contains("is") && infoToLookFor.contains("said")) ||
                        (infoToLookFor.contains("is") && infoToLookFor.contains("shown")) ||
                        (infoToLookFor.contains("is") && infoToLookFor.contains("information")) ||
                        (infoToLookFor.contains("does") && infoToLookFor.contains("say")))) {
            queryType = 2;
        }
        // handwritten text needs to be read
        else if(questionWord.equals("what") &&
                ((infoToLookFor.contains("is") && infoToLookFor.contains("printed")))) {
            queryType = 3;
        }

    }
}