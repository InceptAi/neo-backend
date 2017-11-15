package storage;

import models.*;
import nlu.TextInterpreter;
import util.Utils;

import javax.inject.Singleton;
import java.util.*;

@Singleton
public class SingleDeviceSemanticActionStore implements SemanticActionStore {
    private Map<String, SemanticAction> semanticActionMap = new HashMap<>();

    @Override
    public SemanticAction getAction(String id) {
        return semanticActionMap.get(id);
    }

    @Override
    public Set<SemanticAction> getAllActions() {
        return new HashSet<>(semanticActionMap.values());
    }

    @Override
    public HashMap<String, SemanticActionMatchingTextAndScore> returnAllActionsWithScores() {
        HashMap<String, SemanticActionMatchingTextAndScore> hashMapToReturn = new HashMap<>();
        for (SemanticAction semanticAction : semanticActionMap.values()) {
            hashMapToReturn.put(
                    semanticAction.fetchSemanticActionId(),
                    new SemanticActionMatchingTextAndScore(semanticAction.getSemanticActionDescription(), 0));
        }
        return hashMapToReturn;
    }

    @Override
    public HashMap<String, SemanticActionMatchingTextAndScore> searchActions(
            String inputText,
            String packageName,
            MatchingInfo matchingInfo,
            TextInterpreter textInterpreter,
            int maxResults) {
        HashMap<String, Double> semanticActionIdToMatchMetric = new HashMap<>();
        HashMap<String, String> semanticActionIdToBestMatchingString = new HashMap<>();
        double minMetricInserted = Double.MAX_VALUE;
        String minIdInserted = Utils.EMPTY_STRING;
        for (SemanticAction semanticAction: semanticActionMap.values()) {
            if (matchingInfo != null && !semanticAction.getMatchingInfo().equals(matchingInfo)) {
                //Match the device info here
                return new HashMap<>();
            }
            List<String> referenceStringList = semanticAction.fetchStringsToMatch();
            double bestMatchMetric = 0;
            String bestMatchingString = Utils.EMPTY_STRING;
            for (String referenceString: referenceStringList) {
                double matchMetric = textInterpreter.getMatchMetric(inputText, referenceString);
                if (matchMetric > bestMatchMetric) {
                    bestMatchMetric = matchMetric;
                    bestMatchingString = referenceString;
                }
            }
            if (bestMatchMetric > 0) {
                semanticActionIdToBestMatchingString.put(semanticAction.fetchSemanticActionId(), bestMatchingString);
                semanticActionIdToMatchMetric.put(semanticAction.fetchSemanticActionId(), bestMatchMetric);
            }
        }

        //Sort the hash maps and return
        LinkedHashMap<String, SemanticActionMatchingTextAndScore> sortedSemanticActionIdToDescriptionAndScore = new LinkedHashMap<>();
        Map<String, Double> sortedMetricMap = Utils.sortHashMapByValueDescending(semanticActionIdToMatchMetric);
        int numActions = 0;
        for (HashMap.Entry<String, Double> entry : sortedMetricMap.entrySet()) {
            if (numActions >= maxResults) {
                break;
            }
            String bestMatchingStringForId = semanticActionIdToBestMatchingString.get(entry.getKey());
            sortedSemanticActionIdToDescriptionAndScore.put(
                    entry.getKey(),
                    new SemanticActionMatchingTextAndScore(bestMatchingStringForId, entry.getValue()));
            numActions++;
        }
        return sortedSemanticActionIdToDescriptionAndScore;
    }


    @Override
    public void updateSemanticActionStore(UIScreen uiScreen) {
        //Iterate through all the ui elements and add semantic actions for clickable ones
        if (uiScreen == null) {
            return;
        }
        for (SemanticAction semanticAction: uiScreen.getSemanticActions().values()) {
            addSemanticAction(semanticAction);
        }
    }

    private void addSemanticAction(SemanticAction semanticAction) {
        String key = semanticAction.fetchSemanticActionId();
        semanticActionMap.put(key, semanticAction);
    }
}
