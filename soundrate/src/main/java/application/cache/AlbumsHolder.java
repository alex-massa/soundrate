package application.cache;

import application.model.CatalogAgent;
import deezer.model.Album;
import org.apache.commons.lang3.tuple.MutablePair;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class AlbumsHolder {

    // expressed in minutes
    private static final int ENTRY_TTL_THRESHOLD = 5;

    private static final Map<Long, Optional<MutablePair<Album, Instant>>> albumsMap = new ConcurrentHashMap<>();

    @Inject
    private CatalogAgent catalogAgent;

    @Schedule(hour = "*", minute = "*/5", persistent = false)
    private void cleanUpCache() {
        final Instant now = Instant.now();
        AlbumsHolder.albumsMap.entrySet().removeIf(entry -> {
            final Optional<MutablePair<Album, Instant>> optionalAlbumInstantPair = entry.getValue();
            if (optionalAlbumInstantPair == null || !optionalAlbumInstantPair.isPresent())
                return true;
            return Duration.between(optionalAlbumInstantPair.get().getRight(), now)
                    .toMinutes() >= AlbumsHolder.ENTRY_TTL_THRESHOLD;
        });
    }

    @Lock(LockType.READ)
    public Album getAlbum(@NotNull final Long albumId) {
        final Optional<MutablePair<Album, Instant>> optionalAlbumInstantPair = AlbumsHolder.albumsMap.get(albumId);
        if (optionalAlbumInstantPair != null && optionalAlbumInstantPair.isPresent()) {
            final MutablePair<Album, Instant> albumInstantPair = optionalAlbumInstantPair.get();
            albumInstantPair.setRight(Instant.now());
            return albumInstantPair.getLeft();
        }
        final Album album = this.catalogAgent.getAlbum(albumId);
        AlbumsHolder.albumsMap.put(albumId, album == null
                ? Optional.empty()
                : Optional.of(MutablePair.of(album, Instant.now())));
        return album;
    }

}
