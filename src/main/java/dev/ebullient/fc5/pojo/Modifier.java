package dev.ebullient.fc5.pojo;

/**
 * <p>
 * Java class for modifierType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="modifierType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;>modifierValue">
 *       &lt;attribute name="category" type="{}categoryEnum" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 */
public class Modifier {
    public static final Modifier NONE = new Modifier();

    final String value;
    final ModifierCategoryEnum category;

    private Modifier() {
        value = "";
        category = ModifierCategoryEnum.UNKNOWN;
    }

    // <xs:pattern value="([Pp]assive )?([Ss]trength( Score)?|[Dd]exterity( Score)?|[Cc]onstitution( Score)?|[Ii]ntelligence( Score)?|[Ww]isdom( Score)?|[Cc]harisma( Score)?|[Hh][Pp]) ?[+-]? ?([0-9]*|%0|prof)"/>
    // <xs:pattern value="([Mm]elee [Dd]amage|[Mm]elee [Aa]ttacks|[Ww]eapon [Dd]amage|[Ww]eapon [Aa]ttacks|[Rr]anged [Dd]amage|[Rr]anged [Aa]ttacks|[Ss]pell [Aa]ttack|[Ss]pell [Dd][Cc]|[Ss]aving [Tt]hrows|[Aa][Cc]|[Aa]rmor [Cc]lass|[Ss]peed|[Pp]roficiency [Bb]onus|[Ii]nitiative) ?[+-]? ?([0-9]*|%0|prof)"/>
    // <xs:pattern value="([Aa]thletics|[Aa]crobatics|[Aa]nimal [Hh]andling|[Aa]rcana|[Dd]eception|[Hh]istory|[Ii]nsight|[Ii]ntimidation|[Ii]nvestigation|[Mm]edicine|[Nn]ature|[Pp]erception|[Pp]erformance|[Pp]ersuasion|[Rr]eligion|[Ss]leight [Oo]f [Hh]and|[Ss]tealth|[Ss]urvival) ?[+-]? ?([0-9]*|%0|prof)"/>

    public Modifier(String value, ModifierCategoryEnum category) {
        if (category == ModifierCategoryEnum.UNKNOWN) {
            throw new IllegalArgumentException("Modifier " + value + " is missing required category");
        }
        this.value = Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase().replace(" score", "");
        this.category = category;
    }

    public String getCategory() {
        return category.value();
    }

    public String getValue() {
        return value.replace("Ac", "AC");
    }
}
