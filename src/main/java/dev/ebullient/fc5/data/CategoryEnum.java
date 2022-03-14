package dev.ebullient.fc5.data;

import io.quarkus.qute.TemplateData;

/**
 * <p>
 * Java class for categoryEnum.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * <p>
 *
 * <pre>
 * &lt;simpleType name="categoryEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="bonus"/>
 *     &lt;enumeration value="ability score"/>
 *     &lt;enumeration value="ability modifier"/>
 *     &lt;enumeration value="saving throw"/>
 *     &lt;enumeration value="skills"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 *
 */
@TemplateData
public enum CategoryEnum implements ConvertedEnumType {

    BONUS("Bonus"),
    ABILITY_SCORE("Ability Score"),
    ABILITY_MODIFIER("Ability Modifier"),
    SAVING_THROW("Saving Throw"),
    SKILLS("Skills"),
    UNKNOWN("Unknown");

    private final String longName;

    CategoryEnum(String v) {
        longName = v;
    }

    public String getXmlValue() {
        return longName;
    }

    public String value() {
        return longName;
    }

    public static CategoryEnum fromValue(String v) {
        if (v == null || v.isEmpty()) {
            return UNKNOWN;
        }
        switch (v.toLowerCase()) {
            case "bonus":
                return BONUS;
            case "ability score":
                return ABILITY_SCORE;
            case "ability modifier":
                return ABILITY_MODIFIER;
            case "saving throw":
                return SAVING_THROW;
            case "skills":
                return SKILLS;
        }
        throw new IllegalArgumentException("Invalid/Unknown category " + v);
    }
}
