package application;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ThreadLocalRandom;

public final class AvatarGenerator {

    private final static String PATTERN = "http://tinygraphs.com/%s/%s?theme=%s&numcolors=%s&size=%d&fmt=%s";

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

    public static URL generateAvatar(String username, int size, Format format, Shape shape, Theme theme, Colors colors) throws MalformedURLException {
        return new URL(String.format(PATTERN, shape, username, theme, colors, size, format));
    }

    public static URL randomAvatar(String username, int size, Format format) throws MalformedURLException {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Shape shape = Shape.values()[random.nextInt(Shape.values().length)];
        Theme theme = Theme.values()[random.nextInt(Theme.values().length)];
        Colors colors = Colors.values()[random.nextInt(Colors.values().length)];
        return generateAvatar(username, size, format, shape, theme, colors);
    }

}
