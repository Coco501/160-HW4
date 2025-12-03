package com.ecs160.persistence;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class AppTest 
{
    @Test
    public void redisDBMethodsSanityCheck() throws Exception {
        // RedisDB should expose persist(Object) and load(Object)
        Class<?> redis = Class.forName("com.ecs160.persistence.RedisDB");
        Method persistMethod = redis.getMethod("persist", Object.class);
        Method loadMethod = redis.getMethod("load", Object.class);

        assertNotNull(persistMethod);
        assertNotNull(loadMethod);

        // basic sanity about return types
        assertEquals(boolean.class, persistMethod.getReturnType());
        assertEquals(Object.class, loadMethod.getReturnType());
    }

    @Test
    public void idAnnotationExists() throws Exception {
        Class<?> id= Class.forName("com.ecs160.persistence.annotations.Id");
        assertNotNull(id);
    }

    @Test
    public void lazyLoadAnnotationExists() throws Exception {
        Class<?> lazyLoad = Class.forName("com.ecs160.persistence.annotations.LazyLoad");
        assertNotNull(lazyLoad);
    }

    @Test 
    public void persistableFieldAnnotationExists() throws Exception {
        Class<?> persistableField = Class.forName("com.ecs160.persistence.annotations.PersistableField");
        assertNotNull(persistableField);
    }

    @Test 
    public void persistableObjectAnnotationExists() throws Exception {
        Class<?> persistableObject = Class.forName("com.ecs160.persistence.annotations.PersistableObject");
        assertNotNull(persistableObject);
    }
}
