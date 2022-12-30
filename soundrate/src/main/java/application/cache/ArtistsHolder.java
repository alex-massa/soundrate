package application.cache;

import application.model.CatalogAgent;
import deezer.model.Artist;
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
public class ArtistsHolder {

    // expressed in minutes
    private static final int ENTRY_TTL_THRESHOLD = 5;

    private static final Map<Long, Optional<MutablePair<Artist, Instant>>> artistsMap = new ConcurrentHashMap<>();

    @Inject
    private CatalogAgent catalogAgent;

    @Schedule(hour = "*", minute = "*/5", persistent = false)
    private void cleanUpCache() {
        final Instant now = Instant.now();
        ArtistsHolder.artistsMap.entrySet().removeIf(entry -> {
            final Optional<MutablePair<Artist, Instant>> optionalArtistInstantPair = entry.getValue();
            if (optionalArtistInstantPair == null || !optionalArtistInstantPair.isPresent())
                return true;
            return Duration.between(optionalArtistInstantPair.get().getRight(), now)
                    .toMinutes() >= ArtistsHolder.ENTRY_TTL_THRESHOLD;
        });
    }

    @Lock(LockType.READ)
    public Artist getArtist(@NotNull final Long artistId) {
        final Optional<MutablePair<Artist, Instant>> optionalArtistInstantPair = ArtistsHolder.artistsMap.get(artistId);
        if (optionalArtistInstantPair != null && optionalArtistInstantPair.isPresent()) {
            final MutablePair<Artist, Instant> artistInstantPair = optionalArtistInstantPair.get();
            artistInstantPair.setRight(Instant.now());
            return artistInstantPair.getLeft();
        }
        final Artist artist = this.catalogAgent.getArtist(artistId);
        ArtistsHolder.artistsMap.put(artistId, artist == null
                ? Optional.empty()
                : Optional.of(MutablePair.of(artist, Instant.now())));
        return artist;
    }

}
