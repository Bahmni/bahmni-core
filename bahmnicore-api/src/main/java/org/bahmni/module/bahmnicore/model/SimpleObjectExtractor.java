package org.bahmni.module.bahmnicore.model;
import java.util.LinkedHashMap;

public class SimpleObjectExtractor {

    private LinkedHashMap post;

    public SimpleObjectExtractor(LinkedHashMap post) {
        this.post = post;
    }

    public <T> T extract(String key) {
        return post == null || key == null ? null : (T) post.get(key);
    }

    public <T> T getValueOrDefault(String key, Class clazz) {
        if(post == null || key == null)
            return (T)defaultValue(clazz);
        Object value = post.get(key);
        return value == null ? (T)defaultValue(clazz) : (T)value;
    }

    //TODO: Remove this when openmrs start using jackson-mapper-asl > 1.7.1
    //http://grepcode.com/file/repo1.maven.org/maven2/org.codehaus.jackson/jackson-mapper-asl/1.8.5/org/codehaus/jackson/map/util/ClassUtil.java#ClassUtil.defaultValue%28java.lang.Class%29
    public static Object defaultValue(Class<?> cls)
    {
        if (cls == Integer.TYPE) {
            return Integer.valueOf(0);
        }
        if (cls == Long.TYPE) {
            return Long.valueOf(0L);
        }
        if (cls == Boolean.TYPE) {
            return Boolean.FALSE;
        }
        if (cls == Double.TYPE) {
            return Double.valueOf(0.0);
        }
        if (cls == Float.TYPE) {
            return Float.valueOf(0.0f);
        }
        if (cls == Byte.TYPE) {
            return Byte.valueOf((byte) 0);
        }
        if (cls == Short.TYPE) {
            return Short.valueOf((short) 0);
        }
        if (cls == Character.TYPE) {
            return '\0';
        }
        throw new IllegalArgumentException("Class "+cls.getName()+" is not a primitive type");
    }

}

