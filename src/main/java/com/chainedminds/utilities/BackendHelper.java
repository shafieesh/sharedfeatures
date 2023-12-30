package com.chainedminds.utilities;

import com.chainedminds._Config;

import java.awt.*;
import java.util.Calendar;
import java.util.Random;
import java.util.Set;

public class BackendHelper {

    private static final String[] CHARS = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
            "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "1", "2", "3", "4", "5", "6", "7", "8", "9"};

    public static int getDigitsCount(int n) {

        if (n < 100000) {

            if (n < 100) {

                if (n < 10) {

                    return 1;

                } else {

                    return 2;
                }

            } else {

                if (n < 1000) {

                    return 3;

                } else {

                    if (n < 10000) {

                        return 4;

                    } else {

                        return 5;
                    }
                }
            }

        } else {

            if (n < 10000000) {

                if (n < 1000000) {

                    return 6;

                } else {

                    return 7;
                }

            } else {

                if (n < 100000000) {

                    return 8;

                } else {

                    if (n < 1000000000) {

                        return 9;

                    } else {

                        return 10;
                    }
                }
            }
        }
    }

    public static long getFirstDayOfTheWeekMills(String language) {

        int[] days = new int[]{Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
                Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY};

        Calendar calendar = Calendar.getInstance();

        int firstDayOfTheWeek = "fa".equalsIgnoreCase(language) ? Calendar.SATURDAY : Calendar.SUNDAY;
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        int diff = 0;

        for (int i = today - 1; ; i--) {

            if (i < 0) {

                i = days.length - 1;
            }

            if (firstDayOfTheWeek == days[i]) {

                break;
            }

            diff++;
        }

        calendar.add(Calendar.DAY_OF_MONTH, -diff);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static long getTodayMills() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static int generateID(Set<Integer> ids) {

        int index = 0;

        do {

            index++;

        } while (ids.contains(index));

        return index;
    }

    public static String generateCredential() {

        Random random = new Random();

        StringBuilder token = new StringBuilder();

        for (int i = 0; i < 45; i++) {

            token.append(CHARS[random.nextInt(CHARS.length)]);
        }

        return token.toString();
    }

    public static String getLanguageWithMarket(String market) {

        if (_Config.MARKET_GOOGLE_PLAY.equals(market)) {

            return _Config.LANGUAGE_EN;
        }

        return _Config.LANGUAGE_FA;
    }

    public static String getPaymentType(String market) {

        if (market.equals(_Config.MARKET_CAFEBAZAAR)) {

            return _Config.PAYMENT_OPTION_IAB;
        }

        if (market.equals(_Config.MARKET_TOOSKA)) {

            return _Config.PAYMENT_OPTION_IPG;
        }

        if (market.equals(_Config.MARKET_VADA) || market.equals(_Config.MARKET_ROYAL)) {

            return _Config.PAYMENT_OPTION_VAS;
        }

        return _Config.PAYMENT_OPTION_IAB;
    }

    public static int parseColor(String colorString) {

        if (colorString.charAt(0) == '#') {

            // Use a long to avoid rollovers on #ffXXXXXX
            long color = Long.parseLong(colorString.substring(1), 16);

            if (colorString.length() == 7) {

                // Set the alpha value
                color |= 0x00000000ff000000;

            } else if (colorString.length() != 9) {

                throw new IllegalArgumentException("Unknown color");
            }

            return (int) color;
        }

        throw new IllegalArgumentException("Unknown color");
    }

    public static String darker(String colorString, double fraction) {

        Color color = new Color(parseColor(colorString));

        int red = (int) Math.round(color.getRed() * (1.0 - fraction));
        int green = (int) Math.round(color.getGreen() * (1.0 - fraction));
        int blue = (int) Math.round(color.getBlue() * (1.0 - fraction));

        if (red < 0) red = 0;
        else if (red > 255) red = 255;
        if (green < 0) green = 0;
        else if (green > 255) green = 255;
        if (blue < 0) blue = 0;
        else if (blue > 255) blue = 255;

        int alpha = color.getAlpha();

        int newColor = new Color(red, green, blue, alpha).getRGB();

        return String.format("#%06X", (0xFFFFFF & newColor));
    }
}
