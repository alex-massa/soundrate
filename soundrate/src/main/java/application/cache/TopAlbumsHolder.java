package application.cache;

import application.model.CatalogAgent;
import deezer.model.Album;
import deezer.model.data.Albums;

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

    private static Albums topAlbums;

    @Inject
    private CatalogAgent catalogAgent;

    // @fixme could fail at startup
    @PostConstruct
    @Schedule(hour = "*", minute = "*/30", persistent = false)
    private void clearCache() {
        TopAlbumsHolder.topAlbums = this.catalogAgent.getTopAlbums();
    }

    @Lock(LockType.READ)
    public Albums getTopAlbums() {
        return TopAlbumsHolder.topAlbums;
    }

    @Lock(LockType.READ)
    public Albums getTopAlbums(@NotNull @Min(0) final Integer index, @NotNull @Min(1) final Integer limit) {
        if (TopAlbumsHolder.topAlbums == null)
            return null;
        List<Album> data = TopAlbumsHolder.topAlbums.getData().stream()
                .skip(index)
                .limit(limit)
                .collect(Collectors.toList());
        return data.isEmpty() ? null : new Albums().setData(data).setTotal(TopAlbumsHolder.topAlbums.getTotal());
    }

}
