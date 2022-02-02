package de.skuzzle.ghpromexporter.scrape;

import java.util.Objects;

public record ScrapeTarget(String owner, String repository) {

    public static ScrapeTarget of(String owner, String repository) {
        return new ScrapeTarget(
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
