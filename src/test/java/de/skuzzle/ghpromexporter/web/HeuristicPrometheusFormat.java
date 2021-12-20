package de.skuzzle.ghpromexporter.web;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.skuzzle.test.snapshots.SnapshotException;
import de.skuzzle.test.snapshots.StructuralAssertions;
import de.skuzzle.test.snapshots.data.text.TextDiffStructuralAssertions;

public class HeuristicPrometheusFormat implements StructuralAssertions {

    private final static Pattern CREATED_GAUGE = Pattern.compile("^.+_created\\{.*");
    private final static Pattern SCRAPE_DURATION = Pattern.compile("github_scrape_duration_sum\\{.*");

    private HeuristicPrometheusFormat() {
        // hidden
    }

    public static StructuralAssertions heuristicallyComparePrometheusFormat() {
        return new HeuristicPrometheusFormat();
    }

    @Override
    public void assertEquals(String storedSnapshot, String serializedActual) throws AssertionError, SnapshotException {
        final String snapshotSorted = reorderAndFilter(storedSnapshot);
        final String actualSorted = reorderAndFilter(serializedActual);

        new TextDiffStructuralAssertions().assertEquals(snapshotSorted, actualSorted);
    }

    private String reorderAndFilter(String s) {
        return s.lines()
                .filter(this::filterCreatedGauge)
                .filter(this::filterScrapeDuation)
                .sorted()
                .collect(Collectors.joining("\n"));
    }

    private boolean filterCreatedGauge(String line) {
        return !CREATED_GAUGE.matcher(line).matches();
    }

    private boolean filterScrapeDuation(String line) {
        return !SCRAPE_DURATION.matcher(line).matches();
    }

}
