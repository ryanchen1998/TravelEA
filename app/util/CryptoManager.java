package util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Random;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


/**
 * Hashes and salts passwords and handles password verification as well as securely generating
 * authentication tokens.
 */
public class CryptoManager {

    private CryptoManager() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Check if a password attempt matches the existing salted hash.
     *
     * @param passwordToCheck New password being attempted
     * @param salt Salt data associated with user account (In string format)
     * @param existingHashedPassword Existing password that has already been salted and hashed
     * @return True if passwords match, false otherwise
     */
    public static boolean checkPasswordMatch(String passwordToCheck, String salt,
        String existingHashedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return checkPasswordMatch(passwordToCheck, Base64.getDecoder().decode(salt),
            existingHashedPassword);
    }

    /**
     * Check if a password attempt matches the existing salted hash.
     *
     * @param passwordToCheck New password being attempted
     * @param salt Salt data associated with user account
     * @param existingHashedPassword Existing password that has already been salted and hashed
     * @return True if passwords match, false otherwise
     */
    private static boolean checkPasswordMatch(String passwordToCheck, byte[] salt,
        String existingHashedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // First hash the given password with salt
        String newHashedPassword = hashPassword(passwordToCheck, salt);

        // Now return if this matches the existing hashed password
        return existingHashedPassword.equals(newHashedPassword);
    }

    /**
     * Hash a password with a given salt.
     *
     * @param password Password to hash
     * @param salt Salt for password
     * @return Salted and hashed password
     */
    public static String hashPassword(String password, byte[] salt)
        throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Define iterations and key length
        Integer iterations = 10000;
        Integer keyLength = 256;

        // Now salt and hash the password
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
        SecretKey key = skf.generateSecret(spec);
        byte[] res = key.getEncoded();
        return Base64.getEncoder().encodeToString(res);
    }

    /**
     * Generates a new random base64 string to use as a password salt.
     */
    public static String generateNewSalt() {
        return Base64.getEncoder().encodeToString(generateNewSaltBytes());
    }

    /**
     * Generates a 32 byte salt to use when salting a new password MAKE SURE TO SAVE THE SALT.
     *
     * @return 32 byte salt
     */
    private static byte[] generateNewSaltBytes() {
        // Generate salt (32 bytes at minimum)
        byte[] salt = new byte[32];
        Random rand = new SecureRandom();
        rand.nextBytes(salt);

        // Return salt
        return salt;
    }

    /**
     * Generate JSON web token.
     *
     * @return JWT
     */
    public static String createToken(Long userId, String secret) {
        Algorithm algorithm;
        algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
            .withIssuer("TravelEA")
            .withClaim("userId", userId)
            .sign(algorithm);
    }

    /**
     * Verifies the token has been generated legitimately and gets the user id.
     *
     * @param token The token to verify
     * @param secret The password from config
     * @return The user's id
     */
    public static Long verifyToken(String token, String secret) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("TravelEA")
                .build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("userId").asLong();
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}
