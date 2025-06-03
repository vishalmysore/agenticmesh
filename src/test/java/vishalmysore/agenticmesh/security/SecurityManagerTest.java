package vishalmysore.agenticmesh.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import static org.junit.jupiter.api.Assertions.*;

public class SecurityManagerTest {
    
    private SecurityManager securityManager;
    private static final String TEST_AGENT = "test-agent";
    private static final String TEST_PERMISSION = "read";

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        securityManager = SecurityManager.getInstance();
    }

    @Test
    void testAgentRegistration() throws Exception {
        // Generate a test key pair
        java.security.KeyPairGenerator keyGen = java.security.KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        java.security.KeyPair keyPair = keyGen.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();

        // Register agent
        securityManager.registerAgent(TEST_AGENT, publicKey);
        assertTrue(securityManager.getRegisteredAgents().contains(TEST_AGENT));
    }

    @Test
    void testPermissionManagement() {
        securityManager.grantPermission(TEST_AGENT, TEST_PERMISSION);
        assertTrue(securityManager.hasPermission(TEST_AGENT, TEST_PERMISSION));

        securityManager.revokePermission(TEST_AGENT, TEST_PERMISSION);
        assertFalse(securityManager.hasPermission(TEST_AGENT, TEST_PERMISSION));
    }

    @Test
    void testMessageEncryption() throws Exception {
        byte[] testMessage = "Test message".getBytes();
        byte[] encrypted = securityManager.encryptMessage("sender", "receiver", testMessage);
        assertNotNull(encrypted);
        assertNotEquals(new String(testMessage), new String(encrypted));

        byte[] decrypted = securityManager.decryptMessage("receiver", encrypted);
        assertArrayEquals(testMessage, decrypted);
    }

    @Test
    void testDigitalSignature() throws Exception {
        byte[] testData = "Test data for signing".getBytes();
        byte[] signature = securityManager.sign(testData);
        
        assertTrue(securityManager.verify(testData, signature));
        
        // Modify data to verify signature fails
        byte[] modifiedData = "Modified test data".getBytes();
        assertFalse(securityManager.verify(modifiedData, signature));
    }
}
