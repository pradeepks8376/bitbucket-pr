package com.bitbucket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BitbucketServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BitbucketService bitbucketService;

    @Value("${bitbucket.api.url}")
    private String bitbucketApiUrl;

    @Value("${bitbucket.username}")
    private String username;

    @Value("${bitbucket.password}")
    private String password;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        bitbucketService = new BitbucketService(restTemplate);
        bitbucketService.setBitbucketApiUrl("https://api.bitbucket.org/2.0");
        bitbucketService.setUsername("my_bitbucket_username");
        bitbucketService.setPassword("my_bitbucket_app_password");
    }

    @Test
    public void testListPullRequests() {
        String repo = "test-repo";
        String state = "OPEN";
        String url = String.format("%s/repositories/%s/pullrequests?state=%s", bitbucketApiUrl, repo, state);

        ResponseEntity<String> responseEntity = new ResponseEntity<>("{\"pullrequests\": []}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))).thenReturn(responseEntity);

        ResponseEntity<String> response = bitbucketService.listPullRequests(repo, state);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void testGetSquashMergeMessage() {
        String repo = "test-repo";
        int pullRequestId = 1;
        String url = String.format("%s/repositories/%s/pullrequests/%d", bitbucketApiUrl, repo, pullRequestId);

        ResponseEntity<String> responseEntity = new ResponseEntity<>("{\"title\": \"Test PR\"}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))).thenReturn(responseEntity);

        ResponseEntity<String> response = bitbucketService.getSquashMergeMessage(repo, pullRequestId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void testSquashMergePullRequest() {
        String repo = "test-repo";
        int pullRequestId = 1;
        String message = "Squash merge message";
        String url = String.format("%s/repositories/%s/pullrequests/%d/merge", bitbucketApiUrl, repo, pullRequestId);

        ResponseEntity<String> responseEntity = new ResponseEntity<>("{\"merge_commit\": {}}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class))).thenReturn(responseEntity);

        ResponseEntity<String> response = bitbucketService.squashMergePullRequest(repo, pullRequestId, message);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }
}
