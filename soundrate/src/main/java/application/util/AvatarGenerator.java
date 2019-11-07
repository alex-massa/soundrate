package application.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class AvatarGenerator {

    private static final String PATTERN = "http://tinygraphs.com/%s/%s?theme=%s&numcolors=%s&size=%d&fmt=%s";

    public enum Shape {
        SQUARES {
            @Override
            public String toString() {
                return "squares";
            }
        },
        ISOGRIDS {
            @Override
            public String toString() {
                return "isogrids";
            }
        },
        SPACE_INVADERS {
            @Override
            public String toString() {
                return "spaceinvaders";
            }
        },
        HEXA {
            @Override
            public String toString() {
                return "labs/isogrids/hexa";
            }
        },
        HEXA16 {
            @Override
            public String toString() {
                return "labs/isogrids/hexa16";
            }
        }
    }

    public enum Theme {
        FROGIDEAS {
            @Override
            public String toString() {
                return "frogideas";
            }
        },
        SUGARSWEETS {
            @Override
            public String toString() {
                return "sugarsweets";
            }
        },
        HEATWAVE {
            @Override
            public String toString() {
                return "heatwave";
            }
        },
        DAISYGARDEN {
            @Override
            public String toString() {
                return "daisygarden";
            }
        },
        SEASCAPE {
            @Override
            public String toString() {
                return "seascape";
            }
        },
        SUMMERWARMTH {
            @Override
            public String toString() {
                return "summerwarmth";
            }
        },
        BYTHEPOOL {
            @Override
            public String toString() {
                return "bythepool";
            }
        },
        DUSKFALLING {
            @Override
            public String toString() {
                return "duskfalling";
            }
        },
        BERRYPIE {
            @Override
            public String toString() {
                return "berrypie";
            }
        },
        BASE {
            @Override
            public String toString() {
                return "base";
            }
        }
    }

    public enum Colors {
        TWO {
            @Override
            public String toString() {
                return "2";
            }
        },
        THREE {
            @Override
            public String toString() {
                return "3";
            }
        },
        FOUR {
            @Override
            public String toString() {
                return "4";
            }
        }
    }

    public enum Format {
        SVG {
            @Override
            public String toString() {
                return "svg";
            }
        },
        JPG {
            @Override
            public String toString() {
                return "jpg";
            }
        },
        PNG {
            @Override
            public String toString() {
                return "png";
            }
        }
    }

    private AvatarGenerator() {
        throw new UnsupportedOperationException();
    }

    public static URL generateAvatar(final String username, final int size,
                                     final AvatarGenerator.Format format, final AvatarGenerator.Shape shape,
                                     final AvatarGenerator.Theme theme, final AvatarGenerator.Colors colors) {
        try {
            return new URL(String.format(AvatarGenerator.PATTERN, shape, username, theme, colors, size, format));
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static URL randomAvatar(final String username, final int size, final AvatarGenerator.Format format) {
        final Random random = ThreadLocalRandom.current();
        final AvatarGenerator.Shape shape = AvatarGenerator.Shape.values()
                [random.nextInt(AvatarGenerator.Shape.values().length)];
        final AvatarGenerator.Theme theme = AvatarGenerator.Theme.values()
                [random.nextInt(AvatarGenerator.Theme.values().length)];
        final AvatarGenerator.Colors colors = AvatarGenerator.Colors.values()
                [random.nextInt(AvatarGenerator.Colors.values().length)];
        return AvatarGenerator.generateAvatar(username, size, format, shape, theme, colors);
    }

}
