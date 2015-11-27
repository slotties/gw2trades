package gw2trades.repository.api.model;

import java.util.List;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class SearchResult<T> {
    private List<T> results;
    private int totalResults;

    public SearchResult(List<T> results, int totalResults) {
        this.results = results;
        this.totalResults = totalResults;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public List<T> getResults() {
        return results;
    }
}
