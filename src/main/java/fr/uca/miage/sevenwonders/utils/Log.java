package fr.uca.miage.sevenwonders.utils;

import fr.uca.miage.sevenwonders.models.card.Card;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Central logging utility for the Seven Wonders simulation.
 * <p>
 * This class collects every log entry generated during the execution of the
 * program. Logs are grouped hierarchically by:
 * <ul>
 * <li>Age (age_1, age_2, age_3)</li>
 * <li>Turn number inside the age</li>
 * <li>Misc events (no age/turn context)</li>
 * </ul>
 *
 */
public final class Log {

    private static final Path LOG_DIR = Paths.get("log");
    private static final Path LOG_FILE;
    private static final JSONObject ROOT = new JSONObject();
    private static final ReentrantLock lock = new ReentrantLock();
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    /** The currently active age, provided by the Session. */
    private static Card.Age currentAge = null;

    /** The currently active turn number. */
    private static int currentTurn = -1;

    private static boolean isDetailedMode = true;

    static {
        try {
            if (!Files.exists(LOG_DIR)) {
                Files.createDirectories(LOG_DIR);
            }

            String fileName = LocalDateTime.now().format(FILE_TS) + ".log";
            LOG_FILE = LOG_DIR.resolve(fileName);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (!isDetailedMode) {
                    // Si nous ne sommes pas en mode détaillé, nous ne générons pas de fichier log.
                    return;
                }
                lock.lock();
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("{\n");

                    boolean firstEntry = true;

                    for (int age = 1; age <= 3; age++) {
                        String key = "age_" + age;
                        if (ROOT.has(key)) {
                            if (!firstEntry) {
                                sb.append(",\n");
                            }
                            indent(sb, 1);
                            writeJsonString(key, sb);
                            sb.append(": ");
                            writeJsonValue(ROOT.get(key), sb, 1);
                            firstEntry = false;
                        }
                    }

                    if (ROOT.has("misc")) {
                        if (!firstEntry) {
                            sb.append(",\n");
                        }
                        indent(sb, 1);
                        writeJsonString("misc", sb);
                        sb.append(": ");
                        writeJsonValue(ROOT.get("misc"), sb, 1);
                    }

                    sb.append("\n}\n");

                    Files.writeString(LOG_FILE, sb.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                } finally {
                    lock.unlock();
                }
            }));

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to initialize log file", e);
        }
    }

    private Log() {
    }

    public static void setSimulationMode(boolean isDetailed) {
        isDetailedMode = isDetailed;
    }

    /**
     * Defines both the current Age and the current turn index for all following log
     * entries.
     *
     * @param age
     *            the current Age of the game
     * @param turnNumber
     *            the current turn number inside this Age
     */
    public static void setContext(Card.Age age, int turnNumber) {
        currentAge = age;
        currentTurn = turnNumber;
    }

    /**
     * Defines only the current Age. Useful for global events occurring at the start
     * of an Age.
     *
     * @param age
     *            the Age to set as current
     */
    public static void setAgeContext(Card.Age age) {
        currentAge = age;
    }

    /**
     * Logs a global event belonging to a given Age. These events appear in:
     *
     * <pre>
     * {
     *   "age_1": { "time": "..." }
     * }
     * </pre>
     *
     * @param age
     *            the Age concerned by the event
     * @param description
     *            human-readable description of the event
     */
    public static void logAge(Card.Age age, String description) {
        if (age == null)
            return;
        if (description == null)
            description = "";

        if (!isDetailedMode)
            return;

        String ageKey = "age_" + age.getValue();

        lock.lock();
        try {
            JSONObject ageObj = ROOT.optJSONObject(ageKey);
            if (ageObj == null) {
                ageObj = new JSONObject();
                ROOT.put(ageKey, ageObj);
            }
            ageObj.put("time", description);

        } finally {
            lock.unlock();
        }
    }

    /**
     * Logs a turn-scoped event inside an Age. Events are stored under:
     *
     * <pre>
     * {
     *   "age_1": {
     *      "turns": {
     *         "turn_1": [ {...}, {...} ],
     *         "turn_2": [ {...} ]
     *      }
     *   }
     * }
     * </pre>
     *
     * @param age
     *            the current Age
     * @param turnNumber
     *            the turn index
     * @param description
     *            description of the event
     */
    public static void logTurn(Card.Age age, int turnNumber, String description) {
        if (!isDetailedMode)
            return;
        if (age == null)
            return;
        if (description == null)
            description = "";

        String ageKey = "age_" + age.getValue();
        String turnKey = "turn_" + turnNumber;

        lock.lock();
        try {
            JSONObject ageObj = ROOT.optJSONObject(ageKey);
            if (ageObj == null) {
                ageObj = new JSONObject();
                ROOT.put(ageKey, ageObj);
            }

            JSONObject turnsObj = ageObj.optJSONObject("turns");
            if (turnsObj == null) {
                turnsObj = new JSONObject();
                ageObj.put("turns", turnsObj);
            }

            JSONArray turnArray = turnsObj.optJSONArray(turnKey);
            if (turnArray == null) {
                turnArray = new JSONArray();
                turnsObj.put(turnKey, turnArray);
            }

            JSONObject event = new JSONObject();
            event.put("ts", Instant.now().toString());
            event.put("description", description);

            turnArray.put(event);

        } finally {
            lock.unlock();
        }
    }

    /**
     * Logs an event automatically routed either to:
     * <ul>
     * <li>The current Age/Turn (if context is active)</li>
     * <li>The global misc section otherwise</li>
     * </ul>
     *
     * @param description
     *            event description
     */
    public static void logEvent(String description) {
        if (description == null)
            description = "";

        lock.lock();
        try {
            if (currentAge != null && currentTurn > 0) {
                logTurn(currentAge, currentTurn, description);
                return;
            }
            save(description);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Saves an event in the "misc" section (events not bound to age/turn).
     *
     * @param description
     *            the event description
     */
    public static void save(String description) {
        if (!isDetailedMode)
            return;

        if (description == null)
            description = "";

        lock.lock();
        try {
            JSONArray misc = ROOT.optJSONArray("misc");
            if (misc == null) {
                misc = new JSONArray();
                ROOT.put("misc", misc);
            }

            JSONObject event = new JSONObject();
            event.put("ts", Instant.now().toString());
            event.put("description", description);

            misc.put(event);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Recursive low-level JSON serializer for objects and arrays.
     */
    private static void writeJsonValue(Object value, StringBuilder out, int indent) {
        if (value == null || value == JSONObject.NULL) {
            out.append("null");
        } else if (value instanceof JSONObject obj) {
            out.append("{");
            var keys = obj.keySet().iterator();
            if (keys.hasNext()) {
                out.append("\n");
            }
            boolean first = true;
            for (String key : obj.keySet()) {
                if (!first) {
                    out.append(",\n");
                }
                indent(out, indent + 1);
                writeJsonString(key, out);
                out.append(": ");
                writeJsonValue(obj.get(key), out, indent + 1);
                first = false;
            }
            if (!obj.keySet().isEmpty()) {
                out.append("\n");
                indent(out, indent);
            }
            out.append("}");
        } else if (value instanceof JSONArray arr) {
            out.append("[");
            if (arr.length() > 0) {
                out.append("\n");
            }
            for (int i = 0; i < arr.length(); i++) {
                if (i > 0) {
                    out.append(",\n");
                }
                indent(out, indent + 1);
                writeJsonValue(arr.get(i), out, indent + 1);
            }
            if (arr.length() > 0) {
                out.append("\n");
                indent(out, indent);
            }
            out.append("]");
        } else if (value instanceof String s) {
            writeJsonString(s, out);
        } else if (value instanceof Number || value instanceof Boolean) {
            out.append(value.toString());
        } else {
            writeJsonString(value.toString(), out);
        }
    }

    private static void writeJsonString(String s, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '"' -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        String hex = Integer.toHexString(c);
                        out.append("\\u");
                        for (int j = hex.length(); j < 4; j++) {
                            out.append('0');
                        }
                        out.append(hex);
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }

    private static void indent(StringBuilder out, int indent) {
        for (int i = 0; i < indent; i++) {
            out.append("    ");
        }
    }
}
