import annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DI {
    private static final Logger LOGGER = LoggerFactory.getLogger(DI.class);

    private static DI di = null;

    private Map<Class<?>,Object> instances;

    private DI(){
        instances = new ConcurrentHashMap<>();
    }

    public static DI getInstance(){
        if(di == null)
            di = new DI();
        return di;
    }

    public <T> T putInstance(Class<T> clazzKey,T instance) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        this.instances.put(clazzKey,instance);
        return get(clazzKey);
    }

    public <T> T get(Class<T> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        LOGGER.debug("get=" + clazz.getName());

        if(!instances.containsKey(clazz))
            instances.put(clazz,createInstance(clazz));

        return clazz.cast(instances.get(clazz));
    }

    private <T> T createInstance(Class<T> type) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<T> constructor = getConstructor(type);
        var params = getConstructorParams(constructor);

        T object = constructor.newInstance(params);
        return injectDependencies(object);
    }

    private <T> Constructor<T> getConstructor(Class<T> type){
        return (Constructor<T>) Arrays.stream(type.getConstructors())
                .min(Comparator.comparing(c -> c.getParameters().length))
                .get();
    }

    private <T> Object[] getConstructorParams(Constructor<T> constructor){
        return Arrays.stream(constructor.getParameters()).parallel().map(p -> {
            try {
                return get(p.getType());
            }catch (Exception e){
                LOGGER.error("Fail to create "+constructor.getName()+" instance",e);
                return null;
            }
        }).toArray();
    }

    private <T> T injectDependencies(T object) throws InvocationTargetException, InstantiationException {
        Field[] allFields = object.getClass().getDeclaredFields();

        for (Field field : allFields) {
            if(field.isAnnotationPresent(Inject.class)) {
                Class<?> clazz = field.getAnnotation(Inject.class).type();
                try {
                    boolean originallyAccessible = field.canAccess(object);
                    field.setAccessible(true);
                    field.set(object, get(clazz));
                    if (!originallyAccessible) {
                        field.setAccessible(false);
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return object;
    }
}

