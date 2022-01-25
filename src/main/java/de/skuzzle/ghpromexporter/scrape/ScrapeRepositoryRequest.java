package de.skuzzle.ghpromexporter.scrape;

import java.util.Objects;

public record ScrapeRepositoryRequest(String owner, String repository) {

    public static ScrapeRepositoryRequest of(String owner, String repository) {
        return new ScrapeRepositoryRequest(
                Objects.requireNonNull(owner, "owner must not be null"),
                Objects.requireNonNull(repository, "repository must not be null"));
    }

    public String owner() {
        return this.owner;
    }

    public String name() {
        return this.repository;
    }

    public String repositoryFullName() {
        return owner + "/" + repository;
    }

    @Override
    public String toString() {
        return repositoryFullName();
    }
}
