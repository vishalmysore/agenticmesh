package vishalmysore.agenticmesh.security;

import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Security manager for handling encryption, authentication, and access control
 */
public class SecurityManager {
    private static SecurityManager instance;
    private final Map<String, PublicKey> agentKeys;
    private final KeyPair meshKeyPair;
    private final Map<String, Set<String>> permissions;
    private final SecretKey aesKey;

    private SecurityManager() throws NoSuchAlgorithmException {
        this.agentKeys = new ConcurrentHashMap<>();
        this.permissions = new ConcurrentHashMap<>();
        this.meshKeyPair = generateKeyPair();
        this.aesKey = generateAESKey();
    }

    public static synchronized SecurityManager getInstance() throws NoSuchAlgorithmException {
        if (instance == null) {
            instance = new SecurityManager();
        }
        return instance;
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    private SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();
    }

    public void registerAgent(String agentId, PublicKey publicKey) {
        agentKeys.put(agentId, publicKey);
        permissions.putIfAbsent(agentId, ConcurrentHashMap.newKeySet());
    }

    public void grantPermission(String agentId, String permission) {
        permissions.computeIfAbsent(agentId, k -> ConcurrentHashMap.newKeySet())
                  .add(permission);
    }

    public void revokePermission(String agentId, String permission) {
        Set<String> agentPermissions = permissions.get(agentId);
        if (agentPermissions != null) {
            agentPermissions.remove(permission);
        }
    }

    public boolean hasPermission(String agentId, String permission) {
        Set<String> agentPermissions = permissions.get(agentId);
        return agentPermissions != null && agentPermissions.contains(permission);
    }

    public byte[] encryptMessage(String senderId, String receiverId, byte[] message) 
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
                   IllegalBlockSizeException, BadPaddingException {
        // Use AES for message encryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] iv = cipher.getIV();
        byte[] encryptedMessage = cipher.doFinal(message);
        
        // Combine IV and encrypted message
        byte[] combined = new byte[iv.length + encryptedMessage.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedMessage, 0, combined, iv.length, encryptedMessage.length);
        
        return combined;
    }

    public byte[] decryptMessage(String receiverId, byte[] encryptedMessage) 
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
                   IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        // Extract IV and encrypted content
        byte[] iv = Arrays.copyOfRange(encryptedMessage, 0, 16);
        byte[] content = Arrays.copyOfRange(encryptedMessage, 16, encryptedMessage.length);
        
        // Decrypt using AES
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));
        return cipher.doFinal(content);
    }

    public byte[] sign(byte[] data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(meshKeyPair.getPrivate());
        signature.update(data);
        return signature.sign();
    }

    public boolean verify(byte[] data, byte[] signatureBytes) 
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(meshKeyPair.getPublic());
        signature.update(data);
        return signature.verify(signatureBytes);
    }

    public void removeAgent(String agentId) {
        agentKeys.remove(agentId);
        permissions.remove(agentId);
    }

    public Set<String> getRegisteredAgents() {
        return new HashSet<>(agentKeys.keySet());
    }

    public Map<String, Set<String>> getAgentPermissions() {
        return new HashMap<>(permissions);
    }
}
