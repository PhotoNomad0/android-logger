package org.unfoldingword.tools.logger;

import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Base class for reporting to a server.
 * Reports can include logs, crash reports, and additional information
 */
public abstract class Reporter {

    private final String url;
    private String authToken = null;
    private String username = null;
    private String password = null;

    private int responseCode = -1;

     private String responseString = null;
    private String errorString = null;

    /**
     * Creates a new reporter that posts data to the given url
     * @param url the url that can receive the report
     */
    public Reporter(String url) {
        this.url = url;
    }


    /**
     * Sets the token used for authenticating the report
     * Tokens take precedence over credentials
     * Token authentication.
     * @param token
     */
    public void setAuthorization(String token) {
        this.authToken = token;
    }

    /**
     * Sets the credentials used for authenticating the report
     * Basic authentication.
     * @param username
     * @param password
     */
    public void setAuthorization(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseString() {
        return responseString;
    }

    public String getErrorString() {
        return errorString;
    }

    /**
     * Generates and returns the auth information if available
     * @return
     */
    private String getAuth() {
        if(this.authToken != null) {
            return "token " + this.authToken;
        } else if(this.username != null && this.password != null){
            String credentials = this.username + ":" + this.password;
            try {
                return "Basic " + Base64.encodeToString(credentials.getBytes("UTF-8"), Base64.NO_WRAP);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Submits the data
     * @param data
     * @return
     */
    public String submit(String data) {
        try {
            URL url = new URL(this.url);
            HttpURLConnection conn;
            if(url.getProtocol().equals("https")) {
                conn = (HttpsURLConnection)url.openConnection();
            } else {
                conn = (HttpURLConnection)url.openConnection();
            }
            String auth = getAuth();
            if(auth != null) {
                conn.setRequestProperty("Authorization", auth);
            }
            conn.setRequestProperty("Content-Type", "application/json");

            // post
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(data);
            dos.flush();
            dos.close();

            // response
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int current;
            while ((current = bis.read()) != -1) {
                baos.write((byte) current);
            }
            responseCode = conn.getResponseCode();
            responseString =  baos.toString("UTF-8");
            return responseString;

        } catch (IOException e) {
            e.printStackTrace();
            errorString = e.toString();
        }

        responseString = null;
        return responseString;
    }

    // TODO: 10/5/16 add method to determine if there was an error
}