package org.project.karto.domain.companies.value_objects;

import static org.project.karto.domain.common.util.Utils.required;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;
import org.project.karto.domain.companies.entities.Company;

public final class PictureOfCards {
  private final String path;
  private final String imageType;
  private final byte[] profilePicture;

  private static final int MAX_SIZE = 2_097_152;
  private static final String PATH_FORMAT = "src/main/resources/static/profile/photos/%s";

  private static final byte[][] IMAGE_SIGNATURES = {
      // JPEG (starts with FF D8)
      { (byte) 0xFF, (byte) 0xD8 },
      // PNG (starts with 89 50 4E 47)
      { (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47 }
  };

  private static final String[] IMAGE_EXTENSIONS = { "jpeg", "png" };

  private PictureOfCards(String path, byte[] profilePicture, String imageType) {
    this.path = path;
    this.profilePicture = profilePicture.clone();
    this.imageType = imageType;
  }

  public static PictureOfCards of(byte[] profilePicture, Company company) {
    required("profilePicture", profilePicture);
    required("company", company);

    String path = profilePicturePath(company.id().toString());
    String typeOfImage = validate(profilePicture)
        .orElseThrow(() -> new IllegalDomainArgumentException("Invalid profile picture type."));

    return new PictureOfCards(path, profilePicture, typeOfImage);
  }

  public static PictureOfCards fromRepository(String path, byte[] profilePicture) {
    return new PictureOfCards(path, profilePicture,
        checkImageExtension(profilePicture).orElseThrow(
            () -> new IllegalDomainArgumentException("Invalid profile picture type.")));
  }

  public static String profilePicturePath(String id) {
    return String.format(PATH_FORMAT, id);
  }

  public String path() {
    return path;
  }

  public byte[] profilePicture() {
    return profilePicture.clone();
  }

  public String imageType() {
    return imageType;
  }

  private static Optional<String> validate(byte[] profilePicture) {
    if (profilePicture.length > MAX_SIZE)
      return Optional.empty();
    return checkImageExtension(profilePicture);
  }

  private static Optional<String> checkImageExtension(byte[] profilePicture) {
    for (int i = 0; i < IMAGE_SIGNATURES.length; i++) {
      byte[] imageSignature = IMAGE_SIGNATURES[i];
      if (matchesSignature(profilePicture, imageSignature))
        return Optional.of(IMAGE_EXTENSIONS[i]);
    }
    return Optional.empty();
  }

  private static boolean matchesSignature(byte[] file, byte[] signature) {
    if (file.length < signature.length)
      return false;
    for (int i = 0; i < signature.length; i++) {
      if (file[i] != signature[i])
        return false;
    }
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PictureOfCards that))
      return false;
    return Objects.equals(path, that.path) &&
        Objects.equals(imageType, that.imageType) &&
        Arrays.equals(profilePicture, that.profilePicture);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(path, imageType);
    result = 31 * result + Arrays.hashCode(profilePicture);
    return result;
  }
}
