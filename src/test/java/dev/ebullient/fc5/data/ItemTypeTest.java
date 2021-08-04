package dev.ebullient.fc5.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ItemTypeTest extends ParsingTestBase {

    @Test
    public void testJugItem() throws Exception {
        CompendiumType compendium = doParseInputResource("itemJug.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.items.isEmpty(),
                "Items should not be empty, found " + compendium);

        ItemType item = compendium.items.get(0);
        Assertions.assertAll(
                () -> assertEquals("Jug", item.name),
                () -> assertEquals(ItemEnum.GEAR, item.type),
                () -> assertEquals("Adventuring gear", item.detail),
                () -> assertEquals(4d, item.weight),
                () -> assertEquals(0.02, item.cost),
                () -> assertTrue(textContains(item.text, "A jug holds")));

        String content = templates.renderItem(item);
        Assertions.assertAll(
                () -> assertContains(content, "# Jug"),
                () -> assertContains(content, "Adventuring gear"),
                () -> assertContains(content, "item/gear"),
                () -> assertContains(content, "aliases: ['Jug']"));

    }

    @Test
    public void testLanceItem() throws Exception {
        CompendiumType compendium = doParseInputResource("itemLance.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.items.isEmpty(),
                "Items should not be empty, found " + compendium);

        ItemType item = compendium.items.get(0);
        Assertions.assertAll(
                () -> assertEquals("Lance", item.name),
                () -> assertEquals(ItemEnum.MELEE_WEAPON, item.type),
                () -> assertEquals(true, item.magicItem.isMagic),
                () -> assertEquals("Weapon (Martial melee)", item.detail),
                () -> assertEquals(6d, item.weight),
                () -> assertEquals(10.0, item.cost),
                () -> assertEquals("1d12", item.dmg1.textContent),
                () -> assertEquals(Roll.NONE, item.dmg2),
                () -> assertEquals(DamageEnum.PIERCING, item.dmgType),
                () -> assertContainsProperties(item.properties, "R,S,M"));

        String content = templates.renderItem(item);
        Assertions.assertAll(
                () -> assertContains(content, "# Lance"),
                () -> assertContains(content, "Weapon (Martial melee)"),
                () -> assertContains(content, "item/weapon/martial/melee"),
                () -> assertContains(content, "Special: You have disadvantage"),
                () -> assertContains(content, "aliases: ['Lance']"));

    }

    @Test
    public void testMoreItems() throws Exception {
        CompendiumType compendium = doParseInputResource("items.xml");

        Assertions.assertNotNull(compendium);
        Assertions.assertFalse(compendium.items.isEmpty(),
                "Items should not be empty, found " + compendium);

        boolean carpet = false;
        boolean crossbow = false;
        boolean longsword = false;
        boolean poison = false;
        boolean scimitar = false;
        boolean spikedarmor = false;
        for (ItemType item : compendium.items) {
            String content = templates.renderItem(item);
            if ("Light Crossbow".equals(item.name)) {
                crossbow = true;
                validateCrossbow(item, content);
            } else if ("Longsword of Life Stealing".equals(item.name)) {
                longsword = true;
                validateLongsword(item, content);
            } else if ("+3 Spiked Armor".equals(item.name)) {
                spikedarmor = true;
                assertContains(content, "item/armor/medium");
                assertContains(content, "item/major");
                assertContains(content, "*Armor (medium), major, legendary*");
                assertContains(content, "**Base Armor Class**: 14 + DEX (max of +2)");
                assertContains(content, "- **Bonus**: AC +3");
            } else if ("Double-Bladed Scimitar of Vengeance".equals(item.name)) {
                scimitar = true;
                assertContains(content, "item/weapon/martial/melee");
                assertContains(content, "item/major/uncommon");
                assertContains(content, "*Weapon (Martial melee), major, uncommon, Cursed item*");
            } else if ("Carpet of Flying, 6 ft. × 9 ft.".equals(item.name)) {
                carpet = true;
                assertContains(content, "item/wondrous");
                assertContains(content, "item/major");
                assertContains(content, "*Wondrous item, major*");
            } else if ("Carrion Crawler Mucus".equals(item.name)) {
                poison = true;
                assertContains(content, "item/gear/poison");
                assertContains(content, "item/major");
                assertContains(content, "*Adventuring gear, major, Poison*");
            }
        }

        assertTrue(carpet, "Should have found a carpet");
        assertTrue(crossbow, "Should have found a crossbow");
        assertTrue(longsword, "Should have found a longsword");
        assertTrue(poison, "Should have found a poison");
        assertTrue(scimitar, "Should have found a scimitar");
        assertTrue(spikedarmor, "Should have found a spikedarmor");
    }

    private void validateLongsword(ItemType longsword, String content) {
        Assertions.assertAll(
                () -> assertEquals("Longsword of Life Stealing", longsword.name),
                () -> assertEquals(ItemEnum.MELEE_WEAPON, longsword.type),
                () -> assertEquals("Weapon (Martial melee), major, rare", longsword.detail),
                () -> assertEquals(3d, longsword.weight),
                () -> assertTrue(textContains(longsword.text, "Source:")),
                () -> assertTrue(rollContains(longsword.roll, "3d6")),
                () -> assertEquals("1d8", longsword.dmg1.textContent),
                () -> assertEquals("1d10", longsword.dmg2.textContent),
                () -> assertEquals(DamageEnum.SLASHING, longsword.dmgType),
                () -> assertContainsProperties(longsword.properties, "V,M"));

        Assertions.assertAll(
                () -> assertContains(content, "# Longsword of Life Stealing"),
                () -> assertContains(content, "Weapon (Martial melee), major, rare"),
                () -> assertContains(content, "item/weapon/martial/melee"),
                () -> assertContains(content, "aliases: ['Longsword of Life Stealing']"));
    }

    private void validateCrossbow(ItemType crossbow, String content) {
        Assertions.assertAll(
                () -> assertEquals("Light Crossbow", crossbow.name),
                () -> assertEquals(ItemEnum.RANGED_WEAPON, crossbow.type),
                () -> assertEquals("Weapon (Simple ranged)", crossbow.detail),
                () -> assertEquals(5d, crossbow.weight),
                () -> assertTrue(textContains(crossbow.text, "Source:")),
                () -> assertEquals(25.0, crossbow.cost),
                () -> assertEquals("1d8", crossbow.dmg1.textContent),
                () -> assertEquals(Roll.NONE, crossbow.dmg2),
                () -> assertEquals(DamageEnum.PIERCING, crossbow.dmgType),
                () -> assertContainsProperties(crossbow.properties, "A,LD,2H"),
                () -> assertEquals("80/320", crossbow.range));

        Assertions.assertAll(
                () -> assertContains(content, "# Light Crossbow"),
                () -> assertContains(content, "Weapon (Simple ranged)"),
                () -> assertContains(content, "item/weapon/simple/ranged"),
                () -> assertContains(content, "aliases: ['Light Crossbow']"));
    }

    void assertContainsProperties(List<PropertyEnum> properties, String origXml) {
        for (String key : origXml.split(",")) {
            assertTrue(properties.stream().anyMatch(x -> key.equals(x.getXmlValue())),
                    "Expected to find " + key + " in list " + properties);
        }
    }
}
