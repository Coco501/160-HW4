package com.ecs160.microservices;

import com.ecs160.annotations.Endpoint;
import com.ecs160.annotations.Microservice;
import com.ecs160.interfaces.OllamaInterfacer;

@Microservice
public class IssueSummarizerMicroservice {
    @Endpoint(url = "/summarize_issue")
    public String summarizeIssue(String issueJson) {

        String prompt = "Summarize this issue description and title into the following format: { \"bug_type\": \"<type>\", \"line\": <single_line_number>, \"description\": \"<description_of_bug>\", \"filename\": \"<filename>\" }. Here is the issue:\n\n\n" + issueJson;
        
        return OllamaInterfacer.invokeOllama(prompt);
    }
}
