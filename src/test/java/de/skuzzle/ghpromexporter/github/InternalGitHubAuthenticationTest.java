package de.skuzzle.ghpromexporter.github;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import de.skuzzle.ghpromexporter.github.InternalGitHubAuthentication.BasicAuthentication;
import de.skuzzle.ghpromexporter.github.InternalGitHubAuthentication.TokenAuthentication;

public class InternalGitHubAuthenticationTest {

    @Test
    void token_should_never_be_contained_in_string_representation() throws Exception {
        final TokenAuthentication authentication = new TokenAuthentication("token-string");
        assertThat(authentication.toString()).doesNotContain("token-string");
    }

    @Test
    void password_should_never_be_contained_in_string_representation() throws Exception {
        final BasicAuthentication authentication = new BasicAuthentication("username", "token-string");
        assertThat(authentication.toString()).doesNotContain("token-string");
    }
}
