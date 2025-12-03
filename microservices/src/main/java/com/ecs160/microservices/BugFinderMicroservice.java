package com.ecs160.microservices;

import com.ecs160.annotations.Endpoint;
import com.ecs160.annotations.Microservice;
import com.ecs160.interfaces.OllamaInterfacer;

@Microservice
public class BugFinderMicroservice {
    @Endpoint(url = "/find_bugs")
    public String findBugs(String code) {

        String prompt = "Find any bugs in the following C/C++ file and report them in the following JSON format: { \"bug_type\": \"<type>\", \"line\": <single_line_number>, \"description\": \"<description_of_bug>\", \"filename\": \"<filename>\" }. If there are multiple bugs, return a JSON array of such objects. If there are no bugs, return an empty JSON array. Here is the code:\n\n\n" + code;

        return OllamaInterfacer.invokeOllama(prompt);
    }
}
