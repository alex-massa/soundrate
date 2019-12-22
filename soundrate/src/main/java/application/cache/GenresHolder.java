package application.cache;

import application.model.CatalogAgent;
import deezer.model.Genre;

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
public class GenresHolder {

    private static final Map<Long, Optional<Genre>> genresMap = new ConcurrentHashMap<>();

    @Inject
    private CatalogAgent catalogAgent;

    @Schedule(hour = "*", minute = "*/30", persistent = false)
    private void clearCache() {
        GenresHolder.genresMap.clear();
    }

    @Lock(LockType.READ)
    public Genre getGenre(@NotNull final Long genreId) {
        Optional<Genre> optionalGenre = GenresHolder.genresMap.get(genreId);
        if (optionalGenre != null)
            return optionalGenre.orElse(null);
        Genre genre = this.catalogAgent.getGenre(genreId);
        GenresHolder.genresMap.put(genreId, genre == null ? Optional.empty() : Optional.of(genre));
        return genre;
    }

}
