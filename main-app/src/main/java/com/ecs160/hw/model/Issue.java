package com.ecs160.hw.model;

import java.util.HashMap;
import java.util.Map;

import com.ecs160.persistence.annotations.Id;
import com.ecs160.persistence.annotations.PersistableField;
import com.ecs160.persistence.annotations.PersistableObject;

@PersistableObject
public class Issue {
    @Id
    private String id;

    @PersistableField
    private String title;

    @PersistableField
    private String date;

    @PersistableField
    private String description;
    
    public Issue() {}

    public Issue(Map<String, String> data) {
        this.id = data.get("id");
        this.description = data.get("description");
        this.date = data.get("date");
        this.title = data.get("title");
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public HashMap<String, String> toMap() {
        HashMap<String, String> issueMap = new HashMap<>();
        issueMap.put("id", this.id);
        issueMap.put("description", this.description);
        issueMap.put("date", this.date);
        issueMap.put("title", this.title);
        return issueMap;
    }
}