package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import java.util.*;

public class Utils {
    public static String EMPTY_STRING = "";
    public static String PRINT_MODE = "DEBUG";

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

    //
//    public static List<String> generateKeywordsForFindingElement(String inputText) {
//        if (nullOrEmpty(inputText)) {
//            return new ArrayList<>();
//        }
//        List<String> list = new ArrayList<>();
//        //TODO: We should send down regex for matching text
//        list.add(replaceWord(inputText, ViewUtils.getMapForOnOffTemplateReplacement("on")));
//        list.add(replaceWord(inputText, ViewUtils.getMapForOnOffTemplateReplacement("off")));
//        //TODO: handle check box, seek bar and other stuff
//        //TODO: remove duplicates from the final string -- make sure each element in the list has no duplicates
//        return list;
//    }

    public static List<String> generateKeywordsForFindingElement(String inputText) {
        if (nullOrEmpty(inputText)) {
            return new ArrayList<>();
        }
//        inputText = inputText.replaceAll(ViewUtils.SWITCH_TEXT, "ON#OFF");
//        inputText = inputText.replaceAll(ViewUtils.ON_OFF_TEXT, "ON#OFF");
        //Remove duplicates
        Set<String> keywordListToReturn = new HashSet<>();
        List<String> inputWords = Arrays.asList(inputText.split(" "));
        for (String word: inputWords) {
            if (word.equals(ViewUtils.SWITCH_TEXT) || word.equals(ViewUtils.ON_OFF_TEXT)) {
                word = ViewUtils.ON_OFF_KEYWORD_REPLACEMENT;
            }
            if (!keywordListToReturn.contains(word)) {
                keywordListToReturn.add(word);
            }
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

    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortHashMapByValueDescending(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

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
        return input.replaceAll("[^\\w\\s]","").trim().toLowerCase();
    }
}