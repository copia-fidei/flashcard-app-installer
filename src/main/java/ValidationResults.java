import java.util.ArrayList;
import java.util.List;

public class ValidationResults {
    private final List<ValidationResult> results = new ArrayList<>();

    public ValidationResults(List<ValidationResult> results) {
        this.results.addAll(results);
    }

    public List<ValidationResult> getResults() {
        return results;
    }

    public boolean isValid() {
       return results.isEmpty() || results.stream().noneMatch(result -> result.getSeverity() != ValidationResult.Severity.ERROR);
    }
}
