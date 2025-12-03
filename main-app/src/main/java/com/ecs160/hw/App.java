package com.ecs160.hw;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.ecs160.hw.model.Issue;
import com.ecs160.hw.model.Repo;
import com.ecs160.hw.service.GitService;
import com.ecs160.persistence.RedisDB;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class App {

    // Shared HTTP client for all microservice calls
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public static void main(String[] args) {
        RedisDB db = new RedisDB();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            // read selected_repo.dat:
            List<String> lines = Files.readAllLines(Paths.get("../selected_repo.dat"));
            // line 1 is repo ID
            String repoID = lines.get(0);
            // Filenames on lines 2 to 4
            List<String> filenames = lines.subList(1, 4);

            // Print info about reading from selected_repo.dat
            System.out.println("Reading selected_repo.dat...");
            System.out.println("\tID found: "); 
            System.out.println("\t\t- " + repoID);
            System.out.println("\tFilenames found: ");
            for (String filename : filenames) {
                System.out.println("\t\t- " + filename);
            }
            // spacer newline
            System.out.println();

            // Create an EMPTY Repo instance with only the @Id field pre-filled
            Repo stubRepo = new Repo(repoID);
            Repo proxyRepo = (Repo) db.load(stubRepo);  // load from Redis (lazy issues via getIssues())

            // Clone the repo locally
            System.out.println("Cloning repo " + proxyRepo.getName() + "...");
            // Suppress cloning output
            GitService.cloneRepo(proxyRepo.getHtmlUrl(), "./cloned_repo/" + proxyRepo.getName());
            // newline spacer
            System.out.println();

            // Invoke issue summarizer on all issues for this repo
            JsonArray issueList1 = new JsonArray();
            for (Issue issue : proxyRepo.getIssues()) {   // getIssues() should trigger lazy load
                String summarizedIssueJson = issueSummarizer(issue);
                JsonObject issueJson = JsonParser.parseString(summarizedIssueJson).getAsJsonObject();
                issueList1.add(issueJson);
            }

            // Invoke bug finder on each selected filename
            JsonArray issueList2 = new JsonArray();
            for (String filename : filenames) {
                try {
                    String bugReportJson = bugFinder(filename);
                    JsonObject bugJson = JsonParser.parseString(bugReportJson).getAsJsonObject();
                    issueList2.add(bugJson);
                } catch (Exception e) {
                    System.out.println("Error invoking bugFinder on file " + filename + ": " + e.getMessage());
                }
            }

            // Invoke issue comparator on the two issue lists
            JsonObject commonIssues = issueComparator(issueList1, issueList2);
            System.out.println("\n-----COMMON ISSUES FOUND-----\n");
            System.out.println(gson.toJson(commonIssues));
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    // Helper: send a GET request with a single string query parameter (?q=...)
    // and return the response body as a string (JSON).
    private static String sendGetWithQuery(String baseUrl, String payload) throws Exception {
        String encoded = URLEncoder.encode(payload, StandardCharsets.UTF_8);

        String fullUrl = baseUrl + "?input=" + encoded;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .GET()
                .build();

        try {
            HttpResponse<String> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (Exception e) {
            System.err.println("Error sending GET request to " + baseUrl + ": " + e.getMessage());
            throw e;
        }
    }

    private static String issueSummarizer(Issue issue) throws Exception {
        String url = "http://localhost:8080/summarize_issue";

        // Build a single string representation of the issue for the microservice
        String issueContent = issue.getTitle() + "\n" + issue.getDescription();

        // Microservice reconstructs this string into JSON and returns a JSON string
        return sendGetWithQuery(url, issueContent);
    }

    private static String bugFinder(String filename) throws Exception {
        String url = "http://localhost:8080/find_bugs";

        // Load the file content as a string
        String fileContent = Files.readString(Paths.get(filename));

        // First line is the filename so bug_finder can identify the file
        String payload = filename + "\n" + fileContent;

        // Microservice returns a JSON description of found bugs as a string
        return sendGetWithQuery(url, payload);
    }

    private static JsonObject issueComparator(JsonArray issueList1, JsonArray issueList2) throws Exception {
        String url = "http://localhost:8080/check_equivalence";

        // Convert the two lists into one string, separated by a marker the microservice knows
        String combined = issueList1.toString() + "#####" + issueList2.toString();

        String jsonStr = sendGetWithQuery(url, combined);

        // Microservice returns JSON describing "common issues"
        return JsonParser.parseString(jsonStr).getAsJsonObject();
    }
}
