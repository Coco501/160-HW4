package com.ecs160.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ecs160.persistence.annotations.Id;
import com.ecs160.persistence.annotations.LazyLoad;
import com.ecs160.persistence.annotations.PersistableField;
import com.ecs160.persistence.annotations.PersistableObject;

import redis.clients.jedis.Jedis;

public class RedisDB {
    private final Jedis jedisSession;

    public RedisDB() {
        jedisSession = new Jedis("localhost", 6379);
    }

    // convert String from Redis to the requested type
    private Object convertType(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else {
            System.err.println("Could not convert value, unsupported type: " + targetType.getName());
            return null;
        }
    }

    private boolean isSimpleType(Class<?> type) {
        return type == String.class
                || type == int.class || type == Integer.class
                || type == boolean.class || type == Boolean.class;
    }

    // find the value of the @Id field on obj
    private Object extractId(Object obj) {
        if (obj == null) return null;

        for (Field f : obj.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(Id.class)) {
                f.setAccessible(true);
                try {
                    Object idValue = f.get(obj);
                    if (idValue != null) {
                        return idValue;
                    }
                } catch (IllegalAccessException ignored) {
                }
                break;
            }
        }
        return null;
    }

    private String buildKey(Class<?> clazz, Object idValue) {
        return clazz.getSimpleName() + ":" + idValue.toString();
    }

    // create an empty instance of clazz and set its @Id from the given id string
    private Object createStubForClass(Class<?> clazz, String idString) {
        try {
            Object stub = clazz.getDeclaredConstructor().newInstance();
            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(Id.class)) {
                    f.setAccessible(true);
                    Object convertedId = convertType(idString, f.getType());
                    f.set(stub, convertedId);
                    break;
                }
            }
            return stub;
        } catch (Exception e) {
            System.err.println("Error creating stub for " + clazz.getName() + ": " + e.getMessage());
            return null;
        }
    }

    /** persist with recursive child object support */
    public boolean persist(Object obj) {
        if (obj == null) {
            System.err.println("Object trying to be persisted is null");
            return false;
        } else if (!obj.getClass().isAnnotationPresent(PersistableObject.class)) {
            System.err.println("Object trying to be persisted is not annotated @PersistableObject");
            return false;
        }

        Object idValue = extractId(obj);
        if (idValue == null) {
            System.err.println("No @Id field found or its value was null");
            return false;
        }

        String objectKey = buildKey(obj.getClass(), idValue);
        Map<String, String> fieldMap = new HashMap<>();

        for (Field f : obj.getClass().getDeclaredFields()) {
            if (!f.isAnnotationPresent(PersistableField.class)) continue;

            f.setAccessible(true);
            Object value;
            try {
                value = f.get(obj);
            } catch (IllegalAccessException e) {
                System.err.println("Error reading field " + f.getName() + ": " + e.getMessage());
                continue;
            }

            if (value == null) {
                System.err.println("Field " + f.getName() + " is null, not persisting");
                continue;
            }

            Class<?> fieldType = f.getType();

            // simple value
            if (isSimpleType(fieldType)) {
                fieldMap.put(f.getName(), value.toString());
                continue;
            }

            // child object
            if (value.getClass().isAnnotationPresent(PersistableObject.class)) {
                boolean ok = persist(value);  // recursive persist
                if (!ok) {
                    System.err.println("Failed to persist child object in field " + f.getName());
                    continue;
                }
                Object childId = extractId(value);
                if (childId == null) {
                    System.err.println("Child object in field " + f.getName() + " has null @Id");
                    continue;
                }
                String childKey = buildKey(value.getClass(), childId);
                fieldMap.put(f.getName(), childKey);
                continue;
            }

            // list of elements, stored as comma separated values or keys
            if (List.class.isAssignableFrom(fieldType) && value instanceof List<?>) {
                List<?> list = (List<?>) value;
                StringBuilder sb = new StringBuilder();

                for (Object elem : list) {
                    if (elem == null) continue;

                    if (isSimpleType(elem.getClass())) {
                        sb.append(elem.toString());
                    } else if (elem.getClass().isAnnotationPresent(PersistableObject.class)) {
                        boolean ok = persist(elem);
                        if (!ok) {
                            System.err.println("Failed to persist list element in field " + f.getName());
                            continue;
                        }
                        Object childId = extractId(elem);
                        if (childId != null) {
                            sb.append(buildKey(elem.getClass(), childId));
                        } else {
                            System.err.println("List element in field " + f.getName() + " has null @Id");
                        }
                    } else {
                        System.err.println("Unsupported list element type in field "
                                + f.getName() + ": " + elem.getClass().getName());
                    }
                    sb.append(",");
                }

                if (sb.length() > 0) {
                    sb.setLength(sb.length() - 1); // drop trailing comma
                    fieldMap.put(f.getName(), sb.toString());
                }
                continue;
            }

            System.err.println("Unsupported field type for " + f.getName() + ": " + fieldType.getName());
        }

        jedisSession.hset(objectKey, fieldMap);
        return true;
    }

    /** load with recursive children and lazy fields via method-level @LazyLoad */
    public Object load(Object obj)  {
        if (obj == null) {
            return null;
        }

        Class<?> cls = obj.getClass();
        Object idValue = extractId(obj);
        if (idValue == null) {
            System.err.println("No @Id field found or its value was null");
            return null;
        }

        String key = buildKey(cls, idValue);
        Map<String, String> map = jedisSession.hgetAll(key);
        if (map == null || map.isEmpty()) {
            System.err.println("No data found in Redis for key: " + key);
            return null;
        }

        // Find lazy fields by scanning methods annotated with @LazyLoad(field = "...")
        Set<String> lazyFieldNames = new HashSet<>();
        for (Method m : cls.getDeclaredMethods()) {
            LazyLoad ann = m.getAnnotation(LazyLoad.class);
            if (ann != null) {
                lazyFieldNames.add(ann.field());
            }
        }
        boolean hasLazyFields = !lazyFieldNames.isEmpty();

        // eager load non lazy fields, including non lazy child objects
        for (Field f : cls.getDeclaredFields()) {
            if (!f.isAnnotationPresent(PersistableField.class)) {
                continue;
            }

            String fieldVal = map.get(f.getName());
            if (fieldVal == null) {
                continue;
            }

            f.setAccessible(true);
            Class<?> fieldType = f.getType();

            try {
                // simple type
                if (isSimpleType(fieldType)) {
                    Object convertedVal = convertType(fieldVal, fieldType);
                    f.set(obj, convertedVal);
                }
                // child object reference stored as "ClassName:id"
                else if (fieldType.isAnnotationPresent(PersistableObject.class)) {
                    String[] parts = fieldVal.split(":", 2);
                    if (parts.length == 2) {
                        String idStr = parts[1];
                        Object childStub = createStubForClass(fieldType, idStr);
                        if (childStub != null) {
                            Object childLoaded = load(childStub);  // recursive load, maybe proxy
                            f.set(obj, childLoaded);
                        }
                    }
                }
                // list, stored as comma separated values or keys
                else if (List.class.isAssignableFrom(fieldType)) {
                    String[] tokens = fieldVal.split(",");
                    List<Object> list = new ArrayList<>();

                    boolean isLazy = lazyFieldNames.contains(f.getName());

                    for (String token : tokens) {
                        token = token.trim();
                        if (token.isEmpty()) continue;

                        if (token.contains(":")) {
                            String[] p = token.split(":", 2);
                            String className = p[0];
                            String idStr = p[1];

                            // Resolve class and load object
                            Class<?> elemClass = Class.forName("com.ecs160.hw.model." + className);
                            Object stub = createStubForClass(elemClass, idStr);
                            if (isLazy) {
                                list.add(stub);  // add stub for lazy loading later
                            } else {
                                Object loaded = load(stub);
                                list.add(loaded);
                            }
                            continue;
                        }
                        list.add(token);
                    }
                    f.set(obj, list);
                }
            } catch (Exception e) {
                System.err.println("Error setting field " + f.getName() + ": " + e.getMessage());
            }
        }

        // if no lazy fields or no interfaces, return the fully loaded object
        if (!hasLazyFields) {
            return obj;
        }

        // TEMP
        ProxyCreator proxyCreator = new ProxyCreator();
        try {
            Object proxyObj = proxyCreator.createProxy(obj, this);
            return proxyObj;
        } catch (Exception e) {
            System.err.println("Error creating proxy for class " + cls.getName() + ": " + e.getMessage());
            return obj;
        }
    }
}
