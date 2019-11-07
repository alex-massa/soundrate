package application.cache;

import application.business.DataAgent;
import deezer.model.Album;
import deezer.model.Artist;
import deezer.model.data.Albums;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class ArtistAlbumsHolder {

    private final Map<Artist, Optional<Albums>> artistAlbumsMap = new ConcurrentHashMap<>();

    @Inject
    private DataAgent dataAgent;

    @Schedule(hour = "*", minute = "*/30", persistent = false)
    private void clearCache() {
        this.artistAlbumsMap.clear();
    }

    @Lock(LockType.READ)
    public Albums getArtistAlbums(@NotNull final Artist artist) {
        Optional<Albums> optionalArtistAlbums = this.artistAlbumsMap.get(artist);
        if (optionalArtistAlbums != null)
            return optionalArtistAlbums.orElse(null);
        Albums artistAlbums = this.dataAgent.getArtistAlbums(artist);
        this.artistAlbumsMap.put(artist, artistAlbums == null ? Optional.empty() : Optional.of(artistAlbums));
        return artistAlbums;
    }

    @Lock(LockType.READ)
    public Albums getArtistAlbums
            (@NotNull final Artist artist, @NotNull @Min(0) final Integer index, @NotNull @Min(1) final Integer limit) {
        Albums artistAlbums = this.getArtistAlbums(artist);
        if (artistAlbums == null)
            return null;
        List<Album> data = artistAlbums.getData().stream()
                .skip(index)
                .limit(Math.min(artistAlbums.getData().size() - index, limit))
                .collect(Collectors.toList());
        return new Albums().setData(data).setTotal(artistAlbums.getTotal());
    }

}
