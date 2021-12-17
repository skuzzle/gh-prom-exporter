package de.skuzzle.ghpromexporter.scrape;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;

import org.junit.jupiter.api.Test;

import de.skuzzle.ghpromexporter.github.GitHubAuthentication;
import de.skuzzle.ghpromexporter.scrape.RegistrationRepository.RegisteredScraper;

public class RegistrationRepositoryTest {

    @Test
    void testSerialize() throws Exception {
        final GitHubAuthentication token = GitHubAuthentication.token("testitest");
        final GitHubAuthentication anon = GitHubAuthentication.anonymous(InetAddress.getLocalHost());
        final RegisteredScraper registeredScraper = new RegisteredScraper(anon,
                new ScrapeRepositoryRequest("skuzzle", "gh-prom-exporter"));

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(registeredScraper);

        final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

        final Object readObject = objectInputStream.readObject();
        assertThat(readObject).isInstanceOf(RegisteredScraper.class);
    }
}
