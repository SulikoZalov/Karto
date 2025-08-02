package org.project.karto.application.dto.common;

public record QR(byte[] value) {
  public byte[] value() {
    return value.clone();
  }
}
