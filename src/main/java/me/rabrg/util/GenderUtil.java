package me.rabrg.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GenderUtil {

    private static final Map<String, Gender> map = new HashMap<>();

    public enum Gender {
        MALE, FEMALE, UNSPECIFIED
    }

    public static Gender getGender(final String name) {
        if (map.isEmpty()) {
            try {
                final BufferedReader reader = new BufferedReader(new FileReader("nam_dict.txt"));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("?"))
                        continue;
                    final String[] elements = line.split("  ");
                    map.put(name, elements[0].equals("M") ? Gender.MALE : elements[0].equals("F") ? Gender.FEMALE
                            : Gender.UNSPECIFIED);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return map.getOrDefault(name, Gender.UNSPECIFIED);
    }
}
