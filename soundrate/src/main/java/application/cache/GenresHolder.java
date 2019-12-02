package application.cache;

import application.model.DataAgent;
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

    private final Map<Long, Optional<Genre>> genresMap = new ConcurrentHashMap<>();

    @Inject
    private DataAgent dataAgent;

    @Schedule(hour = "*", minute = "*/30", persistent = false)
    private void clearCache() {
        this.genresMap.clear();
    }

    @Lock(LockType.READ)
    public Genre getGenre(@NotNull final Long genreId) {
        Optional<Genre> optionalGenre = this.genresMap.get(genreId);
        if (optionalGenre != null)
            return optionalGenre.orElse(null);
        Genre genre = this.dataAgent.getGenre(genreId);
        this.genresMap.put(genreId, genre == null ? Optional.empty() : Optional.of(genre));
        return genre;
    }

}
