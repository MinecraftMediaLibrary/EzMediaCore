package com.github.pulsebeat02.http;

import com.github.pulsebeat02.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestHandler implements Runnable, AbstractRequestHandler {

    private final HttpDaemon daemon;
    private final HttpDaemon.ZipHeader header;
    private final Socket client;

    public RequestHandler(@NotNull final HttpDaemon daemon, HttpDaemon.ZipHeader header, @NotNull final Socket client) {
        this.daemon = daemon;
        this.header = header;
        this.client = client;
    }

    @Override
    public void run() {
        handleRequest();
    }

    @Override
    public void handleRequest() {
        daemon.onClientConnect(client);
        boolean flag = false;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), "8859_1"));
            OutputStream out = client.getOutputStream();
            PrintWriter pout = new PrintWriter(new OutputStreamWriter(out, "8859_1"), true);
            String request = in.readLine();
            verbose("Received request '" + request + "' from " + client.getInetAddress().toString());
            Matcher get = requestPattern(request);
            if (get.matches()) {
                request = get.group(1);
                File result = requestFileCallback(request);
                if (result == null) {
                    flag = true;
                    pout.println("HTTP/1.0 400 Bad Request");
                } else {
                    verbose("Request '" + request + "' is being served to " + client.getInetAddress());
                    try {
                        out.write(buildHeader(result).getBytes(StandardCharsets.UTF_8));
                        FileInputStream fis = new FileInputStream(result);
                        byte[] data = new byte[64 * 1024];
                        for (int read; (read = fis.read(data)) > -1; ) {
                            out.write(data, 0, read);
                        }
                        out.flush();
                        fis.close();
                        verbose("Successfully served '" + request + "' to " + client.getInetAddress());
                    } catch (FileNotFoundException e) {
                        flag = true;
                        pout.println("HTTP/1.0 404 Object Not Found");
                    }
                }
            } else {
                flag = true;
                pout.println("HTTP/1.0 400 Bad Request");
            }
            client.close();
        } catch (IOException e) {
            flag = true;
            verbose("I/O error " + e);
        }
        if (flag) {
            daemon.onResourcepackFailedDownload(client);
        }
    }

    private Matcher requestPattern(final String req) {
        return Pattern.compile("GET /?(\\S*).*").matcher(req);
    }

    private void verbose(final String info) {
        if (daemon.isVerbose()) {
            Logger.info(info);
        }
    }

    public File requestFileCallback(final String request) {
        return new File(daemon.getParentDirectory(), request);
    }

    @Override
    public String buildHeader(final File f) {
        return "HTTP/1.0 200 OK\r\n" +
                "Content-Type: " + header.getHeader() + "\r\n" +
                "Content-Length: " + f.length() + "\r\n" +
                "Date: " + new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) + " GMT" + "\r\n" +
                "Server: HttpDaemon\r\n" +
                "User-Agent: HTTPDaemon/1.0.0 (Resourcepack Hosting)\r\n\r\n";
    }

    public HttpDaemon getDaemon() {
        return daemon;
    }

    public HttpDaemon.ZipHeader getHeader() {
        return header;
    }

    public Socket getClient() {
        return client;
    }

}
