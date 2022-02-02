package de.skuzzle.ghpromexporter.web;

import java.util.Arrays;
import java.util.List;

import de.skuzzle.ghpromexporter.scrape.ScrapeTarget;
import reactor.core.publisher.Flux;

final class MultipleScrapeTargets {

    private final String owner;
    private final List<String> repositories;

    private MultipleScrapeTargets(String owner, List<String> repositories) {
        this.owner = owner;
        this.repositories = repositories;
    }

    public static MultipleScrapeTargets parse(String owner, String repositoriesString) {
        final String[] repositories = repositoriesString.split(",");
        return new MultipleScrapeTargets(owner, Arrays.asList(repositories));
    }

    Flux<ScrapeTarget> requests() {
        return Flux.fromStream(repositories.stream()
                .map(repository -> ScrapeTarget.of(owner, repository)));
    }

    @Override
    public String toString() {
        return "owner=%s, repositories=%s".formatted(owner, repositories);
    }
}
