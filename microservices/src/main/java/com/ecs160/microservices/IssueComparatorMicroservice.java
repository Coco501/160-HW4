package com.ecs160.microservices;

import com.ecs160.annotations.Endpoint;
import com.ecs160.annotations.Microservice;
import com.ecs160.interfaces.OllamaInterfacer;

@Microservice
public class IssueComparatorMicroservice {
    @Endpoint(url = "/check_equivalence")
    public String checkEquivalence(String bothListsOfIssues) {
        // Take in two lists of issue arrays (in JSON format)
        // Compare the two lists
        // Find issues common in both lists
        // Return that list of common issues

        String prompt = "You are given two lists of issues (bug reports). The lists are separated by \"#####\". Please find any issues that are shared among both lists. They do not have to be exact matches, be generous. If you think an issue looks similar to another one, consider them a match. Return the list of common issues in a JSON format similar to the one provided in the lists. Here are the two lists:\n\n\n" + bothListsOfIssues;

        return OllamaInterfacer.invokeOllama(prompt);
    }
}
