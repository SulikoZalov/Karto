package org.project.karto.infrastructure.files;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;
import org.project.karto.domain.companies.entities.Company;
import org.project.karto.domain.user.values_objects.PictureOfCards;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CardPicturesRepository {

  public void put(Company company) {
    final PictureOfCards picture = company.picture()
        .orElseThrow(() -> new IllegalDomainArgumentException(
            "Can`t get profile picture. User does`t contains profile picture."));
    final String path = picture.path();
    final byte[] pictureBytes = picture.profilePicture();

    try {
      final Path profilePicturePath = Paths.get(path);

      Files.createDirectories(profilePicturePath.getParent());
      Files.write(profilePicturePath, pictureBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      Log.info("Successfully write a/in file");
    } catch (IOException e) {
      Log.errorf("Something get wrong when attempting to put an image: %s", e.getMessage());
    }
  }

  public Optional<ProfilePicture> load(String path) {
    try {
      final Path profilePicturePath = Paths.get(path);

      final boolean isFileExists = Files.exists(profilePicturePath);
      if (isFileExists) {
        byte[] bytes = Files.readAllBytes(profilePicturePath);

        Log.info("Successfully loaded profile picture");
        return Optional.of(PictureOfCards.fromRepository(path, bytes));
      }
    } catch (IOException e) {
      Log.errorf("Can`t load a picture: %s", e.getMessage());
    }

    Log.info("Do not found picture");
    return Optional.empty();
  }
}
