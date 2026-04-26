package com.example.bookrecommendation;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class PasswordUtils {

    //Convert palin to SHA-256 hash password
    public static String hashPassword(String password){
        try{
            MessageDigest md= MessageDigest.getInstance("SHA-256");  //choose algorithm
            byte[] hashBytes = md.digest(password.getBytes());

            //convert byte arry into hex string
            StringBuilder sb =new StringBuilder();
            for (byte b: hashBytes){
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch(NoSuchAlgorithmException e){
            throw new RuntimeException("Error: SHA-256 Algorithm not found");
        }
    }
}
