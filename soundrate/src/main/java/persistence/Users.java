package persistence;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.transfer.Review;
import model.transfer.User;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public final class Users {

    private static final String USERS_PATH = "data/users.json";
    private static final String REVIEWS_PATH = "data/reviews.json";

    private static Gson serializer = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    private static List<User> users;
    private static List<Review> reviews;

    static {
        try {
            users = loadUsers();
            reviews = loadReviews();
        } catch (JsonIOException | JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    private Users() {
        throw new UnsupportedOperationException();
    }

    private static List<User> loadUsers() throws JsonIOException, JsonSyntaxException {
        JsonArray data = new JsonParser()
                .parse(new InputStreamReader(Users.class.getClassLoader().getResourceAsStream(USERS_PATH), StandardCharsets.UTF_8))
                .getAsJsonArray();
        List<User> users = serializer.fromJson(data, new TypeToken<List<User>>() {
        }.getType());
        return users == null ? new LinkedList<>() : users;
    }

    private static List<Review> loadReviews() throws JsonIOException, JsonSyntaxException {
        JsonArray data = new JsonParser()
                .parse(new InputStreamReader(Users.class.getClassLoader().getResourceAsStream(REVIEWS_PATH), StandardCharsets.UTF_8))
                .getAsJsonArray();
        List<Review> reviews = serializer.fromJson(data, new TypeToken<List<Review>>() {
        }.getType());
        return reviews == null ? new LinkedList<>() : reviews;
    }

    public static List<User> getUsers() {
        return users;
    }

    public static List<Review> getReviews() {
        return reviews;
    }

}
