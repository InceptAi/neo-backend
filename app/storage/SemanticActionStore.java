package storage;

import models.MatchingInfo;
import models.SemanticAction;
import models.SemanticActionMatchingTextAndScore;
import models.UIScreen;
import nlu.TextInterpreter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface SemanticActionStore {
    void updateSemanticActionStore(UIScreen uiScreen);
    SemanticAction getAction(String id);
    Set<SemanticAction> getAllActions();
    Map<String, SemanticActionMatchingTextAndScore> returnAllActionsWithScores();
    Map<String, SemanticActionMatchingTextAndScore> searchActions(
            String inputText,
            String packageName,
            MatchingInfo matchingInfo,
            TextInterpreter textInterpreter,
            int maxResults);
}
