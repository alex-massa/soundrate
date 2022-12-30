package application.cache;

import application.model.CatalogAgent;
import deezer.model.Genre;
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
public class GenresHolder {

    // expressed in minutes
    private static final int ENTRY_TTL_THRESHOLD = 5;

    private static final Map<Long, Optional<MutablePair<Genre, Instant>>> genresMap = new ConcurrentHashMap<>();

    @Inject
    private CatalogAgent catalogAgent;

    @Schedule(hour = "*", minute = "*/5", persistent = false)
    private void cleanupCache() {
        final Instant now = Instant.now();
        GenresHolder.genresMap.entrySet().removeIf(entry -> {
            final Optional<MutablePair<Genre, Instant>> optionalGenreInstantPair = entry.getValue();
            if (optionalGenreInstantPair == null || !optionalGenreInstantPair.isPresent())
                return true;
            return Duration.between(optionalGenreInstantPair.get().getRight(), now)
                    .toMinutes() >= GenresHolder.ENTRY_TTL_THRESHOLD;
        });
    }

    @Lock(LockType.READ)
    public Genre getGenre(@NotNull final Long genreId) {
        final Optional<MutablePair<Genre, Instant>> optionalGenreInstantPair = GenresHolder.genresMap.get(genreId);
        if (optionalGenreInstantPair != null && optionalGenreInstantPair.isPresent()) {
            final MutablePair<Genre, Instant> genreInstantPair = optionalGenreInstantPair.get();
            genreInstantPair.setRight(Instant.now());
            return genreInstantPair.getLeft();
        }
        final Genre genre = this.catalogAgent.getGenre(genreId);
        GenresHolder.genresMap.put(genreId, genre == null
                ? Optional.empty()
                : Optional.of(MutablePair.of(genre, Instant.now())));
        return genre;
    }

}
