package org.project.karto.infrastructure.files;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.project.karto.domain.common.containers.Result;

public class StreamUtils {

  private StreamUtils() {
  }

  public static Result<byte[], Throwable> toByteArray(InputStream input) {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      byte[] data = new byte[8192];
      int nRead;
      while ((nRead = input.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }
      input.close();
      return Result.success(buffer.toByteArray());
    } catch (IOException e) {
      return Result.failure(e);
    }
  }
}
