package org.daiitech.naftah.utils;

import static org.daiitech.naftah.utils.arabic.ArabicUtils.padText;

public class ConsoleLoader {
  private static final char[] SPINNER = {'|', '/', '-', '\\'};
  private static Thread LOADER_THREAD;

  public static void startLoader(String text) {
    LOADER_THREAD =
        new Thread(
            () -> {
              int i = 0;
              int j = 0;
              while (!Thread.currentThread().isInterrupted()) {
                clearScreen();
                System.out.print(
                    padText(
                        String.format(
                            "%c %s %c",
                            SPINNER[i++ % SPINNER.length], text, SPINNER[j++ % SPINNER.length]), false));
                try {
                  Thread.sleep(150); // control speed
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }
              }
            });

    LOADER_THREAD.start();
  }

  public static void stopLoader() {
    if (LOADER_THREAD.isAlive()) LOADER_THREAD.interrupt();
    clearScreen();
  }

  public static void clearScreen() {
    // ANSI escape code to clear screen and move cursor to top-left
    System.out.print("\033[H\033[2J");
    System.out.flush();
  }
}
