package com.ecs160.hw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import com.ecs160.hw.model.Issue;
import com.ecs160.hw.model.Repo;
import com.ecs160.microservices.BugFinderMicroservice;
import com.ecs160.microservices.IssueComparatorMicroservice;
import com.ecs160.microservices.IssueSummarizerMicroservice;
import com.ecs160.persistence.RedisDB;

public class AppTest {

    // This test relies on redis being up and running
    @Test 
    public void completePersistAndLoadTest() {
        RedisDB db = new RedisDB();
        System.out.println("Flushing Redis DB...");

        Map<String, String> repoMap = new HashMap<>();
        repoMap.put("name", "testrepo");
        repoMap.put("fullName", "joevogel/testrepo");
        repoMap.put("ownerLogin", "joevogel");
        repoMap.put("htmlUrl", "google.com");
        repoMap.put("openIssuesCount", "10000");
        repoMap.put("createdAt", "2025");

        Map<String, String> issueMap = new HashMap<>();
        issueMap.put("id", "15533");
        issueMap.put("title", "Bug in code");
        issueMap.put("date", "2025");
        issueMap.put("description", "I think there is a bug here on line 23");

        Map<String, String> issueMap2 = new HashMap<>();
        issueMap2.put("id", "67890");
        issueMap2.put("title", "Other bug in code");
        issueMap2.put("date", "2023");
        issueMap2.put("description", "I think there is a bug here on line 93");

        Issue dummyIssue = new Issue(issueMap);
        Issue dummyIssue2 = new Issue(issueMap2);

        List<Issue> issues = new ArrayList<>();
        issues.add(dummyIssue);
        issues.add(dummyIssue2);

        Repo myRepo = new Repo(repoMap);
        myRepo.setIssues(issues);
        myRepo.setId("201");
        System.out.println("Persisting example repo");
        db.persist(myRepo);

        // Now try loading functionality
        Repo newRepo = new Repo("201");
        System.out.println("Repo before loading: ");
        System.out.println(newRepo.toMap().toString());

        Repo proxyRepo = (Repo) db.load(newRepo);

        System.out.println("Repo after loading: ");
        System.out.println(newRepo.toMap().toString());

        // See if issue children were loaded
        List<Issue> loadedIssues = proxyRepo.getIssues();
        System.out.println("Loaded issues: ");
        for (Issue issue : loadedIssues) {
            System.out.println(issue.toMap().toString());
        }

        assertEquals(2, loadedIssues.size());
        assertEquals("15533", loadedIssues.get(0).getId());
        assertEquals("67890", loadedIssues.get(1).getId());
        assertEquals("Bug in code", loadedIssues.get(0).getTitle());
        assertEquals("Other bug in code", loadedIssues.get(1).getTitle());
    }

    // Simple lightweight test: provide a dummy subclass that overrides the remote call
    @Test
    public void issueSummarizer_dummyReturnsExpected() {
        class Dummy extends IssueSummarizerMicroservice {
            public String lastInput = null;
            @Override
            public String summarizeIssue(String issueJson) {
                this.lastInput = issueJson;
                return "{\"bug_type\":\"X\"}";
            }
        }

        Dummy d = new Dummy();
        String out = d.summarizeIssue("some issue text");
        assertNotNull(out);
        assertEquals("{\"bug_type\":\"X\"}", out);
        assertEquals("some issue text", d.lastInput);
    }

    // Simple lightweight test: subclass BugFinderMicroservice
    @Test
    public void bugFinder_dummyReturnsExpected() {
        class Dummy extends BugFinderMicroservice {
            public String lastInput = null;
            @Override
            public String findBugs(String code) {
                this.lastInput = code;
                return "[]";
            }
        }

        Dummy d = new Dummy();
        String out = d.findBugs("file contents");
        assertEquals("[]", out);
        assertEquals("file contents", d.lastInput);
    }

    // Simple lightweight test: subclass IssueComparatorMicroservice
    @Test
    public void comparator_dummyReturnsExpected() {
        class Dummy extends IssueComparatorMicroservice {
            public String lastInput = null;
            @Override
            public String checkEquivalence(String bothListsOfIssues) {
                this.lastInput = bothListsOfIssues;
                return "{\"common\": []}";
            }
        }

        Dummy d = new Dummy();
        String out = d.checkEquivalence("left#####right");
        assertEquals("{\"common\": []}", out);
        assertEquals("left#####right", d.lastInput);
    }
}
