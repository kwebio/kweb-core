package com.github.sanity.kweb.plugins.materialdesignlite.typography

import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.modification.addClasses
import com.github.sanity.kweb.plugins.materialdesignlite.materialDesignLite

/**
 * See https://material.io/guidelines/style/typography.html#typography-styles
 */

fun Element.typography(style: TypographyStyles): Element {
    require(materialDesignLite::class)
    addClasses("mdl-typography--" + style.classSubstring)
    return this
}

enum class TypographyStyles(val classSubstring: String) {
    /** Regular 14px (Device), Regular 13px (Desktop) */
    body1("body-1"),
    /** Regular 14px (Device), Regular 13px (Desktop) */
    body1ForcePreferredFont("body-1-force-preferred-font"),
    /** Medium 14px (Device), Medium 13px (Desktop) */
    body2("body-2"),
    /** Body with color contrast */
    body2ColorContrast("body-2-color-contrast"),
    /** Medium 14px (Device), Medium 13px (Desktop) */
    body2ForcePreferredFont("body-2-force-preferred-font"),
    /** Medium (All Caps) 14px */
    button("button"),
    /** Regular 12px */
    caption("caption"),
    /** Caption with color contrast */
    captionColorContrast("caption-color-contrast"),
    /** Regular 34px */
    display1("display-1"),
    /** Display with color contrast */
    display1ColorContrast("display-1-color-contrast"),
    /** Regular 45px */
    display2("display-2"),
    /** Regular 56px */
    display3("display-3"),
    /** Light 112px */
    display4("display-4"),
    /** Regular 24px */
    headline("headline"),
    /** Medium 14px (Device), Medium 13px (Desktop) */
    menu("menu"),
    /** Regular 16px (Device), Regular 15px (Desktop) */
    subhead("subhead"),
    /** Subhead with color contrast */
    subheadColorContrast("subhead-color-contrast"),
    /** Striped table */
    tableStriped("table-striped"),
    /** Capitalized text */
    textCapitalize("text-capitalize"),
    /** Center aligned text */
    textCenter("text-center"),
    /** Justified text */
    textJustify("text-justify"),
    /** Left aligned text */
    textLeft("text-left"),
    /** Lowercased text */
    textLowercase("text-lowercase"),
    /** No wrap text */
    textNowrap("text-nowrap"),
    /** Right aligned text */
    textRight("text-right"),
    /** Uppercased text */
    textUppercase("text-uppercase"),
    /** Medium 20px */
    title("title"),
    /** Title with color contrast */
    titleColorContrast("title-color-contrast")
}

// Used to generate these conveniently directly from documentation
fun main(args: Array<String>) {
    val data = """
mdl-typography--body-1	Regular 14px (Device), Regular 13px (Desktop)	Optional
mdl-typography--body-1-force-preferred-font	Regular 14px (Device), Regular 13px (Desktop)	Optional
mdl-typography--body-2	Medium 14px (Device), Medium 13px (Desktop)	Optional
mdl-typography--body-2	mdl-typography-body-2	Optional
mdl-typography--body-2-color-contrast	Body with color contrast	Optional
mdl-typography--body-2-force-preferred-font	Medium 14px (Device), Medium 13px (Desktop)	Optional
mdl-typography--button	Medium (All Caps) 14px	Optional
mdl-typography--caption	Regular 12px	Optional
mdl-typography--caption-color-contrast	Caption with color contrast	Optional
mdl-typography--display-1	Regular 34px	Optional
mdl-typography--display-1-color-contrast	Display with color contrast	Optional
mdl-typography--display-2	Regular 45px	Optional
mdl-typography--display-3	Regular 56px	Optional
mdl-typography--display-4	Light 112px	Optional
mdl-typography--headline	Regular 24px	Optional
mdl-typography--menu	Medium 14px (Device), Medium 13px (Desktop)	Optional
mdl-typography--subhead	Regular 16px (Device), Regular 15px (Desktop)	Optional
mdl-typography--subhead-color-contrast	Subhead with color contrast	Optional
mdl-typography--table-striped	Striped table	Optional
mdl-typography--text-capitalize	Capitalized text	Optional
mdl-typography--text-center	Center aligned text	Optional
mdl-typography--text-justify	Justified text	Optional
mdl-typography--text-left	Left aligned text	Optional
mdl-typography--text-lowercase	Lowercased text	Optional
mdl-typography--text-nowrap	No wrap text	Optional
mdl-typography--text-right	Right aligned text	Optional
mdl-typography--text-uppercase	Uppercased text	Optional
mdl-typography--title	Medium 20px	Optional
mdl-typography--title-color-contrast	Title with color contrast	Optional
""".lines().map { it.split("\t") }

    for (line in data) {
        if (line.size > 1) {
            val cls = line[0]
            val truncCls = cls.substring(startIndex = "mdl-typography--".length)
            val splitCls = truncCls.split("-")
            val enumNameBuilder = StringBuilder()
            enumNameBuilder.append(splitCls[0])
            for (subsequent in splitCls.subList(1, splitCls.size)) {
                enumNameBuilder.append(subsequent.capitalize())
            }
            val enumName = enumNameBuilder.toString()
            println(
                    """/** ${line[1]} */ $enumName("$truncCls"), """
            )
        }
    }
}