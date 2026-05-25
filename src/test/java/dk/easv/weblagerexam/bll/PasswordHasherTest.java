package dk.easv.weblagerexam.bll;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void generateSaltReturnsSixteenRandomBytesEncodedAsBase64() throws Exception {
        String salt = PasswordHasher.generateSalt();

        byte[] decodedSalt = Base64.getDecoder().decode(salt);

        assertEquals(16, decodedSalt.length);
    }

    @Test
    void hashPasswordIsDeterministicForSamePasswordAndSalt() throws Exception {
        String salt = PasswordHasher.generateSalt();

        String firstHash = PasswordHasher.hashPassword("secret", salt);
        String secondHash = PasswordHasher.hashPassword("secret", salt);

        assertEquals(firstHash, secondHash);
    }

    @Test
    void verifyPasswordAcceptsMatchingPasswordAndRejectsDifferentPassword() throws Exception {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword("correct-password", salt);

        assertTrue(PasswordHasher.verifyPassword("correct-password", hash, salt));
        assertFalse(PasswordHasher.verifyPassword("wrong-password", hash, salt));
    }

    @Test
    void differentSaltsProduceDifferentHashesForSamePassword() throws Exception {
        String firstSalt = PasswordHasher.generateSalt();
        String secondSalt = PasswordHasher.generateSalt();

        String firstHash = PasswordHasher.hashPassword("same-password", firstSalt);
        String secondHash = PasswordHasher.hashPassword("same-password", secondSalt);

        assertNotEquals(firstHash, secondHash);
    }
}
