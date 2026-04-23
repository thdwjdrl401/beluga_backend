package com.thdwjdrl.yejeong.beluga.user;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.thdwjdrl.yejeong.beluga.common.exception.InvalidRequestException;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {

	private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
	private static final int ITERATION_COUNT = 65_536;
	private static final int KEY_LENGTH = 256;
	private static final int SALT_LENGTH = 16;

	private final SecureRandom secureRandom = new SecureRandom();

	public String hash(String rawPassword) {
		byte[] salt = new byte[SALT_LENGTH];
		secureRandom.nextBytes(salt);
		byte[] hash = generateHash(rawPassword.toCharArray(), salt, ITERATION_COUNT);
		return ITERATION_COUNT
				+ ":" + Base64.getEncoder().encodeToString(salt)
				+ ":" + Base64.getEncoder().encodeToString(hash);
	}

	public boolean matches(String rawPassword, String storedHash) {
		String[] tokens = storedHash.split(":");
		if (tokens.length != 3) {
			throw new InvalidRequestException("저장된 비밀번호 형식이 올바르지 않습니다.");
		}

		int iterationCount = Integer.parseInt(tokens[0]);
		byte[] salt = Base64.getDecoder().decode(tokens[1]);
		byte[] expectedHash = Base64.getDecoder().decode(tokens[2]);
		byte[] actualHash = generateHash(rawPassword.toCharArray(), salt, iterationCount);
		return MessageDigest.isEqual(expectedHash, actualHash);
	}

	private byte[] generateHash(char[] rawPassword, byte[] salt, int iterationCount) {
		try {
			PBEKeySpec spec = new PBEKeySpec(rawPassword, salt, iterationCount, KEY_LENGTH);
			SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
			return factory.generateSecret(spec).getEncoded();
		}
		catch (GeneralSecurityException exception) {
			throw new IllegalStateException("비밀번호 해시 생성에 실패했습니다.", exception);
		}
	}

}
