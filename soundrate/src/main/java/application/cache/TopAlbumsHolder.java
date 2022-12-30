package application.cache;

import application.model.CatalogAgent;
import deezer.model.Album;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class TopAlbumsHolder {

    private static List<Album> topAlbums;

    @Inject
    private CatalogAgent catalogAgent;

    // @fixme could fail at startup if disconnected or API offline
    @PostConstruct
    @Schedule(hour = "*", minute = "*/30", persistent = false)
    private void clearCache() {
        TopAlbumsHolder.topAlbums = this.catalogAgent.getTopAlbums();
    }

    @Lock(LockType.READ)
    public List<Album> getTopAlbums() {
        return TopAlbumsHolder.topAlbums;
    }

    @Lock(LockType.READ)
    public List<Album> getTopAlbums(@NotNull @Min(0) final Integer index, @NotNull @Min(1) final Integer limit) {
        if (TopAlbumsHolder.topAlbums == null || TopAlbumsHolder.topAlbums.isEmpty())
            return null;
        return TopAlbumsHolder.topAlbums.stream()
                .skip(index)
                .limit(limit)
                .collect(Collectors.toList());
    }

}
