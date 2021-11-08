package edu.umn.midb.population.atlas.utils;

import java.util.Random;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class Utils {
	
	public static String convertJcpyt(String encrypted, String strkey) {
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword(strkey);

		String decrypted = encryptor.decrypt(encrypted);
		return decrypted;
	}
		
	public static String encryptJsypt(String to_encrypt, String strkey) {
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword(strkey);
		String encrypted= encryptor.encrypt(to_encrypt);
		return encrypted;
	}
	

}
