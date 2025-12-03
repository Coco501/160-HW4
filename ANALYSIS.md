### Poor Ollama, it never stood a chance

TODO: This is the old ANALYSIS.md file from HW2. Update it with HW4 findings.

As expected, the simple Ollama model hallucinates more often than not. But, we were able to have it produce consistent JSON formatted results!

Unfortunately, the Ollama model really doesn't seem to be able to detect any real bugs, even when provided files with relatively simple bugs. I spent about 30 minutes going through the OBS repo's Pull Requests and hand picking these 3 specific files that had bugs I thought the model MIGHT be able to detect. Still no luck though. I imagine this project with a better model like today's ChatGPT could be very successful, but here we are. 

It is worth noting that it seems like the model would often just take a "bug" it found from the provided code files, and report it as a "common" issue regardless of whether the issue also existed in the list of issues produced by the issue summarizer.

That being said, here are some of the more interesting "common issues" (or hallucinations) the model found, after several runs.

```
-----COMMON ISSUES FOUND-----  
{  
    "filename": "obs-studio/plugins/obs-nvenc",  
    "bug_type": "C++",  
    "line": 3,  
    "description": "Failed to handle certain encoding options properly."  
}  
```


```
-----COMMON ISSUES FOUND-----  
{  
    "filename": "obs-studio/libobs/media_io/video-frame.c",  
    "bug_type": "invalid-function-name",  
    "line": 1,  
    "description": "The function 'video_frame_get_linesizes' doesn't appear to be a valid C function. The comment says that it's assumed to return an array of linesize values for each plane, but this code returns a single value which is the size of the buffer."  
}   
```

```
-----COMMON ISSUES FOUND-----  
{   
    "filename": "obs-deps",  
    "bug_type": "missing_required",  
    "line": 2,  
    "description": "The test failed because the dependency 'obs-deps' was not found in the required list. The package obs-deps is a requirement for the pull request to be processed."  
}     
```

```
-----COMMON ISSUES FOUND-----
{   
    "filename": "cloned_repo/obs-studio/plugins/obs-nvenc/nvenc-properties.c",  
    "bug_type": "invalid-line-offsets",   
    "line": 4,   
    "description": "The file has invalid line offsets."  
}   
```
