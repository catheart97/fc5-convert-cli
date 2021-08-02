package dev.ebullient.fc5.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.ebullient.fc5.Log;
import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for monsterType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="monsterType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="size" type="{}sizeEnum"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="alignment" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ac" type="{}acType"/>
 *         &lt;element name="hp" type="{}hpType"/>
 *         &lt;element name="speed" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="str" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="dex" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="con" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="int" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="wis" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="cha" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="save" type="{}abilityBonusList"/>
 *         &lt;element name="skill" type="{}skillBonusList"/>
 *         &lt;element name="resist" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="vulnerable" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="immune" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="conditionImmune" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="senses" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="passive" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="languages" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cr" type="{}crType"/>
 *         &lt;element name="trait" type="{}traitType"/>
 *         &lt;element name="action" type="{}traitType"/>
 *         &lt;element name="legendary" type="{}traitType"/>
 *         &lt;element name="reaction" type="{}traitType"/>
 *         &lt;element name="spells" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="slots" type="{}slotsType"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="environment" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@TemplateData
public class MonsterType implements BaseType {
    static final Pattern TYPE_DETAIL = Pattern.compile("(.+?) \\((.+?)\\)");

    class AbilityScores {
        int strength;
        int dexterity;
        int constitution;
        int intelligence;
        int wisdom;
        int charisma;

        private String toAbilityModifier(int value) {
            int mod = (value - 10);
            if (mod % 2 != 0) {
                mod -= 1; // round down
            }
            int modifier = mod / 2;
            return String.format("%s (%s%s)", value,
                    modifier >= 0 ? "+" : "",
                    modifier);
        }

        @Override
        public String toString() {
            return toAbilityModifier(strength)
                    + "|" + toAbilityModifier(dexterity)
                    + "|" + toAbilityModifier(constitution)
                    + "|" + toAbilityModifier(intelligence)
                    + "|" + toAbilityModifier(wisdom)
                    + "|" + toAbilityModifier(charisma);
        }
    }

    final String name;
    final AbilityScores scores = new AbilityScores();
    final SizeEnum size;
    final String type;
    final String alignment;
    final String ac;
    final String hp;
    final String speed;
    final String save;
    final List<String> skill;
    final String resist;
    final String vulnerable;
    final String immune;
    final String conditionImmune;
    final String senses;
    final int passive;
    final String languages;
    final String cr;
    final List<Trait> trait;
    final List<Trait> action;
    final List<Trait> legendary;
    final List<Trait> reaction;
    final String spells;
    final SpellSlots slots;
    final Text description;
    final String environment;

    public MonsterType(ParsingContext context) {
        name = context.getOrFail(context.owner, "name", String.class);

        try {
            size = context.getOrDefault("size", SizeEnum.UNKNOWN);
            type = context.getOrDefault("type", "");
            alignment = context.getOrDefault("alignment", "");
            ac = context.getOrDefault("ac", "");
            hp = context.getOrDefault("hp", "");
            speed = context.getOrDefault("speed", "");

            scores.strength = context.getOrDefault("str", 10);
            scores.dexterity = context.getOrDefault("dex", 10);
            scores.constitution = context.getOrDefault("con", 10);
            scores.intelligence = context.getOrDefault("int", 10);
            scores.wisdom = context.getOrDefault("wis", 10);
            scores.charisma = context.getOrDefault("cha", 10);

            save = context.getOrDefault("save", "");
            skill = context.getOrDefault("skill", Collections.emptyList());
            resist = context.getOrDefault("resist", "");
            vulnerable = context.getOrDefault("vulnerable", "");
            immune = context.getOrDefault("immune", "");
            conditionImmune = context.getOrDefault("conditionImmune", "");
            senses = context.getOrDefault("senses", "");
            passive = context.getOrDefault("passive", 10);
            languages = context.getOrDefault("languages", "--");
            cr = context.getOrDefault("cr", "0");
            trait = context.getOrDefault("trait", Collections.emptyList());
            action = context.getOrDefault("action", Collections.emptyList());
            legendary = context.getOrDefault("legendary", Collections.emptyList());
            reaction = context.getOrDefault("reaction", Collections.emptyList());

            spells = context.getOrDefault("spells", "");
            slots = context.getOrDefault("slots", SpellSlots.NONE);
            environment = context.getOrDefault("environment", "");

            description = new Text(Collections.singletonList(context.getOrDefault("description", "")));
        } catch (ClassCastException ex) {
            Log.err().println("Error parsing monster " + name);
            throw ex;
        }
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slugify(name);
    }

    public List<String> getTags() {
        List<String> result = new ArrayList<>();
        result.add("monster/" + slugify(size.prettyName()));

        Matcher m = TYPE_DETAIL.matcher(type);
        if (m.matches()) {
            for (String detail : m.group(2).split("\\s*,\\s*")) {
                result.add("monster/" + slugify(m.group(1)) + "/" + slugify(detail));
            }
        } else {
            result.add("monster/" + slugify(type));
        }
        return result;
    }

    public String getScores() {
        return scores.toString();
    }

    public String getSize() {
        return size.prettyName();
    }

    public String getType() {
        return type;
    }

    public String getAlignment() {
        return alignment;
    }

    public String getAc() {
        return ac;
    }

    public String getHp() {
        return hp;
    }

    public String getSpeed() {
        return speed;
    }

    public String getSave() {
        return save;
    }

    public List<String> getSkill() {
        return skill;
    }

    public String getSkillString() {
        return String.join(", ", skill);
    }

    public String getResist() {
        return resist;
    }

    public String getVulnerable() {
        return vulnerable;
    }

    public String getImmune() {
        return immune;
    }

    public String getConditionImmune() {
        return conditionImmune;
    }

    public String getSenses() {
        return senses;
    }

    public int getPassive() {
        return passive;
    }

    public String getLanguages() {
        return languages;
    }

    public String getCr() {
        return cr;
    }

    public List<Trait> getTrait() {
        return trait;
    }

    public List<Trait> getAction() {
        return action;
    }

    public List<Trait> getLegendary() {
        return legendary;
    }

    public List<Trait> getReaction() {
        return reaction;
    }

    public String getSpells() {
        return spells;
    }

    public SpellSlots getSlots() {
        return slots;
    }

    public String getDescription() {
        return String.join("\n", description.content);
    }

    public String getEnvironment() {
        return environment;
    }

    String slugify(String text) {
        return MarkdownWriter.slugifier().slugify(text);
    }

    @Override
    public String toString() {
        return "MonsterType [name=" + name + "]";
    }
}
