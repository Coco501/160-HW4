package com.ecs160;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import com.ecs160.annotations.Endpoint;
import com.ecs160.annotations.Microservice;
import com.ecs160.microservices.BugFinderMicroservice;
import com.ecs160.microservices.IssueComparatorMicroservice;
import com.ecs160.microservices.IssueSummarizerMicroservice;

public class AppTest 
{
    @Microservice
    public static class DummyService {
        @com.ecs160.annotations.Endpoint(url = "/dummy")
        public String doSomething(String input) {
            return "ok";
        }
    }

    @Test
    public void dummyService_shouldHaveAnnotations() throws Exception {
        // verify class annotation
        assertTrue(DummyService.class.isAnnotationPresent(Microservice.class));

        // verify method and endpoint url
        Method m = DummyService.class.getDeclaredMethod("doSomething", String.class);
        assertTrue(m.isAnnotationPresent(Endpoint.class));
        assertEquals("/dummy", m.getAnnotation(Endpoint.class).url());
    }

    /*
     * Verify that the microservices in this module are annotated properly
     * and that their endpoint methods expose the expected URLs.
     */

    @Test
    public void sanityCheck_IssueComparator() throws Exception {
        // IssueComparatorMicroservice
        Class<?> comparator = IssueComparatorMicroservice.class;
        assertNotNull(comparator);
        assertTrue(comparator.isAnnotationPresent(Microservice.class));
        Method checkEquiv = comparator.getMethod("checkEquivalence", String.class);
        assertTrue(checkEquiv.isAnnotationPresent(Endpoint.class));
        assertEquals("/check_equivalence", checkEquiv.getAnnotation(Endpoint.class).url());
    }

    @Test
    public void sanityCheck_BugFinder() throws Exception {
        // BugFinderMicroservice
        Class<?> bugFinder = BugFinderMicroservice.class;
        assertNotNull(bugFinder);
        assertTrue(bugFinder.isAnnotationPresent(Microservice.class));
        Method findBugs = bugFinder.getMethod("findBugs", String.class);
        assertTrue(findBugs.isAnnotationPresent(Endpoint.class));
        assertEquals("/find_bugs", findBugs.getAnnotation(Endpoint.class).url());

    }

    @Test 
    public void sanityCheck_IssueSummarizer() throws Exception {
        // IssueSummarizerMicroservice
        Class<?> issueSummarizer = IssueSummarizerMicroservice.class;
        assertNotNull(issueSummarizer);
        assertTrue(issueSummarizer.isAnnotationPresent(Microservice.class));
        Method summarizeIssue = issueSummarizer.getMethod("summarizeIssue", String.class);
        assertTrue(summarizeIssue.isAnnotationPresent(Endpoint.class));
        assertEquals("/summarize_issue", summarizeIssue.getAnnotation(Endpoint.class).url());
    }
}
