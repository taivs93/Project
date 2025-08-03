package com.taivs.project.security.encryption;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class TokenEncryptor {

    @Value("${jwt.encrypt-secret}")
    private String secretKey;

    private SecretKeySpec keySpec;

    @PostConstruct
    public void init(){
        System.out.println("Token Encryptor generate");
        keySpec = new SecretKeySpec(secretKey.getBytes(),"AES");
    }

    public String encrypt(String token){
        try{
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(token.getBytes());
            System.out.println("Hello");
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e){
            throw new RuntimeException("Error decrypting token",e);
        }
    }

    public String decrypt(String encryptedToken){
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedToken);
            byte[] decrypted = cipher.doFinal(decodedBytes);
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Cannot decrypt token", e);
        }
    }
}
