package nlu;

import config.BackendConfiguration;
import util.Utils;

import java.util.*;

public class SimpleTextInterpreter implements TextInterpreter {
    private final double minMatchPercentage;

    public SimpleTextInterpreter(double minMatchPercentage) {
        this.minMatchPercentage = minMatchPercentage;
    }

    public SimpleTextInterpreter() {
        this.minMatchPercentage = BackendConfiguration.DEFAULT_MIN_MATCH_PERCENTAGE_FOR_SIMPLE_TEXT_INTERPRETER;
    }

    @Override
    public String sanitizeInputTextForMatching(String inputText) {
        //TODO: Remove special characters, also remove plurals
        if (Utils.nullOrEmpty(inputText)) {
            return inputText;
        }
        return inputText.toLowerCase();
    }

    @Override
    public double getMatchMetric(String inputText, String referenceText, double minThresholdForMatching) {
        //Break input text into words, and see how many words occur in reference text
        if (Utils.nullOrEmpty(inputText) || Utils.nullOrEmpty(referenceText)) {
            return 0;
        }

        //Sanitize the text
        inputText = sanitizeInputTextForMatching(inputText);
        referenceText = sanitizeInputTextForMatching(referenceText);

        List<String> referenceWords = Arrays.asList(referenceText.split(" "));
        final Set<String> referenceSet = new HashSet<String>(referenceWords);

        int numMatches = 0;
        List<String> inputWords = Arrays.asList(inputText.split(" "));
        for (String inputWord: inputWords) {
            if (referenceSet.contains(getSingularForm(inputWord)) || referenceSet.contains(getPluralForm(inputWord))) {
                numMatches++;
            }
        }

        double inputMatch = (double)numMatches / inputWords.size();
        if (inputMatch < minMatchPercentage) {
            return 0;
        }

        double overallMatch = (double)numMatches / (inputWords.size() + referenceSet.size());
        //Normalize -- max value can be 0.5, min is 0
        return overallMatch * 2.0;
    }

    @Override
    public double getMatchMetric(String inputText, String referenceText) {
        return getMatchMetric(inputText, referenceText, minMatchPercentage);
    }

    private String getPluralForm(String word) {
        if (Utils.nullOrEmpty(word)) {
            return Utils.EMPTY_STRING;
        }

        if (word.endsWith("s")) {
            return word;
        } else {
            return word + "s";
        }
    }

    private String getSingularForm(String word) {
        if (Utils.nullOrEmpty(word)) {
            return Utils.EMPTY_STRING;
        }
        if (word.endsWith("s")) {
            return word.substring(0, word.length() - 1);
        } else {
            return word;
        }
    }
}
