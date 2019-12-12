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

    private Albums topAlbums;

    @Inject
    private CatalogAgent catalogAgent;

    @PostConstruct
    @Schedule(hour = "*", minute = "*/30", persistent = false)
    private void clearCache() {
        this.topAlbums = this.catalogAgent.getTopAlbums();
    }

    @Lock(LockType.READ)
    public Albums getTopAlbums() {
        return this.topAlbums;
    }

    @Lock(LockType.READ)
    public Albums getTopAlbums(@NotNull @Min(0) final Integer index, @NotNull @Min(1) final Integer limit) {
        if (this.topAlbums == null)
            return null;
        List<Album> data = this.topAlbums.getData().stream()
                .skip(index)
                .limit(Math.min(this.topAlbums.getTotal() - index, limit))
                .collect(Collectors.toList());
        return new Albums().setData(data).setTotal(this.topAlbums.getTotal());
    }

}
