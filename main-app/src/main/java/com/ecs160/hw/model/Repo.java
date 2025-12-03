package com.ecs160.hw.model;

import java.util.List;
import java.util.Map;

import com.ecs160.persistence.annotations.Id;
import com.ecs160.persistence.annotations.LazyLoad;
import com.ecs160.persistence.annotations.PersistableField;
import com.ecs160.persistence.annotations.PersistableObject;

@PersistableObject
public class Repo {
    @Id
    private String id;

    @PersistableField
    private String htmlUrl;

    @PersistableField
    private String createdAt;

    @PersistableField
    private String ownerLogin;

    @PersistableField
    private List<String> issueIDs;

    @PersistableField
    private List<Issue> issues;

    @PersistableField
    private String name;

    private String fullName;
    private int openIssuesCount;

    public Repo() {}

    public Repo(String id) {
        this.id = id;
    }

    public Repo(Map<String, String> data) {
        this.name = (String) data.get("name");
        this.ownerLogin = (String) data.get("ownerLogin");
        this.fullName = (String) data.get("fullName");
        this.htmlUrl = (String) data.get("htmlUrl");
        this.openIssuesCount = (int) Integer.parseInt(data.get("openIssuesCount"));
        this.createdAt = (String) data.get("createdAt");
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new java.util.HashMap<>();
        map.put("id", this.id);
        map.put("name", this.name);
        map.put("fullName", this.fullName);
        map.put("ownerLogin", this.ownerLogin);
        map.put("htmlUrl", this.htmlUrl);
        map.put("openIssuesCount", Integer.toString(this.openIssuesCount));
        if (this.issueIDs != null) {
            String issueIDstring = String.join(",", this.issueIDs);
            map.put("issueIDs", issueIDstring);
        } else {
            map.put("issueIDs", "");
        }
        map.put("createdAt", this.createdAt);
        return map;
    }

    public void setId(String id) {
        if (this.id != null) {
            System.err.println("Repo ID is already set, cannot overwrite existing ID");
        } else {
            this.id = id;
        }
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getName() {
        return name;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @LazyLoad(field = "issues")
    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
        String[] issueIdString = new String[issues.size()];
        for (int i = 0; i < issues.size(); i++) {
            issueIdString[i] = issues.get(i).getId();
        }
        this.issueIDs = java.util.Arrays.asList(issueIdString);
    }

    public List<String> getIssueIDs() {
        return issueIDs;
    }
}
