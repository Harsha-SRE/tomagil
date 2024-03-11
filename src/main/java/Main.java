

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JSONTransformer {

	public static void main(String[] args) {
		try {
			File inputFile = new File("input.json");
			InputStream inputStream = new FileInputStream(inputFile);
			JSONObject jsonObject = new JSONObject(new JSONTokener(inputStream));
			inputStream.close();

			JSONObject transformedJSON = transformJSON(jsonObject);
			JSONArray outArray = new JSONArray();
			outArray.put(transformedJSON);
			System.out.println(outArray.toString(2));
		} catch (IOException | JSONException | ParseException e) {
			e.printStackTrace();
		}
	}

	public static JSONObject transformJSON(JSONObject input) throws JSONException, ParseException {
		
		JSONObject output = new JSONObject();
		for (String key : input.keySet()) {
			// Sanitize key
			String sanitizedKey = key.trim();
			if (sanitizedKey.isEmpty())
				continue; // Skip empty keys

			// Process value
			JSONObject value = input.getJSONObject(key);
			String k = value.keys().next();
			switch (k.trim()) {
			case "S":
				output.put(sanitizedKey, sanitizeString(value.getString(k)));
				break;
			case "N":
				output.put(sanitizedKey, sanitizeNumber(value.getString(k)));
				break;
			case "BOOL":
				output.put(sanitizedKey, sanitizeBoolean(value.getString(k)));
				break;
			case "NULL":
				if (value.getString(k).trim().matches("1|t|T|true|True|TRUE")) {
					output.put(sanitizedKey, JSONObject.NULL);
				}
				break;
			case "L":
				JSONArray transformedList = transformList(value);
				if (transformedList != null && transformedList.length() > 0) {
					output.put(sanitizedKey, transformedList);
				}
				break;
			case "M":
				JSONObject nestedMap = transformJSON(value.getJSONObject(k));
				if (nestedMap != null && nestedMap.length() > 0) {
					output.put(sanitizedKey, nestedMap);
				}
				break;
			default:
				// Skip unsupported types
				break;
			}
		}
		  if (output.has("string_2")) {
		        String string2Value = output.getString("string_2");
		        output.put("string_2", transformString2(string2Value));
		    }
		return output;
	}
	
	public static Object transformString2(String value) {
	    try {
	        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX").parse(value).getTime() / 1000;
	    } catch (ParseException e) {
	        return value; // Return original value if transformation fails
	    }
	}

	public static JSONArray transformList(JSONObject input) throws JSONException, ParseException {
		try {
			JSONArray list = input.getJSONArray("L");
			JSONArray transformedList = new JSONArray();
			for (int i = 0; i < list.length(); i++) {
				Object element = list.get(i);
				if (element instanceof JSONObject) {
					JSONObject value = (JSONObject) element;
					switch (value.keys().next()) {
					case "S":
						String str = sanitizeString(value.getString("S"));
						if (str != null)
							transformedList.put(str);
						break;
					case "N":
						Number num = sanitizeNumber(value.getString("N"));
						if (num != null)
							transformedList.put(num);
						break;
					case "BOOL":
						Boolean bool = sanitizeBoolean(value.getString("BOOL"));
						if (bool != null)
							transformedList.put(bool);
						break;
					default:
						break;
					}
				}
			}
			return transformedList;
		} catch (Exception e) {
			return null;
		}

	}

	public static String sanitizeString(String value) {
		if(value == null || value.trim().length() == 0) {
			return null;
			
		}
		return value.trim();
	}

	public static Number sanitizeNumber(String value) {
		// Remove leading zeros
		try {
			String sanitizedValue = value.replaceFirst("^0+(?!$)", "");
			// Parse number
			if (sanitizedValue.contains(".")) {
				return Double.parseDouble(sanitizedValue);
			} else {
				return Long.parseLong(sanitizedValue);
			}
		} catch (Exception e) {
			return null;
		}
	}

	public static Boolean sanitizeBoolean(String value) {
		String sanitizedValue = value.trim().toLowerCase();
		if (sanitizedValue.matches("1|t|true")) {
			return true;
		} else if (sanitizedValue.matches("0|f|false")) {
			return false;
		} else {
			return null; // Invalid boolean
		}
	}
}
