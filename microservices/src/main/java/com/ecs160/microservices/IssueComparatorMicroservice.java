package com.ecs160.microservices;

import com.ecs160.interfaces.OllamaInterfacer;
import org.springframework.web.bind.annotation.GetMapping;

public class IssueComparatorMicroservice {
    @GetMapping(value = "/check_equivalence")
    public String checkEquivalence(String bothListsOfIssues) {
        String prompt = "You are given two lists of issues (bug reports). The lists are separated by \"#####\". Please find any issues that are shared among both lists. They do not have to be exact matches, be generous. If you think an issue looks similar to another one, consider them a match. Return the list of common issues in a JSON format similar to the one provided in the lists. Here are the two lists:\n\n\n" + bothListsOfIssues;

        return OllamaInterfacer.invokeOllama(prompt);
    }
}
