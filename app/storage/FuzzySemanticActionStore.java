package storage;

import models.MatchingInfo;
import models.SemanticAction;
import models.SemanticActionMatchingTextAndScore;
import models.UIScreen;
import nlu.TextInterpreter;
import util.Utils;

import javax.inject.Singleton;
import java.util.*;

@Singleton
public class FuzzySemanticActionStore implements SemanticActionStore {
    private Map<String, SemanticAction> semanticActionMap = new HashMap<>();
    private Map<String, List<SemanticAction>> packageNameToSemanticActionsMap = new HashMap<>();
    private Set<String> systemPackageNameList = new HashSet<>();

    @Override
    public SemanticAction getAction(String id) {
        return semanticActionMap.get(id);
    }

    @Override
    public Set<SemanticAction> getAllActions() {
        return new HashSet<>(semanticActionMap.values());
    }

    @Override
    public Map<String, SemanticActionMatchingTextAndScore> returnAllActionsWithScores() {
        HashMap<String, SemanticActionMatchingTextAndScore> hashMapToReturn = new HashMap<>();
        for (SemanticAction semanticAction : semanticActionMap.values()) {
            hashMapToReturn.put(
                    semanticAction.fetchSemanticActionId(),
                    new SemanticActionMatchingTextAndScore(semanticAction.getSemanticActionDescription(), 0));
        }
        return hashMapToReturn;
    }

    @Override
    public Map<String, SemanticActionMatchingTextAndScore> searchActions(
            String inputText,
            String packageName,
            MatchingInfo matchingInfo,
            TextInterpreter textInterpreter,
            int maxResults) {
        //TODO: handle cases where package name changes even within settings -- like cellular broadcast settings
        //TODO: In this scenario we don't know the exact package name
        if (!matchingInfo.isSystemPackage()) {
            return searchActionsForPackageNameFuzzy(inputText, packageName, matchingInfo, textInterpreter, maxResults);
        }

        HashMap<String, SemanticActionMatchingTextAndScore> matchingActionMap = new HashMap<>();
        //Search all non apps packageName
        for (String systemPackageName: systemPackageNameList) {
            matchingActionMap.putAll(searchActionsForPackageNameFuzzy(inputText, systemPackageName,
                    matchingInfo, textInterpreter, maxResults));
        }
        //TODO: sort all the actions and then return max results
        Map<String, SemanticActionMatchingTextAndScore> sortedTextAndMetricMap = Utils.sortHashMapByValueDescending(matchingActionMap);
        LinkedHashMap<String, SemanticActionMatchingTextAndScore> sortedTextAndMetricMapLimited = new LinkedHashMap<>();
        int numActions = 0;
        for (Map.Entry<String, SemanticActionMatchingTextAndScore> entry : sortedTextAndMetricMap.entrySet()) {
            if (numActions >= maxResults) {
                break;
            }
            sortedTextAndMetricMapLimited.put(entry.getKey(), entry.getValue());
            numActions++;
        }
        return sortedTextAndMetricMapLimited;
    }



    @Override
    public void updateSemanticActionStore(UIScreen uiScreen) {
        //Iterate through all the ui elements and add semantic actions for clickable ones
        if (uiScreen == null) {
            return;
        }
        for (SemanticAction semanticAction: uiScreen.getSemanticActions().values()) {
            addSemanticAction(semanticAction);
            if (uiScreen.getMatchingInfo().isSystemPackage()) {
                systemPackageNameList.add(uiScreen.getPackageName());
            }
        }
    }

    private void addSemanticAction(SemanticAction semanticAction) {
        String key = semanticAction.fetchSemanticActionId();
        semanticActionMap.put(key, semanticAction);
        List<SemanticAction> actionList = packageNameToSemanticActionsMap.get(semanticAction.getPackageName());
        if (actionList == null) {
            actionList = new ArrayList<>();
            packageNameToSemanticActionsMap.put(semanticAction.getPackageName(), actionList);
        }
        actionList.add(semanticAction);
    }

    public Map<String, SemanticActionMatchingTextAndScore> searchActionsForPackageNameFuzzy(
            String inputText,
            String packageName,
            MatchingInfo matchingInfo,
            TextInterpreter textInterpreter,
            int maxResults) {
        List<SemanticAction> semanticActionList = packageNameToSemanticActionsMap.get(packageName);
        if (semanticActionList == null || semanticActionList.isEmpty() && !matchingInfo.isSystemPackage()) {
            //We have nothing for this package period -- return
            return new HashMap<>();
        }
        return fuzzyFindSemanticActionInList(semanticActionList, textInterpreter,
                inputText, matchingInfo, maxResults);
    }


    private Map<String, SemanticActionMatchingTextAndScore> fuzzyFindSemanticActionInList(List<SemanticAction> semanticActionList,
                                                                                          TextInterpreter textInterpreter,
                                                                                          String inputText,
                                                                                          MatchingInfo inputMatchingInfo,
                                                                                          int maxResults) {
        HashMap<String, Double> semanticActionIdToMatchMetric = new HashMap<>();
        HashMap<String, String> semanticActionIdToBestMatchingString = new HashMap<>();
        for (SemanticAction semanticAction: semanticActionList) {
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
                double matchingScore = Utils.getMatchingScore(
                        semanticAction.getMatchingInfo(),
                        inputMatchingInfo,
                        true,
                        inputMatchingInfo.isSystemPackage());
                bestMatchMetric = bestMatchMetric * matchingScore;
                if (bestMatchMetric > 0) {
                    semanticActionIdToBestMatchingString.put(semanticAction.fetchSemanticActionId(), bestMatchingString);
                    semanticActionIdToMatchMetric.put(semanticAction.fetchSemanticActionId(), bestMatchMetric);
                }
            }
        }

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



}
