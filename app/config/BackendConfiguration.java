package config;

public class BackendConfiguration {
    public static final int DEFAULT_MAX_RESULTS_FOR_ACTION_SEARCH = 3;
    public static final double MIN_MATCH_PERCENTAGE_FOR_FUZZY_ELEMENT_SEARCH_IN_SCREENS = 0.5;
    public static final double MAX_SCORE_GAP_FOR_GROUPING_TOP_RESULTS_IN_FUZZY_ELEMENT_SEARCH = 0.2;
    public static final double DEFAULT_MIN_MATCH_PERCENTAGE_FOR_SIMPLE_TEXT_INTERPRETER = 0.5;
    public static final int INTERVAL_FOR_BACKING_UP_SCREEN_MAP_MINUTES = 30;
    public static final boolean GET_TOP_ELEMENT_IF_MULTIPLE_MATCHES = true;
}
