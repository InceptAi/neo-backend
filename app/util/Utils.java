package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.MatchingInfo;
import play.libs.Json;

import java.util.*;

public class Utils {
    public static final String EMPTY_STRING = "";
    private static final String PRINT_MODE = "DEBUG";
    public static final String SETTINGS_TITLE = "Settings";
    public static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    public static final String SETTINGS_SUBTITLE = "Wireless & networks";
    public static ObjectNode createResponse(Object response, boolean ok) {
        ObjectNode result = Json.newObject();
        result.put("isSuccessfull", ok);
        if (response instanceof String)
            result.put("body", (String) response);
        else result.set("body", (JsonNode) response);

        return result;
    }

    public static ObjectNode createSimpleResponse(Object response, boolean ok) {
        ObjectNode result = Json.newObject();
        result.put("isSuccessfull", ok);
        if (response instanceof String)
            result.put("body", (String) response);
        else result.set("body", (JsonNode) response);

        return result;
    }


    public static boolean nullOrEmpty(String target) {
        return target == null || target.isEmpty() || target.equals("null");
    }

    public static List<String> splitSentenceToWords(String sentence) {
        if (nullOrEmpty(sentence)) {
            return new ArrayList<>();
        }
        return Arrays.asList(sentence.split(" "));
    }

    public static String combineWordsToSentence(List<String> words) {
        if (words == null) {
            return Utils.EMPTY_STRING;
        }
        StringBuilder stringBuilder  = new StringBuilder();
        for (String word: words) {
            stringBuilder.append(word);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString().toLowerCase().trim();
    }

    public static void printDebug(String stringToPrint) {
        if (PRINT_MODE.equalsIgnoreCase("DEBUG")) {
            System.out.println(stringToPrint);
        }
    }

    public static boolean containsWord(String sentence, String word) {
        if(nullOrEmpty(sentence) || nullOrEmpty(word)) {
            return false;
        }
        List<String> words = splitSentenceToWords(sentence);
        for (String inputWord: words) {
            if (inputWord.trim().equalsIgnoreCase(word.trim())) {
                return true;
            }
        }
        return false;
    }

    public static String replaceWord(String inputSentence, HashMap<String, String> replacementMap) {
        Utils.printDebug("In replaceWord, replacing: " + inputSentence);
        if (inputSentence == null) {
            return null;
        }

        if (replacementMap == null) {
            return inputSentence;
        }

        List<String> words = Arrays.asList(inputSentence.split(" "));
        StringBuilder replacedSentenceBuilder = new StringBuilder();
        for (String word: words) {
            String replacementWord = replacementMap.get(word.toLowerCase());
            if (replacementWord != null) {
                replacedSentenceBuilder.append(replacementWord);
                replacedSentenceBuilder.append(" ");
            } else {
                replacedSentenceBuilder.append(word);
                replacedSentenceBuilder.append(" ");
            }
        }
        Utils.printDebug("In replaceWord, output: " + replacedSentenceBuilder.toString().trim().toLowerCase());
        return replacedSentenceBuilder.toString().trim().toLowerCase();
    }

    public static String removeSuccessiveDuplicateWords(String input) {
        if (Utils.nullOrEmpty(input)) {
            return Utils.EMPTY_STRING;
        }
        List<String> words = splitSentenceToWords(input);
        List<String> outputWords = new ArrayList<>();
        String lastWord = Utils.EMPTY_STRING;
        for (String word: words) {
            if (!word.equalsIgnoreCase(lastWord)) {
                outputWords.add(word);
            }
            lastWord = word;
        }
        return combineWordsToSentence(outputWords);
    }

    public static List<String> generateKeywordsForFindingElement(String inputText) {
        if (nullOrEmpty(inputText)) {
            return new ArrayList<>();
        }
        //Remove duplicates
        Set<String> keywordListToReturn = new HashSet<>();
        List<String> inputWords = Arrays.asList(inputText.split(" "));
        for (String word: inputWords) {
            if (Utils.nullOrEmpty(word) ||
                    word.equalsIgnoreCase(ViewUtils.SWITCH_TEXT) ||
                    word.equalsIgnoreCase(ViewUtils.CHECK_BOX_TEXT) ||
                    word.equalsIgnoreCase(ViewUtils.NUMBER_REPLACEMENT) ||
                    word.equalsIgnoreCase(ViewUtils.EMAIL_REPLACEMENT)) {
                continue; //We don't need to match the switch text specifically -- since we will be matching
                // other keywords for context
            }

            if (word.equalsIgnoreCase(ViewUtils.ON_OFF_TEXT)) {
                word = ViewUtils.ON_OFF_KEYWORD_REPLACEMENT;
            }


            if (!keywordListToReturn.contains(word)) {
                keywordListToReturn.add(word);
            }

            //TODO: File Bug in accessibilitySearchByText -- doesn't match switch class text
            // if (word.equals(ViewUtils.SWITCH_TEXT) || word.equals(ViewUtils.ON_OFF_TEXT) || word.equals(ViewUtils.CHECK_BOX_TEXT)) {
            //       word = ViewUtils.ON_OFF_KEYWORD_REPLACEMENT;
            //   }

        }
        return new ArrayList<>(keywordListToReturn);
    }


    public static String removeDuplicateWords(String input) {
        if (nullOrEmpty(input)) {
            return input;
        }
        StringBuilder toReturn = new StringBuilder();
        HashSet<String> stringHashSet = new HashSet<>();
        List<String> inputWords = Arrays.asList(input.split(" "));
        for (String word: inputWords) {
            if (!stringHashSet.contains(word)) {
                stringHashSet.add(word);
                toReturn.append(word);
                toReturn.append(" ");
            }
        }
        return toReturn.toString().trim().toLowerCase();
    }

    @SuppressWarnings("Java8ListSort")
    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortHashMapByValueDescending(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, (o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static String sanitizeText(String input) {
        if (nullOrEmpty(input)) {
            return input;
        }
        //input = input.replaceAll("[^\\w\\s]","").trim().toLowerCase();
        input = input.replaceAll("[^\\w\\s.@]","").trim().toLowerCase();
        //Squash multiple consecutive white spaces
        input = input.replaceAll("\\s+"," ");
        return input;
    }

    public static double getMatchingScore(MatchingInfo matchingInfo1, MatchingInfo matchingInfo2,
                                   boolean fuzzyMatch, boolean prioritizeDevice) {
        final double MAX_MATCHING_SCORE = 10;

        if (matchingInfo1 == null || matchingInfo2 == null) {
            return 0;
        }

        if (matchingInfo1.equals(matchingInfo2)) {
            return MAX_MATCHING_SCORE;
        }

        if (!fuzzyMatch) {
            return 0;
        }

        double matchingScore = 1;

        if (prioritizeDevice) {
            if (matchingInfo1.getPhoneManufacturer().equalsIgnoreCase(matchingInfo2.getPhoneManufacturer())) {
                matchingScore += 4.0;
            }
            if (matchingInfo1.getRelease().equalsIgnoreCase(matchingInfo2.getRelease())) {
                matchingScore += 3.0;
            }
            if (matchingInfo1.getPhoneModel().equalsIgnoreCase(matchingInfo2.getPhoneModel())) {
                matchingScore += 2.0;
            }
        } else {
            if (matchingInfo1.getAppVersion().equalsIgnoreCase(matchingInfo2.getAppVersion())) {
                matchingScore += 7.0;
            }
            if (matchingInfo1.getVersionCode().equalsIgnoreCase(matchingInfo2.getVersionCode())) {
                matchingScore += 2.0;
            }
        }
        return matchingScore;
    }
}