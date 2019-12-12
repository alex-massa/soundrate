package application.cache;

import application.model.CatalogAgent;
import deezer.model.Artist;

import javax.ejb.*;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Startup
public class ArtistsHolder {

    private final Map<Long, Optional<Artist>> artistsMap = new ConcurrentHashMap<>();

    @Inject
    private CatalogAgent catalogAgent;

    @Schedule(hour = "*", minute = "*/30", persistent = false)
    private void clearCache() {
        this.artistsMap.clear();
    }

    @Lock(LockType.READ)
    public Artist getArtist(@NotNull final Long artistId) {
        Optional<Artist> optionalArtist = this.artistsMap.get(artistId);
        if (optionalArtist != null)
            return optionalArtist.orElse(null);
        Artist artist = this.catalogAgent.getArtist(artistId);
        this.artistsMap.put(artistId, artist == null ? Optional.empty() : Optional.of(artist));
        return artist;
    }

}
