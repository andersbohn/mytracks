package com.andersbohn.mytracks.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<Track, UUID> {
  List<Track> findByUser(User user);

  Optional<Track> findByUserAndSourceId(User user, String sourceId);
}
