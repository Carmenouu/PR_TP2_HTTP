package backend.forms;

import java.util.TreeMap;

public class SignInForm {
	
	private final static int ARGUMENTS_NUMBER = 2;
	
	private final static TreeMap<String, Class<?>> PARAMETERS_PROPERTIES = new TreeMap<>() {{
		put("firstName", String.class);
		put("lastName", String.class);
	}};
	
	private static TreeMap<String, Object> dataValidation(String... args) {
		
		TreeMap<String, Object> params = new TreeMap<>();
		
		if(args.length < ARGUMENTS_NUMBER * 2) { return null; }
		
		for(int i=0; i<ARGUMENTS_NUMBER * 2; i+=2) { params.put(args[i], PARAMETERS_PROPERTIES.get(args[i]).cast(args[i+1])); }
		
		return params;
		
	}
	
	private static void dataProcessing(TreeMap<String, Object> params) {
		
		System.out.println("Salut " + params.get("firstName") + " " + params.get("lastName"));
		
	}

	public static void main(String[] args) {
		
		TreeMap<String, Object> params;

		if((params = dataValidation(args)) != null) { dataProcessing(params); }

	}

}
