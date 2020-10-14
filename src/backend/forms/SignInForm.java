package backend.forms;

import java.util.TreeMap;

/**
 * @author Nel Bouvier et Carmen Prévot
 * @version 1.0
 */
public class SignInForm {
	
	private final static int ARGUMENTS_NUMBER = 2;
	
	private final static TreeMap<String, Class<?>> PARAMETERS_PROPERTIES = new TreeMap<>() {{
		put("signInFirstName", String.class);
		put("signInLastName", String.class);
	}};
	
	
	/**
	 * Checks the data, in order to have only two arguments (as defined in the fields).
	 * @param args The data, contained in the request.
	 * @return The parameters.
	 */
	private static TreeMap<String, Object> dataValidation(String... args) {
		
		TreeMap<String, Object> params = new TreeMap<>();
		
		if(args.length < ARGUMENTS_NUMBER * 2) { return null; }
		
		for(int i=0; i<ARGUMENTS_NUMBER * 2; i+=2) { params.put(args[i], PARAMETERS_PROPERTIES.get(args[i]).cast(args[i+1])); }
		
		return params;
		
	}
	
	
	/**
	 * Processes the data, prints a short string using the parameters.
	 * @param params The parameters, given by the request (see dataValidation).
	 */
	private static void dataProcessing(TreeMap<String, Object> params) {
		
		System.out.println("Salut " + params.get("signInFirstName") + " " + params.get("signInLastName"));
		
	}

	/**
	 * The main method of this SignInForm.
	 * @param args
	 */
	public static void main(String[] args) {
		
		TreeMap<String, Object> params;

		if((params = dataValidation(args)) != null) { dataProcessing(params); }

	}

}
