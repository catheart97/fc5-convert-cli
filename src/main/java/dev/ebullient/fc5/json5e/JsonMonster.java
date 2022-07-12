package dev.ebullient.fc5.json5e;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import dev.ebullient.fc5.Log;
import dev.ebullient.fc5.pojo.QuteTrait;

public interface JsonMonster extends JsonBase {
    default String decorateMonsterName(JsonNode jsonSource) {
        String revised = getSources().getName().replace("\"", "");
        if (booleanOrDefault(jsonSource, "isNpc", false)) {
            return revised + " (NPC)";
        }
        return revised;
    }

    default String monsterDescription(JsonNode jsonSource) {
        List<String> text = monsterDescriptionList(jsonSource);
        return String.join("\n", text);
    }

    default List<String> monsterDescriptionList(JsonNode jsonSource) {
        List<String> text = new ArrayList<>();
        try {
            getFluffDescription(jsonSource, JsonIndex.IndexType.monsterfluff, text);
        } catch (Exception e) {
            Log.errorf(e, "Unable to parse description for %s", getSources());
        }
        maybeAddBlankLine(text);
        text.add("Source: " + getSources().getSourceText());
        return text;
    }

    default String monsterType(JsonNode jsonSource) {
        JsonNode type = jsonSource.get("type");
        if (type == null) {
            System.out.println("Empty type for " + getSources());
        }
        if (type.isTextual()) {
            return type.asText();
        }

        StringBuilder result = new StringBuilder();
        result.append(type.get("type").asText());
        List<String> tags = new ArrayList<>();
        type.withArray("tags").forEach(tag -> {
            if (tag.isTextual()) {
                tags.add(tag.asText());
            } else {
                tags.add(String.format("%s %s",
                        tag.get("prefix").asText(),
                        tag.get("tag").asText()));
            }
        });
        if (!tags.isEmpty()) {
            result.append(" (")
                    .append(String.join(", ", tags))
                    .append(")");
        }
        return result.toString();
    }

    default String monsterAlignment(JsonNode jsonSource) {
        ArrayNode a1 = jsonSource.withArray("alignment");
        if (a1.size() == 0) {
            return "Unaligned";
        }
        if (a1.size() == 1 && a1.get(0).has("special")) {
            return a1.get(0).get("special").asText();
        }

        String choices = a1.toString();
        if (choices.contains("note")) {
            List<String> notes = new ArrayList<>(List.of(choices.split("\\},\\{")));
            for (int i = 0; i < notes.size(); i++) {
                int pos = notes.get(i).indexOf("note");
                String alignment = mapAlignmentToString(toAlignmentCharacters(notes.get(i).substring(0, pos)));
                String note = notes.get(i).substring(pos + 4).replaceAll("[^A-Za-z ]+", "");
                notes.set(i, String.format("%s (%s)", alignment, note));
            }
            return String.join(", ", notes);
        } else {
            choices = toAlignmentCharacters(choices);
            return mapAlignmentToString(choices);
        }
    }

    default String toAlignmentCharacters(String src) {
        return src.replaceAll("\"[A-Z]*[a-z ]+\"", "") // remove notes
                .replaceAll("[^LCNEGAUXY]", ""); // keep only alignment characters
    }

    default String mapAlignmentToString(String a) {
        switch (a) {
            case "A":
                return "Any alignment";
            case "C":
                return "Chaotic";
            case "CE":
                return "Chaotic Evil";
            case "CELENE":
            case "LNXCE":
                return "Any Evil Alignment";
            case "CG":
                return "Chaotic Good";
            case "CGNE":
                return "Chaotic Good or Neutral Evil";
            case "CGNYE":
                return "Any Chaotic alignment";
            case "CN":
                return "Chaotic Neutral";
            case "N":
            case "NX":
            case "NY":
                return "Neutral";
            case "NE":
                return "Neutral Evil";
            case "NG":
                return "Neutral Good";
            case "NGNE":
            case "NENG":
                return "Neutral Good or Neutral Evil";
            case "NNXNYN":
            case "NXCGNYE":
                return "Any Non-Lawful alignment";
            case "L":
                return "Lawful";
            case "LE":
                return "Lawful Evil";
            case "LG":
                return "Lawful Good";
            case "LN":
                return "Lawful Neutral";
            case "LNXCNYE":
                return "Any Non-Good alignment";
            case "E":
                return "Any Evil alignment";
            case "G":
                return "Any Good alignment";
            case "U":
                return "Unaligned";
        }
        Log.errorf("What alignment is this? %s (from %s)", a, getSources());
        return "Unknown";
    }

    default String monsterAc(JsonNode jsonSource) {
        StringBuilder result = new StringBuilder();
        List<String> details = new ArrayList<>();
        jsonSource.withArray("ac").forEach(ac -> {
            if (result.length() == 0) {
                if (ac.isNumber()) {
                    result.append(ac.asText());
                } else if (ac.has("special")) {
                    result.append(ac.get("special").asText());
                } else {
                    result.append(ac.get("ac").asText());
                    ac.withArray("from").forEach(f -> details.add(f.asText()));
                }
            } else {
                if (ac.isNumber()) {
                    details.add(ac.asText());
                } else {
                    StringBuilder value = new StringBuilder();
                    value.append(ac.get("ac").asText()).append(" from ");
                    ac.withArray("from").forEach(f -> value.append(f.asText()));
                    details.add(value.toString());
                }
            }
        });
        if (!details.isEmpty()) {
            result.append(" (")
                    .append(String.join(", ", details))
                    .append(")");
        }
        return replaceAttributes(result.toString());
    }

    default String monsterSpeed(JsonNode jsonSource) {
        List<String> speed = new ArrayList<>();
        jsonSource.get("speed").fields().forEachRemaining(f -> {
            if (f.getValue().isNumber()) {
                speed.add(String.format("%s %s ft.", f.getKey(), f.getValue().asText()));
            } else if (f.getValue().has("number")) {
                speed.add(String.format("%s %s ft.%s",
                        f.getKey(),
                        f.getValue().get("number").asText(),
                        f.getValue().has("condition")
                                ? " " + f.getValue().get("condition").asText()
                                : ""));
            }
        });
        return replaceAttributes(String.join(", ", speed));
    }

    default String monsterHp(JsonNode jsonSource) {
        JsonNode health = jsonSource.get("hp");
        if (health.has("special")) {
            JsonNode special = health.get("special");
            if (special.isNumber()) {
                return special.asText();
            } else {
                return special.asText().replaceAll("^(\\d+) .*", "$1");
            }
        } else if (health.has("formula")) {
            return String.format("%s (%s)",
                    health.get("average").asText(),
                    health.get("formula").asText());
        }
        Log.errorf("Unknown hp from %s: %s", getSources(), health.toPrettyString());
        throw new IllegalArgumentException("Unknown hp from " + getSources());
    }

    default String monsterImmunities(JsonNode jsonSource) {
        if (jsonSource.has("immune") && !jsonSource.get("immune").isNull()) {
            List<String> immunities = new ArrayList<>();
            StringBuilder separator = new StringBuilder();
            jsonSource.withArray("immune").forEach(immunity -> {
                if (immunity.isTextual()) {
                    immunities.add(immunity.asText());
                } else {
                    StringBuilder str = new StringBuilder();
                    str.append(joinAndReplace(immunity, "immune"));
                    if (immunity.has("note")) {
                        str.append(" ")
                                .append(immunity.get("note").asText());
                    }

                    if (separator.length() == 0) {
                        separator.append(";");
                    }
                    immunities.add(str.toString());
                }
            });
            if (separator.length() == 0) {
                separator.append(",");
            }
            return String.join(separator.toString(), immunities);
        }
        return null;
    }

    default void spellcastingTrait(JsonNode jsonSource,
            Consumer<Collection<String>> spellHandler, Consumer<String[]> spellSlotHandler,
            Consumer<QuteTrait> actionHandler, Consumer<QuteTrait> traitHandler) {
        JsonNode node = jsonSource.get("spellcasting");
        if (node == null || node.isNull()) {
            return;
        } else if (node.isObject()) {
            throw new IllegalArgumentException("Unknown spellcasting: " + getSources());
        }
        JsonNode spellcasting = node.get(0);
        List<String> text = new ArrayList<>();
        Set<String> diceRolls = new HashSet<>();
        Set<String> spells = new TreeSet<>();

        String traitName = getTextOrEmpty(spellcasting, "name");
        appendEntryToText(text, spellcasting.get("headerEntries"), diceRolls);
        if (spellcasting.has("will")) {
            blankBeforeList(text);
            List<String> atWill = getSpells(spellcasting, "will");
            text.add(li() + "At will: " + String.join(", ", atWill));
            spells.addAll(atWill);
        }
        JsonNode daily = spellcasting.get("daily");
        if (daily != null) {
            blankBeforeList(text);
            daily.fieldNames().forEachRemaining(field -> {
                List<String> things = getSpells(daily, field);
                spells.addAll(things);
                switch (field) {
                    case "1":
                    case "2":
                    case "3":
                        text.add(String.format("%s%s/day: %s", li(),
                                field.charAt(0),
                                String.join(", ", things)));
                        break;
                    case "1e":
                    case "2e":
                    case "3e":
                        text.add(String.format("%s%s/day each: %s", li(),
                                field.charAt(0),
                                String.join(", ", things)));
                        break;
                    default:
                        Log.debugf("What is this: %s", spellcasting.toPrettyString());
                }
            });
        }
        JsonNode knownSpells = spellcasting.get("spells");
        if (knownSpells != null) {
            String[] slots = new String[] { "0", "0", "0", "0", "0", "0", "0", "0", "0", "0" };
            knownSpells.fields().forEachRemaining(spellLevelEntry -> {
                JsonNode spellLevel = spellLevelEntry.getValue();
                int level = Integer.parseInt(spellLevelEntry.getKey());
                List<String> things = getSpells(spellLevel, "spells");
                spells.addAll(things);

                switch (level) {
                    case 0:
                        text.add(String.format("%sCantrips: %s", li(),
                                String.join(", ", things)));
                        slots[0] = "" + things.size();
                        break;
                    case 1:
                        text.add(String.format("%s1st-level spells: %s", li(),
                                String.join(", ", things)));

                        slots[1] = getTextOrDefault(spellLevel, "slots", "0");
                        break;
                    case 2:
                        text.add(String.format("%s2nd-level spells: %s", li(),
                                String.join(", ", things)));
                        slots[2] = getTextOrDefault(spellLevel, "slots", "0");
                        break;
                    case 3:
                        text.add(String.format("%s3rd-level spells: %s", li(),
                                String.join(", ", things)));
                        slots[3] = getTextOrDefault(spellLevel, "slots", "0");
                        break;
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                        text.add(String.format("%s%sth-level spells: %s", li(),
                                spellLevelEntry.getKey(),
                                String.join(", ", things)));
                        slots[level] = getTextOrDefault(spellLevel, "slots", "0");
                        break;
                    default:
                        Log.debugf("What is this: %s", spellcasting.toPrettyString());
                }
            });
            spellSlotHandler.accept(slots);
        }
        appendEntryToText(text, spellcasting.get("footerEntries"), diceRolls);
        if (!spells.isEmpty()) {
            spellHandler.accept(spells);
        }

        QuteTrait trait = createTrait(traitName, text, diceRolls);
        if ("action".equals(getTextOrEmpty(spellcasting, "displayAs"))) {
            actionHandler.accept(trait);
        } else {
            traitHandler.accept(trait);
        }
    }

    default List<String> getSpells(JsonNode source, String fieldName) {
        List<String> spells = new ArrayList<>();
        JsonNode spellNode = null;
        if (source.isArray()) {
            spellNode = source;
        } else if (source.isObject() && source.has(fieldName)) {
            spellNode = source.get(fieldName);
            if (spellNode.isObject()) {
                spellNode = spellNode.get("spells");
            }
        }
        if (spellNode != null) {
            spellNode.forEach(s -> {
                String spell = replaceText(s.asText());
                spells.add(spell);
            });
        } else {
            Log.debugf("Asked for spells w/ not exist field: %s", fieldName);
        }
        return spells;
    }
}
