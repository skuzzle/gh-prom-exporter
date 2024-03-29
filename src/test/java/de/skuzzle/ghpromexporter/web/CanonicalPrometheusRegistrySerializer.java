package de.skuzzle.ghpromexporter.web;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.skuzzle.test.snapshots.SnapshotException;
import de.skuzzle.test.snapshots.SnapshotSerializer;

/**
 * Creates a canonical view from a textual prometheus registry representation.
 */
class CanonicalPrometheusRegistrySerializer implements SnapshotSerializer {

    private final static String HEADER = "INFO: This registry is sorted and cleaned up from some random values to ensure deterministic testing behavior\n";
    private final static Pattern CREATED_GAUGE = Pattern.compile("^.+_created\\{.*");
    private final static Pattern SCRAPE_DURATION = Pattern.compile("github_scrape_duration_sum\\{.*");

    private CanonicalPrometheusRegistrySerializer() {
        // hidden
    }

    public static SnapshotSerializer canonicalPrometheusRegistry() {
        return new CanonicalPrometheusRegistrySerializer();
    }

    @Override
    public String serialize(Object testResult) throws SnapshotException {
        return reorderAndFilter(testResult.toString());
    }

    private String reorderAndFilter(String s) {
        return Stream.concat(Stream.of(HEADER), s.lines()
                .filter(this::filterCreatedGauge)
                .filter(this::filterScrapeDuation)
                .sorted())
                .collect(Collectors.joining("\n"));
    }

    private boolean filterCreatedGauge(String line) {
        return !CREATED_GAUGE.matcher(line).matches();
    }

    private boolean filterScrapeDuation(String line) {
        return !SCRAPE_DURATION.matcher(line).matches();
    }

}
