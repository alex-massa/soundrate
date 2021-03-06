package application.cache;

import application.model.CatalogAgent;
import deezer.model.Artist;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ArtistsHolder {

    private static final Map<Long, Optional<Artist>> artistsMap = new ConcurrentHashMap<>();

    @Inject
    private CatalogAgent catalogAgent;

    @Schedule(hour = "*", minute = "*/30", persistent = false)
    private void clearCache() {
        ArtistsHolder.artistsMap.clear();
    }

    @Lock(LockType.READ)
    public Artist getArtist(@NotNull final Long artistId) {
        Optional<Artist> optionalArtist = ArtistsHolder.artistsMap.get(artistId);
        if (optionalArtist != null)
            return optionalArtist.orElse(null);
        Artist artist = this.catalogAgent.getArtist(artistId);
        ArtistsHolder.artistsMap.put(artistId, artist == null ? Optional.empty() : Optional.of(artist));
        return artist;
    }

}
