package application.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class AvatarGenerator {
    private static final String DYNAMIC_URL =
            "https://avatars.dicebear.com/4.10/api/identicon/%s.svg?colors[]=%s&colorLevel=%s";

    public enum Colors {
        AMBER {
            @Override
            public String toString() {
                return "amber";
            }
        },
        BLUE {
            @Override
            public String toString() {
                return "blue";
            }
        },
        BLUE_GREY {
            @Override
            public String toString() {
                return "blueGrey";
            }
        },
        BROWN {
            @Override
            public String toString() {
                return "brown";
            }
        },
        CYAN {
            @Override
            public String toString() {
                return "cyan";
            }
        },
        DEEP_ORANGE {
            @Override
            public String toString() {
                return "deepOrange";
            }
        },
        DEEP_PURPLE {
            @Override
            public String toString() {
                return "deepPurple";
            }
        },
        GREEN {
            @Override
            public String toString() {
                return "green";
            }
        },
        GREY {
            @Override
            public String toString() {
                return "grey";
            }
        },
        INDIGO {
            @Override
            public String toString() {
                return "indigo";
            }
        },
        LIGHT_BLUE {
            @Override
            public String toString() {
                return "lightBlue";
            }
        },
        LIGHT_GREEN {
            @Override
            public String toString() {
                return "lightGreen";
            }
        },
        LIME {
            @Override
            public String toString() {
                return "lime";
            }
        },
        ORANGE {
            @Override
            public String toString() {
                return "orange";
            }
        },
        PINK {
            @Override
            public String toString() {
                return "pink";
            }
        },
        PURPLE {
            @Override
            public String toString() {
                return "purple";
            }
        },
        RED {
            @Override
            public String toString() {
                return "red";
            }
        },
        TEAL {
            @Override
            public String toString() {
                return "teal";
            }
        },
        YELLOW {
            @Override
            public String toString() {
                return "yellow";
            }
        },
    }

    public enum ColorLevel {
        ONE {
            @Override
            public String toString() {
                return "100";
            }
        },
        TWO {
            @Override
            public String toString() {
                return "200";
            }
        },
        THREE {
            @Override
            public String toString() {
                return "300";
            }
        },
        FOUR {
            @Override
            public String toString() {
                return "400";
            }
        },
        FIVE {
            @Override
            public String toString() {
                return "500";
            }
        },
        SIX {
            @Override
            public String toString() {
                return "600";
            }
        },
        SEVEN {
            @Override
            public String toString() {
                return "700";
            }
        },
        EIGHT {
            @Override
            public String toString() {
                return "800";
            }
        },
        NINE {
            @Override
            public String toString() {
                return "900";
            }
        }
    }

    public static URL generateAvatarUrl(final String username, final AvatarGenerator.Colors color,
                                        final AvatarGenerator.ColorLevel colorLevel) {
        try {
            return new URL(String.format(AvatarGenerator.DYNAMIC_URL, hashString(username), color, colorLevel));
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static URL generateRandomAvatarUrl(final String username) {
        final Random random = ThreadLocalRandom.current();
        final AvatarGenerator.Colors color = AvatarGenerator.Colors.values()
                [random.nextInt(AvatarGenerator.Colors.values().length)];
        final AvatarGenerator.ColorLevel colorLevel = AvatarGenerator.ColorLevel.values()
                [random.nextInt(AvatarGenerator.ColorLevel.values().length)];
        return AvatarGenerator.generateAvatarUrl(username, color, colorLevel);
    }

    private static String hashString(final String input) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-1");
            final byte[] hash = digest.digest(input.getBytes());
            final StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
