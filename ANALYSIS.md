### Poor Ollama, it never stood a chance

Once again, Ollama never really stood a chance at detecting bugs and comparing issues. We saw no difference in the results of HW4 and HW2, which was expected.  

One interesting thing to note about this project vs HW2 is that SpringBoot gave us some complications when it came to large queries in the GET Requests. Like our 1000-line C files that we would send to bug_finder. It initially complained that the request URL was too large. We got around this by setting `server.max-http-request-header-size=1048576` in the application.properties file.  

Still, here are some of the more interesting "common issues" (or hallucinations) the model found, after several runs.  

```
-----COMMON ISSUES FOUND-----

{
  "filename": "obs-output.c",
  "bug_type": "segmentation fault",
  "line": 121,
  "description": "the function does not check for null input parameter"
}
```

```
-----COMMON ISSUES FOUND-----

{
  "filename": "...",
  "bug_type": "common Issue",
  "line": 3,
  "description": "..."
}

lol...
```
