package models;

public class SemanticActionMatchingTextAndScore implements Comparable {
    private String matchingDescription;
    private double confidenceScore;
    public SemanticActionMatchingTextAndScore(String matchingDescription, double confidenceScore) {
        this.matchingDescription = matchingDescription;
        this.confidenceScore = confidenceScore;
    }

    public String getMatchingDescription() {
        return matchingDescription;
    }

    public void setMatchingDescription(String matchingDescription) {
        this.matchingDescription = matchingDescription;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SemanticActionMatchingTextAndScore)) return false;

        SemanticActionMatchingTextAndScore that = (SemanticActionMatchingTextAndScore) o;

        if (Double.compare(that.confidenceScore, confidenceScore) != 0) return false;
        return matchingDescription.equals(that.matchingDescription);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = matchingDescription.hashCode();
        temp = Double.doubleToLongBits(confidenceScore);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public int compareTo(Object o) {
        SemanticActionMatchingTextAndScore otherScore = (SemanticActionMatchingTextAndScore)o;
        double diff = confidenceScore - otherScore.confidenceScore;
        if (diff > 0) {
            return 1;
        } else if (diff < 0) {
            return -1;
        }
        return 0;
    }
}
