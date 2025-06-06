package corsica.comiti.debloater.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import corsica.comiti.debloater.interfaces.ProgressListener;

public class HTTPClient implements ProgressListener {

    private final URL url;
    private final Map<String, String> headers = Collections.synchronizedMap(new HashMap<>());
    private final List<ProgressListener> progressListeners = Collections.synchronizedList(new ArrayList<>());
    private int connectTimeout = 1000;
    private int readTimeout = 1000;

    public HTTPClient(URL url) {
        this.url = url;
        setUserAgent(String.format(
            "Android Debloater HTTPClient/1.0.0 (%s; %s; Java %s)",
            System.getProperty("os.name"),
            System.getProperty("os.arch"),
            System.getProperty("java.version")
        ));
    }

    public String fetch() throws IOException {
        if (url == null) return null;

        HttpURLConnection connection = openConnection(url);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        setRequestHeaders(connection);

        int totalSize = connection.getContentLength();

        try (InputStream in = connection.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

            StringBuilder content = new StringBuilder();
            char[] buffer = new char[4096];
            int read;
            int totalRead = 0;

            while ((read = reader.read(buffer)) != -1) {
                content.append(buffer, 0, read);
                totalRead += read;
                onProgress(totalRead * 2, totalSize);
            }
            return content.toString();
        } finally {
            connection.disconnect();
        }
    }

    public void download(URL url, File file) throws IOException {
        if (url == null) throw new IllegalArgumentException("URL must not be null");
        if (file == null) throw new IllegalArgumentException("Output file must not be null");

        HttpURLConnection connection = openConnection(url);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        setRequestHeaders(connection);

        int totalSize = connection.getContentLength();

        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(file)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            int totalRead = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
                onProgress(totalRead, totalSize);
            }
            out.flush();
        } finally {
            connection.disconnect();
        }
    }

    protected HttpURLConnection openConnection(URL url) throws IOException {
        switch (url.getProtocol().toLowerCase()) {
            case "http":
                return (HttpURLConnection) url.openConnection();
            case "https":
                return (HttpsURLConnection) url.openConnection();
            default:
                throw new IOException("Unsupported protocol: " + url.getProtocol());
        }
    }

    protected void setRequestHeaders(HttpURLConnection connection) {
        for (Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setUserAgent(String userAgent) {
        addHeader("User-Agent", userAgent);
    }

    public void addHeader(String key, String value) {
        if (key == null || value == null) return;
        headers.put(key, value);
    }

    public void removeHeader(String key) {
        if (key != null) {
            headers.remove(key);
        }
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(new HashMap<>(headers));
    }

    public void setHeaders(Map<String, String> newHeaders) {
        if (newHeaders == null || newHeaders.isEmpty()) return;
        synchronized (headers) {
            headers.clear();
            headers.putAll(newHeaders);
        }
    }

    @Override
    public void onProgress(int current, int total) {
        for (ProgressListener listener : getProgressListeners()) {
            listener.onProgress(current, total);
        }
    }

    public List<ProgressListener> getProgressListeners() {
        synchronized (progressListeners) {
            return new ArrayList<>(progressListeners);
        }
    }

    public void setProgressListeners(List<ProgressListener> listeners) {
        if (listeners == null || listeners.isEmpty()) return;
        synchronized (progressListeners) {
            progressListeners.clear();
            progressListeners.addAll(listeners);
        }
    }

    public void addProgressListener(ProgressListener listener) {
        if (listener != null) {
            progressListeners.add(listener);
        }
    }

    public URL getUrl() {
        return url;
    }
}