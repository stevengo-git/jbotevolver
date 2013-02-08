package simulation.util;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class Factory {

	public final static Map<Class<?>, Class<?>> map = new HashMap<Class<?>, Class<?>>();
	static {
		map.put(Boolean.class, boolean.class);
		map.put(Byte.class, byte.class);
		map.put(Short.class, short.class);
		map.put(Character.class, char.class);
		map.put(Integer.class, int.class);
		map.put(Long.class, long.class);
		map.put(Float.class, float.class);
		map.put(Double.class, double.class);
	}
	
	public static Object getInstance(String className, Object... objects) {
		
		try {
			Constructor<?>[] constructors = Class.forName(className).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				boolean found = true;
				Class<?>[] params = constructor.getParameterTypes();
				if(params.length == objects.length) {
					found = true;
					for(int i = 0 ; i < objects.length; i++) {
						Class c = objects[i].getClass();
						if(params[i].isPrimitive())
							c = map.get(objects[i].getClass());
						
						if(!params[i].isAssignableFrom(c)) {
							found = false;
							break;
						}
					}
					if(found) {
						return constructor.newInstance(objects);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		throw new RuntimeException("Unknown classname: " + className);
	}
}