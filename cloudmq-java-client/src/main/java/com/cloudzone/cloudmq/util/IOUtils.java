package com.cloudzone.cloudmq.util;

import java.io.*;
import java.net.Socket;


public class IOUtils {
    private IOUtils() {
    }


    public static void closeQuietly(Socket sock) {
        // It's same thing as Apache Commons - IOUtils.closeQuietly()
        if (sock != null) {
            try {
                sock.close();
            }
            catch (IOException e) {
                e.printStackTrace();
                // ignored
            }
        }
    }


    public static void closeInputStrerm(InputStream inputStream) {
        // It's same thing as Apache Commons - IOUtils.closeQuietly()
        if (inputStream != null) {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    static public String toString(InputStream input, String encoding) throws IOException {
        if (null == encoding) {
            return toString(new InputStreamReader(input));
        }
        return toString(new InputStreamReader(input, encoding));
    }


    private static String toString(Reader reader) throws IOException {
        CharArrayWriter sw = new CharArrayWriter();
        copy(reader, sw);
        return sw.toString();
    }


    static public long copy(Reader input, Writer output) throws IOException {
        char[] buffer = new char[1 << 12];
        long count = 0;
        for (int n = 0; (n = input.read(buffer)) >= 0;) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
