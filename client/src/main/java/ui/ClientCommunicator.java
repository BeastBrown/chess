package ui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;

public class ClientCommunicator {

    private String url;

    public ClientCommunicator(String urlString) {
        this.url = urlString;
    }

    private HttpURLConnection getHttpConn(String path) throws IOException {
        try {
            return (HttpURLConnection) new URI(this.url + path).toURL().openConnection();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String doRequest(String path, String method, HashMap<String, String> props, String body)
            throws IOException {
        HttpURLConnection c = configureConnection(path, method, props);
        if (!method.equals("GET")) {
            writeBody(body, c);
        }
        String resBody = new String(c.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (c.getResponseCode() != 200) {
            throw new IOException(resBody);
        }
        return resBody;
    }

    private static void writeBody(String body, HttpURLConnection c) throws IOException {
        try(PrintWriter toWrite =  new PrintWriter(c.getOutputStream())) {
            toWrite.print(body);
        }
    }

    private HttpURLConnection configureConnection(String path, String method, HashMap<String, String> props) throws IOException {
        HttpURLConnection c = getHttpConn(path);
        c.setConnectTimeout(5000);
        c.setReadTimeout(5000);
        c.setRequestMethod(method);
        c.setDoOutput(true);
        c.setDoInput(true);
        for (String k : props.keySet()) {
            c.setRequestProperty(k, props.get(k));
        }
        return c;
    }
}
