package application.cache;

import application.model.CatalogAgent;
import deezer.model.Album;
import deezer.model.Artist;
import org.apache.commons.lang3.tuple.MutablePair;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class ArtistAlbumsHolder {


    // expressed in minutes
    private static final int ENTRY_TTL_THRESHOLD = 5;

    private static final Map<Artist, Optional<MutablePair<List<Album>, Instant>>> artistAlbumsMap =
            new ConcurrentHashMap<>();

    @Inject
    private CatalogAgent catalogAgent;

    @Schedule(hour = "*", minute = "*/5", persistent = false)
    private void cleanupCache() {
        final Instant now = Instant.now();
        ArtistAlbumsHolder.artistAlbumsMap.entrySet().removeIf(entry -> {
            final Optional<MutablePair<List<Album>, Instant>> optionalArtistAlbumsInstantPair = entry.getValue();
            if (optionalArtistAlbumsInstantPair == null || !optionalArtistAlbumsInstantPair.isPresent())
                return true;
            return Duration.between(optionalArtistAlbumsInstantPair.get().getRight(), now)
                    .toMinutes() >= ArtistAlbumsHolder.ENTRY_TTL_THRESHOLD;
        });
    }

    @Lock(LockType.READ)
    public List<Album> getArtistAlbums(@NotNull final Artist artist) {
        final Optional<MutablePair<List<Album>, Instant>> optionalArtistAlbumsInstantPair =
                ArtistAlbumsHolder.artistAlbumsMap.get(artist);
        if (optionalArtistAlbumsInstantPair != null && optionalArtistAlbumsInstantPair.isPresent()) {
            final MutablePair<List<Album>, Instant> artistAlbumsInstantPair = optionalArtistAlbumsInstantPair.get();
            artistAlbumsInstantPair.setRight(Instant.now());
            return artistAlbumsInstantPair.getLeft();
        }
        final List<Album> artistAlbums = this.catalogAgent.getArtistAlbums(artist);
        ArtistAlbumsHolder.artistAlbumsMap.put(artist, artistAlbums == null
                ? Optional.empty()
                : Optional.of(MutablePair.of(artistAlbums, Instant.now())));
        return artistAlbums;
    }

    @Lock(LockType.READ)
    public List<Album> getArtistAlbums
            (@NotNull final Artist artist, @NotNull @Min(0) final Integer index, @NotNull @Min(1) final Integer limit) {
        final List<Album> artistAlbums = this.getArtistAlbums(artist);
        if (artistAlbums == null || artistAlbums.isEmpty())
            return null;
        return artistAlbums.stream()
                .skip(index)
                .limit(Math.min(artistAlbums.size() - index, limit))
                .collect(Collectors.toList());
    }

}
