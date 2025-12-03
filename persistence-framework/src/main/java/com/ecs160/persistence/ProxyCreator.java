package com.ecs160.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import com.ecs160.persistence.annotations.LazyLoad;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

public class ProxyCreator {
    private HashMap<String, Object> proxyCache = new HashMap<>();

    class LazyLoadHandler implements MethodHandler {
        private Object target;
        private Class<?> cls;
        private RedisDB db;

        LazyLoadHandler(Object target, RedisDB db) {
            this.target = target;
            this.cls = target.getClass();
            this.db = db;
        }

        @Override
        public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
            LazyLoad ann = thisMethod.getAnnotation(LazyLoad.class);

            if (ann != null) {
                System.out.println("Invoking method from lazyload proxy: " + thisMethod.getName());
                String fieldName = ann.field();
                ensureLazyFieldLoaded(fieldName);
            }

            return thisMethod.invoke(target, args);
        }

        private void ensureLazyFieldLoaded(String fieldName) throws Exception {
            try {
                // Grab field
                Field field = this.cls.getDeclaredField(fieldName);
                field.setAccessible(true);

                // Load the field value from Redis
                System.out.println("Lazy loading field: " + fieldName);

                // The targets should be issue objects with only their IDs populated (stubs)
                List<?> targets = (List<?>) field.get(target);
                if (targets == null || targets.isEmpty()) {
                    System.out.println("No targets to lazy load for field: " + fieldName);
                    return;
                }

                for (Object lazytarget : targets) {
                    db.load(lazytarget);
                }

                // After loading, the field should be populated
            } catch (Exception e) {
                System.err.println("Error accessing field " + fieldName + ": " + e.getMessage());
                throw e;
            }
        }
    }

    public Object createProxy(Object obj, RedisDB db) throws Exception {
        Class<?> clazz = obj.getClass();
        String className = clazz.getName();

        // avoid creating the proxy each time
        if (proxyCache.containsKey(className)) {
            return proxyCache.get(className);
        }

        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(clazz);
        Class<?> proxyClass = factory.createClass();
        Object proxyInstance = proxyClass.getDeclaredConstructor().newInstance();

        // Create the LazyLoader
        ((ProxyObject) proxyInstance).setHandler(new LazyLoadHandler(obj, db));

        proxyCache.put(className, proxyInstance);
        return proxyInstance;
    }
}
