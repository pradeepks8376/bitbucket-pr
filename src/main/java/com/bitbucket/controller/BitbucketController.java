package com.bitbucket.controller;

import com.bitbucket.service.BitbucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bitbucket")
public class BitbucketController {

    @Autowired
    private BitbucketService bitbucketService;

    @GetMapping("/pullrequests")
    public ResponseEntity<String> listPullRequests(@RequestParam String repo, @RequestParam String state) {
        return bitbucketService.listPullRequests(repo, state);
    }

    @PostMapping("/pullrequests/{id}/checkout")
    public ResponseEntity<String> checkoutPullRequest(@RequestParam String repo, @PathVariable int id, @RequestParam String localPath) {
        return bitbucketService.checkoutPullRequest(repo, id, localPath);
    }

    @PostMapping("/pullrequests/{id}/approve")
    public ResponseEntity<String> approvePullRequest(@RequestParam String repo, @PathVariable int id) {
        return bitbucketService.approvePullRequest(repo, id);
    }

    @DeleteMapping("/pullrequests/{id}/approve")
    public ResponseEntity<String> unapprovePullRequest(@RequestParam String repo, @PathVariable int id) {
        return bitbucketService.unapprovePullRequest(repo, id);
    }

    @PostMapping("/pullrequests/{id}/decline")
    public ResponseEntity<String> declinePullRequest(@RequestParam String repo, @PathVariable int id) {
        return bitbucketService.declinePullRequest(repo, id);
    }

    @PostMapping("/pullrequests/{id}/merge")
    public ResponseEntity<String> mergePullRequest(@RequestParam String repo, @PathVariable int id, @RequestParam String message) {
        return bitbucketService.mergePullRequest(repo, id, message);
    }

    @DeleteMapping("/branches/{branchName}")
    public ResponseEntity<String> closeBranch(@RequestParam String repo, @PathVariable String branchName) {
        return bitbucketService.closeBranch(repo, branchName);
    }

    @GetMapping("/pullrequests/{id}")
    public ResponseEntity<String> getPullRequestDetails(@RequestParam String repo, @PathVariable int id) {
        return bitbucketService.getPullRequestDetails(repo, id);
    }


    @GetMapping("/pullrequests/{id}/squash-msg")
    public ResponseEntity<String> getSquashMergeMessage(@RequestParam String repo, @PathVariable int id) {
        return bitbucketService.getSquashMergeMessage(repo, id);
    }

    @PostMapping("/squash-merge")
    public void getSquashMergeMessage(@RequestParam String branchName,@RequestParam(required = false, defaultValue = "false") boolean force) {
         bitbucketService.mergeAndDeleteBranch(branchName, force);
    }
}
