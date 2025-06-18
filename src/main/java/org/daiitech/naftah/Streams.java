package org.daiitech.naftah;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Chakib Daii
 */
public final class Streams {

  private Streams() {}

  /**
   * Opens an {@link InputStream} reading from the given URL with/without caching the stream. This
   * prevents file descriptor leaks when reading from file system URLs.
   *
   * @param url the URL to connect to
   * @return an input stream reading from the URL connection
   */
  public static InputStream openStream(URL url, boolean useCaches) throws IOException {
    URLConnection urlConnection = url.openConnection();
    urlConnection.setUseCaches(useCaches);
    return urlConnection.getInputStream();
  }
}
