package utilities;

import java.util.HashMap;
import java.util.Map;

public class ScenarioContext {

	private static Map<String, Object> context = new HashMap<>();

	// Store data
	public static void setContext(String key, Object value) {
		context.put(key, value);
	}

	// Get stored data
	public static Object getContext(String key) {
		return context.get(key);
	}

	// Check if a key exists
	public static Boolean isContains(String key) {
		return context.containsKey(key);
	}

	// Clear context after each scenario (optional)
	public static void clearContext() {
		context.clear();
	}
}
