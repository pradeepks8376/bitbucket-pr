package com.bitbucket.service;

import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class BitbucketService {

    @Value("${bitbucket.api.url}")
    private String bitbucketApiUrl;

    @Value("${bitbucket.username}")
    private String username;

    @Value("${bitbucket.password}")
    private String password;

    @Value("${bitbucket.repoSlug}")
    private String repoSlug;

    private final RestTemplate restTemplate;

    public BitbucketService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders createHeaders1() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public ResponseEntity<String> listPullRequests(String repo, String state) {
        String url = String.format("%s/repositories/%s", bitbucketApiUrl, repo);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url+"/pullrequests")
                .queryParam("state", state);

        HttpEntity<String> entity = new HttpEntity<>(createHeaders1());
        return restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);
    }

    public ResponseEntity<String> checkoutPullRequest(String repo, int pullRequestId, String localPath) {
        try {
            String url = String.format("%s/pull-requests/"+pullRequestId, repo);
            String repoUrl = String.format("https://bitbucket.org/%s", url);

            Git git = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(new File(localPath))
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                    .call();

            git.checkout().setName("master").call();
            git.close();
            return ResponseEntity.ok("Checked out pull request #" + pullRequestId + " to " + localPath);

        } catch (GitAPIException gitAPIException) {
            gitAPIException.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error checking out pull request: " + gitAPIException.getMessage());
        }
    }

    public ResponseEntity<String> approvePullRequest(String repo, int pullRequestId) {
        String url = String.format("%s/repositories/%s/pullrequests/%d/approve", bitbucketApiUrl, repo, pullRequestId);

        HttpEntity<String> entity = new HttpEntity<>(createHeaders1());
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    public ResponseEntity<String> unapprovePullRequest(String repo, int pullRequestId) {
        String url = String.format("%s/repositories/%s/pullrequests/%d/approve", bitbucketApiUrl, repo, pullRequestId);

        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        return restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
    }

    public ResponseEntity<String> declinePullRequest(String repo, int pullRequestId) {
        String url = String.format("%s/repositories/%s/pullrequests/%d/decline", bitbucketApiUrl, repo, pullRequestId);

        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    public ResponseEntity<String> mergePullRequest(String repo, int pullRequestId, String message) {
        String url = String.format("%s/repositories/%s/pullrequests/%d/merge", bitbucketApiUrl, repo, pullRequestId);

        String requestBody = String.format("{\"message\": \"%s\"}", message);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, createHeaders());
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    public ResponseEntity<String> closeBranch(String repo, String branchName) {
        String url = String.format("%s/repositories/%s/refs/branches/%s", bitbucketApiUrl, repo, branchName);

        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        return restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
    }

    public ResponseEntity<String> getPullRequestDetails(String repo, int pullRequestId) {
        String url = String.format("%s/repositories/%s/pullrequests/%d", bitbucketApiUrl, repo, pullRequestId);

        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    public ResponseEntity<String> getSquashMergeMessage(String repo, int pullRequestId) {
        String url = String.format("%s/repositories/%s/pullrequests/%d", bitbucketApiUrl, repo, pullRequestId);

        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // Extract the squash message from the response (customize as per your response structure)
        String squashMessage = "Default squash merge message";
        return ResponseEntity.ok(squashMessage);
    }

    public ResponseEntity<String> squashMergePullRequest(String repo, int pullRequestId, String message) {
        String url = String.format("%s/repositories/%s/pullrequests/%d/merge", bitbucketApiUrl, repo, pullRequestId);

        String requestBody = String.format("{\"message\": \"%s\", \"close_source_branch\": true, \"merge_strategy\": \"squash\"}", message);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, createHeaders());
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    public void setBitbucketApiUrl(String bitbucketApiUrl) {
        this.bitbucketApiUrl = bitbucketApiUrl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPullRequestId(String branchName) {
        String url = String.format("%s/repositories/%s/pullrequests?source.branch.name=%s", bitbucketApiUrl, repoSlug, branchName);
        HttpEntity<String> entity = new HttpEntity<>(createHeaders1());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        // Extract PR ID from response
        // This is a placeholder, implement the correct JSON parsing logic
        return "3";
    }

    public void squashMergePullRequest(String prId, boolean force) {
        String url = UriComponentsBuilder.fromHttpUrl(bitbucketApiUrl)
                .pathSegment("repositories", "jwalton/git-scripts", "pullrequests", prId, "merge")
                .toUriString();
//        HttpEntity<String> entity = new HttpEntity<>(createHeaders1());
        String body = "{\"merge_strategy\": \"squash\"}";
        HttpHeaders headers = new HttpHeaders(createHeaders1());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        if (force) {
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        } else {
            // Check PR settings before merging
            // Implement the logic to verify PR settings
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        }
    }

    public void deleteBranch(String branchName) {
        String url = String.format("%s/repositories/%s/refs/branches/%s", bitbucketApiUrl, repoSlug, branchName);
        HttpEntity<String> entity = new HttpEntity<>(createHeaders1());
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    public void mergeAndDeleteBranch(String branchName, boolean force) {
        String prId = getPullRequestId(branchName);
        squashMergePullRequest(prId, force);
        deleteBranch(branchName);
    }
}


