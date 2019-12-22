package application.cache;

import application.model.CatalogAgent;
import deezer.model.Album;

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
public class AlbumsHolder {

    private static final Map<Long, Optional<Album>> albumsMap = new ConcurrentHashMap<>();

    @Inject
    private CatalogAgent catalogAgent;

    @Schedule(hour = "*", minute = "*/30", persistent = false)
    private void clearCache() {
        AlbumsHolder.albumsMap.clear();
    }

    @Lock(LockType.READ)
    public Album getAlbum(@NotNull final Long albumId) {
        Optional<Album> optionalAlbum = AlbumsHolder.albumsMap.get(albumId);
        if (optionalAlbum != null)
            return optionalAlbum.orElse(null);
        Album album = this.catalogAgent.getAlbum(albumId);
        AlbumsHolder.albumsMap.put(albumId, album == null ? Optional.empty() : Optional.of(album));
        return album;
    }

}
