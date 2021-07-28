<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" />

    <!-- Merge the compendiums together -->
    <xsl:template match="collection">

        <!-- Store compendium in an intermediate format (firstStage) -->
        <xsl:variable name="merged">
            <xsl:apply-templates mode="firstStage" select="document(doc/@href)" />
        </xsl:variable>

        <!-- Second stage works from the output of the first stage -->
        <xsl:variable name="filtered">
            <xsl:call-template name="items-extendable">
                <xsl:with-param name="items" select="($merged)/compendium/item" />
            </xsl:call-template>
            <xsl:call-template name="race-unique-filter">
                <xsl:with-param name="races" select="($merged)/compendium/race" />
            </xsl:call-template>
            <xsl:call-template name="class-extendable">
                <xsl:with-param name="classes" select="($merged)/compendium/class" />
            </xsl:call-template>
            <xsl:apply-templates mode="secondStage" select="($merged)/compendium/feat"/>
            <xsl:apply-templates mode="secondStage" select="($merged)/compendium/background"/>
            <xsl:call-template name="spells-extendable">
                <xsl:with-param name="spells" select="($merged)/compendium/spell" />
            </xsl:call-template>
            <xsl:call-template name="monster-unique-filter">
                <xsl:with-param name="monsters" select="($merged)/compendium/monster" />
            </xsl:call-template>
        </xsl:variable>

        <compendium version="5" auto_indent="NO">
            <xsl:apply-templates mode="finalStage" select="$filtered" />
        </compendium>
    </xsl:template>

    <!-- First Stage -->

    <xsl:template mode="firstStage" match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates mode="firstStage" select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Convert spell class comma-separated list to <c></c> elements -->

    <xsl:template mode="firstStage" match="compendium/spell/classes">
        <classes>
            <xsl:for-each select="tokenize(replace(., ', ', ','), ',')">
                <spellClass>
                    <xsl:value-of select="." />
                </spellClass>
            </xsl:for-each>
        </classes>
    </xsl:template>

    <!-- Add sorting keys to dice rolls -->
    <xsl:template mode="firstStage" match="roll|dmg1|dmg2">
        <xsl:variable name="extra" select="tokenize(., '[+-]')" />
        <xsl:variable name="bits" select="tokenize($extra[1], 'd')" />
        <xsl:copy>
            <xsl:copy-of select="@*" />
            <xsl:attribute name="num"><xsl:value-of select="$bits[1]" /></xsl:attribute>
            <xsl:attribute name="die"><xsl:value-of select="$bits[2]" /></xsl:attribute>
            <xsl:copy-of select="node()" />
        </xsl:copy>
    </xsl:template>

    <!-- Second Stage -->

    <xsl:template mode="secondStage" match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates mode="secondStage" select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template mode="secondStage" match="background">
        <background>
            <xsl:apply-templates mode="secondStage" select="name" />
            <xsl:apply-templates mode="secondStage" select="proficiency" />
            <xsl:apply-templates mode="secondStage" select="trait" />
        </background>
    </xsl:template>

    <!-- Merge and Classes (second stage)  -->

    <xsl:template name="class-extendable">
        <xsl:param name="classes" />
        <xsl:for-each select="$classes">
            <xsl:sort select="." order="ascending"/>
            <xsl:choose>
                <!-- Check if there's a duplicate -->
                <xsl:when test="count($classes[name = current()/name]) &gt; 1">
                    <!-- Use the original class that includes the "hd" element -->
                    <!-- Important: Subclasses should only contain "name" and "autolevel" elements -->
                    <xsl:if test="hd">
                        <xsl:call-template name="single-class">
                            <xsl:with-param name="classes" select="$classes" />
                            <xsl:with-param name="class" select="." />
                        </xsl:call-template>
                    </xsl:if>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="single-class">
                        <xsl:with-param name="classes" select="$classes" />
                        <xsl:with-param name="class" select="." />
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="single-class">
        <xsl:param name="class" />
        <xsl:param name="classes" />
        <class>
            <xsl:apply-templates mode="secondStage" select="$class/name" />
            <xsl:apply-templates mode="secondStage" select="$class/hd" />
            <xsl:apply-templates mode="secondStage" select="$class/proficiency" />
            <xsl:apply-templates mode="secondStage" select="$class/spellAbility" />
            <xsl:apply-templates mode="secondStage" select="$class/numSkills" />
            <xsl:apply-templates mode="secondStage" select="$class/armor" />
            <xsl:apply-templates mode="secondStage" select="$class/weapons" />
            <xsl:apply-templates mode="secondStage" select="$class/tools" />
            <xsl:apply-templates mode="secondStage" select="$class/wealth" />
            <!-- Important: Subclasses should only contain "name" and "autolevel" elements -->
            <xsl:for-each select="$classes[name = current()/name]">
                <xsl:apply-templates mode="secondStage" select="autolevel"/>
            </xsl:for-each>
        </class>
    </xsl:template>

    <xsl:template mode="secondStage" match="feat">
        <feat>
            <xsl:apply-templates mode="secondStage" select="name" />
            <xsl:apply-templates mode="secondStage" select="prerequisite" />
            <xsl:apply-templates mode="secondStage" select="text" />
            <xsl:apply-templates mode="secondStage" select="proficiency" />
            <xsl:apply-templates mode="secondStage" select="modifier" />
        </feat>
    </xsl:template>

    <!-- Merge / extend items -->

    <xsl:template name="items-extendable">
        <xsl:param name="items" />
        <xsl:for-each select="$items">
            <xsl:sort select="." order="ascending"/>
            <xsl:choose>
                <!-- Check if there's a duplicate -->
                <xsl:when test="count($items[name = current()/name]) &gt; 1">
                    <!-- Use the original class that includes the "type" element -->
                    <!-- Important: Duplicate items should only specify additional attributes like rolls -->
                    <xsl:if test="type">
                        <xsl:call-template name="single-item">
                            <xsl:with-param name="items" select="$items" />
                            <xsl:with-param name="item" select="." />
                        </xsl:call-template>
                    </xsl:if>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="single-item">
                        <xsl:with-param name="items" select="$items" />
                        <xsl:with-param name="item" select="." />
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="single-item">
        <xsl:param name="item" />
        <xsl:param name="items" />
        <xsl:variable name="modifier_list">
            <xsl:for-each-group select="$items[name = $item/name]/modifier" group-by=".">
                <xsl:sort select="." order="ascending"/>
                <xsl:copy-of select='.'/>
            </xsl:for-each-group>
        </xsl:variable>
        <xsl:variable name="item_roll_list">
            <xsl:for-each-group select="$items[name = $item/name]/roll" group-by=".">
                <xsl:sort select="@num" order="ascending" data-type="number"/>
                <xsl:sort select="@die" order="ascending" data-type="number"/>
                <xsl:copy-of select='.'/>
            </xsl:for-each-group>
        </xsl:variable>
        <item>
            <xsl:apply-templates mode="secondStage" select="$item/name" />
            <xsl:apply-templates mode="secondStage" select="$item/type" />
            <xsl:apply-templates mode="secondStage" select="$item/magic" />
            <xsl:apply-templates mode="secondStage" select="$item/detail" />
            <xsl:apply-templates mode="secondStage" select="$item/weight" />
            <xsl:apply-templates mode="secondStage" select="$item/text" />
            <xsl:apply-templates mode="secondStage" select="$item_roll_list" />
            <xsl:apply-templates mode="secondStage" select="$item/value" />
            <xsl:apply-templates mode="secondStage" select="$modifier_list" />
            <xsl:apply-templates mode="secondStage" select="$item/ac" />
            <xsl:apply-templates mode="secondStage" select="$item/strength" />
            <xsl:apply-templates mode="secondStage" select="$item/stealth" />
            <xsl:apply-templates mode="secondStage" select="$item/dmg1" />
            <xsl:apply-templates mode="secondStage" select="$item/dmg2" />
            <xsl:apply-templates mode="secondStage" select="$item/dmgType" />
            <xsl:apply-templates mode="secondStage" select="$item/property" />
            <xsl:apply-templates mode="secondStage" select="$item/range" />
        </item>
    </xsl:template>

    <!-- Disambiguate monsters (second stage) -->

    <xsl:template name="monster-unique-filter">
        <xsl:param name="monsters" />
        <xsl:for-each-group select="$monsters" group-by="./name">
            <xsl:sort select="." order="ascending"/>
            <xsl:call-template name="single-monster">
                <xsl:with-param name="monster" select="." />
            </xsl:call-template>
        </xsl:for-each-group>
    </xsl:template>

    <xsl:template name="single-monster">
        <xsl:param name="monster" />
        <monster>
            <xsl:apply-templates mode="secondStage" select="$monster/name" />
            <xsl:apply-templates mode="secondStage" select="$monster/size" />
            <xsl:apply-templates mode="secondStage" select="$monster/type" />
            <xsl:apply-templates mode="secondStage" select="$monster/alignment" />
            <xsl:apply-templates mode="secondStage" select="$monster/ac" />
            <xsl:apply-templates mode="secondStage" select="$monster/hp" />
            <xsl:apply-templates mode="secondStage" select="$monster/speed" />
            <xsl:apply-templates mode="secondStage" select="$monster/str" />
            <xsl:apply-templates mode="secondStage" select="$monster/dex" />
            <xsl:apply-templates mode="secondStage" select="$monster/con" />
            <xsl:apply-templates mode="secondStage" select="$monster/int" />
            <xsl:apply-templates mode="secondStage" select="$monster/wis" />
            <xsl:apply-templates mode="secondStage" select="$monster/cha" />
            <xsl:apply-templates mode="secondStage" select="$monster/save" />
            <xsl:apply-templates mode="secondStage" select="$monster/skill" />
            <xsl:apply-templates mode="secondStage" select="$monster/resist" />
            <xsl:apply-templates mode="secondStage" select="$monster/vulnerable" />
            <xsl:apply-templates mode="secondStage" select="$monster/immune" />
            <xsl:apply-templates mode="secondStage" select="$monster/conditionImmune" />
            <xsl:apply-templates mode="secondStage" select="$monster/senses" />
            <xsl:apply-templates mode="secondStage" select="$monster/passive" />
            <xsl:apply-templates mode="secondStage" select="$monster/languages" />
            <xsl:apply-templates mode="secondStage" select="$monster/cr" />
            <xsl:apply-templates mode="secondStage" select="$monster/trait" />
            <xsl:apply-templates mode="secondStage" select="$monster/action" />
            <xsl:apply-templates mode="secondStage" select="$monster/legendary" />
            <xsl:apply-templates mode="secondStage" select="$monster/reaction" />
            <xsl:apply-templates mode="secondStage" select="$monster/spells" />
            <xsl:apply-templates mode="secondStage" select="$monster/slots" />
            <xsl:apply-templates mode="secondStage" select="$monster/description" />
            <xsl:apply-templates mode="secondStage" select="$monster/environment" />
        </monster>
    </xsl:template>

    <!-- Disambiguate races (second stage) -->

    <xsl:template name="race-unique-filter">
        <xsl:param name="races" />
        <xsl:for-each-group select="$races" group-by="./name">
            <xsl:call-template name="single-race">
                <xsl:with-param name="race" select="." />
            </xsl:call-template>
        </xsl:for-each-group>
    </xsl:template>

    <xsl:template name="single-race">
        <xsl:param name="race" />
        <race>
            <xsl:apply-templates mode="secondStage" select="$race/name" />
            <xsl:apply-templates mode="secondStage" select="$race/size" />
            <xsl:apply-templates mode="secondStage" select="$race/speed" />
            <xsl:apply-templates mode="secondStage" select="$race/ability" />
            <xsl:apply-templates mode="secondStage" select="$race/proficiency" />
            <xsl:apply-templates mode="secondStage" select="$race/spellAbility" />
            <xsl:apply-templates mode="secondStage" select="$race/trait" />
            <xsl:apply-templates mode="secondStage" select="$race/modifier" />
        </race>
    </xsl:template>

    <!-- Merge and extend spells (second stage) -->

    <xsl:template name="spells-extendable">
        <xsl:param name="spells" />
        <xsl:for-each select="$spells">
            <xsl:sort select="." order="ascending"/>
            <xsl:choose>
                <!-- Check if there's a duplicate -->
                <xsl:when test="count($spells[name = current()/name]) &gt; 1">
                    <!-- Use the original spell that includes the "level" element -->
                    <!-- Important: Duplicate spells should only contain "name", and
                         "classes" and/or "roll" elements -->
                    <xsl:if test="level">
                        <xsl:call-template name="single-spell">
                            <xsl:with-param name="spells" select="$spells" />
                            <xsl:with-param name="spell" select="." />
                        </xsl:call-template>
                    </xsl:if>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="single-spell">
                        <xsl:with-param name="spells" select="$spells" />
                        <xsl:with-param name="spell" select="." />
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="single-spell">
        <xsl:param name="spell" />
        <xsl:param name="spells" />

        <xsl:variable name="spell_roll_list">
            <xsl:for-each-group select="$spells[name = $spell/name]/roll" group-by=".">
                <xsl:sort select="@num" order="ascending" data-type="number"/>
                <xsl:sort select="@die" order="ascending" data-type="number"/>
                <xsl:copy-of select='.'/>
            </xsl:for-each-group>
        </xsl:variable>
        <xsl:variable name="sorted_class_list">
            <xsl:for-each-group select="$spells[name = $spell/name]/classes/spellClass" group-by=".">
                <xsl:sort select="." order="ascending"/>
                <xsl:copy-of select='.'/>
            </xsl:for-each-group>
        </xsl:variable>

        <spell>
            <xsl:apply-templates mode="secondStage" select="$spell/name" />
            <xsl:apply-templates mode="secondStage" select="$spell/level" />
            <xsl:apply-templates mode="secondStage" select="$spell/school" />
            <xsl:apply-templates mode="secondStage" select="$spell/ritual" />
            <xsl:apply-templates mode="secondStage" select="$spell/time" />
            <xsl:apply-templates mode="secondStage" select="$spell/range" />
            <xsl:apply-templates mode="secondStage" select="$spell/components" />
            <xsl:apply-templates mode="secondStage" select="$spell/duration" />
            <classes>
                <xsl:apply-templates mode="secondStage" select="$sorted_class_list"/>
            </classes>
            <xsl:apply-templates mode="secondStage" select="$spell/source" />
            <xsl:apply-templates mode="secondStage" select="$spell/text" />
            <xsl:apply-templates mode="secondStage" select="$spell_roll_list" />
        </spell>
    </xsl:template>

    <!-- Final Stage -->

    <!--empty template suppresses this attribute-->
    <xsl:template mode="finalStage" match="@die" />
    <xsl:template mode="finalStage" match="@num" />

    <!--Convert spellClass elements into comma separated string-->
    <xsl:template mode="finalStage" match="spellClass">
        <xsl:if test="position() > 1">, </xsl:if>
        <xsl:value-of select="." />
    </xsl:template>

    <xsl:template mode="finalStage" match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates mode="finalStage" select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:transform>
