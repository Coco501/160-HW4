package com.ecs160.interfaces;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.github.ollama4j.Ollama;
import io.github.ollama4j.models.generate.OllamaGenerateRequest;
import io.github.ollama4j.models.request.ThinkMode;
import io.github.ollama4j.models.response.OllamaResult;

public class OllamaInterfacer {

    private static final Ollama ollama = new Ollama("http://localhost:11434/");

    static {
        // Set ollama timeout to 600 seconds, so it has time to boot up on first request, just to be safe
        ollama.setRequestTimeoutSeconds(600);
    }

    public static String invokeOllama(String prompt) {
        if (ollama == null) {
            return "Ollama API not initialized";
        }

        try {
            ollama.pullModel("deepcoder:1.5b");
        } catch (Exception e) {
            e.printStackTrace();
            return "Error pulling Ollama model: " + e.getMessage();
        }

        System.out.println("INVOKING OLLAMA\n");

        try {
            // Generate accepts the model, prompt, and then a hashmap of options (we will leave the options blank for now) - it returns a custom OllamaResult object that contains the response 

            // Structure pulled from ollama4j docs: https://ollama4j.github.io/ollama4j/apis-generate/generate/#with-response-as-a-map
            Map<String, Object> bug_type = new HashMap<>();
            bug_type.put("type", String.class.getSimpleName().toLowerCase());

            Map<String, Object> line = new HashMap<>();
            line.put("type", Integer.class.getSimpleName().toLowerCase());

            Map<String, Object> description = new HashMap<>();
            description.put("type", String.class.getSimpleName().toLowerCase());

            Map<String, Object> filename = new HashMap<>();
            filename.put("type", String.class.getSimpleName().toLowerCase());

            Map<String, Object> properties = new HashMap<>();
            properties.put("bug_type", bug_type);
            properties.put("line", line);
            properties.put("description", description);
            properties.put("filename", filename);

            Map<String, Object> format = new HashMap<>();
            format.put("type", "object");
            format.put("properties", properties);
            format.put("required", Arrays.asList("bug_type", "line", "description", "filename"));

            // OllamaResult result = ollama.generate("deepcoder:1.5b", prompt, new HashMap<>());
            OllamaResult result = ollama.generate(
                OllamaGenerateRequest.builder()
                    .withModel("deepcoder:1.5b")
                    .withPrompt(prompt)
                    .withFormat(format)
                    .withThink(ThinkMode.DISABLED)
                    .build(),
                    null
            );
            return result.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error when invoking Ollama: " + e.getMessage();
        }
    }
}
