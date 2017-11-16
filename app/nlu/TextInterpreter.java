package nlu;

public interface TextInterpreter {
    String sanitizeInputTextForMatching(String inputText);
    double getMatchMetric(String inputText, String referenceText);
    double getMatchMetric(String inputText, String referenceText, double minThresholdForMatching);
}
