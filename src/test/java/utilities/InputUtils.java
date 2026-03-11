package utilities;

import java.util.Random;

public class InputUtils {
	private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final Random RANDOM = new Random();

	/**
	 * Generates a valid 10-digit mobile number. Ensures the number matches the
	 * regex: starts with 7-9 and has 10 digits.
	 */
	public static String generateMobileNumber() {
		String regex = "^[789][0-9]{9}$";
		Random random = new Random();
		String mobileNumber;

		// Keep generating until it matches regex
		do {
			int firstDigit = 7 + random.nextInt(3); // 7, 8, or 9
			long remainingDigits = (long) (Math.pow(10, 9) + random.nextInt(1_000_000_000));
			mobileNumber = firstDigit + String.valueOf(remainingDigits).substring(1, 10);
		} while (!mobileNumber.matches(regex));

		return mobileNumber;
	}

	public static String generateRandomString(int length) {
		/*
		 *************************************************************************************
		 * FunctionName: generateRandomString Input Parameters: lenght Output
		 * Parameters: random aplhabets string Description: This method will
		 * generate random alpha-numeric string Date created/modified: 09/05/2024
		 * Modified by: Rahul Abhay Kamat
		 *************************************************************************************/
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder result = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            result.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }

        return "Auto"+result.toString();
	}
	/**
     * Generates a valid random email address.
     * Format: prefix + random + domain
     * Example: user1234@testmail.com
     */
	public static String generateEmail() {
        StringBuilder sb = new StringBuilder();

        // Prefix with random 6 letters
        for (int i = 0; i < 6; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }

        // Add random number
        int randomNum = 1000 + RANDOM.nextInt(9000);

        // Combine prefix, number and domain
        String email = sb.toString() + randomNum + "@testmail.com";
        // Final validation against regex before returning
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!email.matches(regex)) {
            throw new IllegalStateException("Generated email is invalid: " + email);
        }
        return email;
    }

	/**
	 * Validates if a string is a valid password. Example: Password must be at least
	 * 8 characters long, with at least one digit and one special character.
	 */
	public static boolean isValidPassword(String password) {
		// Regex for a simple password validation
		String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
		return password != null && password.matches(regex);
	}
}
