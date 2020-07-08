package client.gui.image;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ImageLocation {
  private String relativePath;

  public ImageLocation(String relativePath) {
    this.relativePath = relativePath;
  }

  public String localPath() {
    if(hasLocalFile()) {
      return relativePath();
    } else {
      System.out.println("Path translation (" + relativePath() + " -> " + assetPath() + ") applied");
      return assetPath();
    }
  }

  public String localPathJXParsed() {
    return "file:" + localPath();
  }

  public ImageLocation loaded() {
    if(!hasLocalFile()) {
      try {
        String pathInJar = relativePath();
        if(!pathInJar.startsWith("/")) {
          pathInJar = "/" + pathInJar;
        }
        InputStream resourceAsStream = ImageLocation.class.getResourceAsStream(pathInJar);
        Path target = new File(assetPath()).toPath();
        File targetFile = new File(target.toUri());
        if (!targetFile.exists()) {
          System.out.println("Copying resource " + pathInJar + " to "+ targetFile + "...");
          targetFile.deleteOnExit();
          Files.copy(resourceAsStream, target);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return this;
  }

  private String assetPath() {
    File file = new File(assetDirectory(), "Quizduell/" + relativePath());
    file.getParentFile().mkdirs();
    return file.getAbsolutePath();
  }

  private File assetDirectory() {
    return new File(System.getenv("APPDATA"));
  }

  private boolean hasLocalFile() {
    return new File(relativePath()).exists();
  }

  public String relativePath() {
    return relativePath;
  }
}
