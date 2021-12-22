package de.skuzzle.ghpromexporter.web;

import java.util.Arrays;
import java.util.List;

import de.skuzzle.ghpromexporter.scrape.ScrapeRepositoryRequest;
import reactor.core.publisher.Flux;

final class MultipleRepositories {

    private final String owner;
    private final List<String> repositories;

    private MultipleRepositories(String owner, List<String> repositories) {
        this.owner = owner;
        this.repositories = repositories;
    }

    public static MultipleRepositories parse(String owner, String repositoriesString) {
        final String[] repositories = repositoriesString.split(",");
        return new MultipleRepositories(owner, Arrays.asList(repositories));
    }

    Flux<ScrapeRepositoryRequest> requests() {
        return Flux.fromStream(repositories.stream()
                .map(repository -> ScrapeRepositoryRequest.of(owner, repository)));
    }

    @Override
    public String toString() {
        return "owner=%s, repositories=%s".formatted(owner, repositories);
    }
}
