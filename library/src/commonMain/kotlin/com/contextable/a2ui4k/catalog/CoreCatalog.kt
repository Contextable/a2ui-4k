package com.contextable.a2ui4k.catalog

import com.contextable.a2ui4k.catalog.widgets.AudioPlayerWidget
import com.contextable.a2ui4k.catalog.widgets.ButtonWidget
import com.contextable.a2ui4k.catalog.widgets.CardWidget
import com.contextable.a2ui4k.catalog.widgets.CheckBoxWidget
import com.contextable.a2ui4k.catalog.widgets.MultipleChoiceWidget
import com.contextable.a2ui4k.catalog.widgets.ColumnWidget
import com.contextable.a2ui4k.catalog.widgets.DateTimeInputWidget
import com.contextable.a2ui4k.catalog.widgets.DividerWidget
import com.contextable.a2ui4k.catalog.widgets.IconWidget
import com.contextable.a2ui4k.catalog.widgets.ImageWidget
import com.contextable.a2ui4k.catalog.widgets.ListWidget
import com.contextable.a2ui4k.catalog.widgets.ModalWidget
import com.contextable.a2ui4k.catalog.widgets.RowWidget
import com.contextable.a2ui4k.catalog.widgets.SliderWidget
import com.contextable.a2ui4k.catalog.widgets.TabsWidget
import com.contextable.a2ui4k.catalog.widgets.TextFieldWidget
import com.contextable.a2ui4k.catalog.widgets.TextWidget
import com.contextable.a2ui4k.catalog.widgets.VideoWidget
import com.contextable.a2ui4k.model.Catalog

/**
 * The core catalog of built-in A2UI widgets.
 *
 * This catalog provides the standard set of widgets defined in
 * the A2UI protocol standard_catalog_definition.json. Applications
 * can use this catalog directly or combine it with custom catalogs.
 *
 * Standard Catalog Widgets:
 * - Text: Display text with optional markdown and styling
 * - TextField: Text input field with label and data binding
 * - Button: Clickable button with action support
 * - Image: Display images from URLs
 * - Column: Vertical layout container
 * - Row: Horizontal layout container
 * - List: Scrollable list (vertical or horizontal)
 * - Card: Material card container
 * - Divider: Horizontal or vertical divider line
 * - Icon: Material icon from predefined set
 * - CheckBox: Boolean input with label
 * - Slider: Numeric range input
 * - MultipleChoice: Selection component
 * - DateTimeInput: Date/time picker
 * - Tabs: Tabbed navigation container
 * - Modal: Dialog overlay
 * - Video: Video player (placeholder)
 * - AudioPlayer: Audio player (placeholder)
 *
 * Usage:
 * ```kotlin
 * A2UiSurface(
 *     definition = uiDefinition,
 *     catalog = CoreCatalog
 * )
 * ```
 */
val CoreCatalog: Catalog = Catalog.of(
    id = "standard",
    TextWidget,
    TextFieldWidget,
    ButtonWidget,
    ImageWidget,
    ColumnWidget,
    RowWidget,
    ListWidget,
    CardWidget,
    DividerWidget,
    IconWidget,
    CheckBoxWidget,
    SliderWidget,
    MultipleChoiceWidget,
    DateTimeInputWidget,
    TabsWidget,
    ModalWidget,
    VideoWidget,
    AudioPlayerWidget
)

/**
 * Object providing access to core catalog items individually.
 */
object CoreCatalogItems {
    val text = TextWidget
    val textField = TextFieldWidget
    val button = ButtonWidget
    val image = ImageWidget
    val column = ColumnWidget
    val row = RowWidget
    val list = ListWidget
    val card = CardWidget
    val divider = DividerWidget
    val icon = IconWidget
    val checkBox = CheckBoxWidget
    val slider = SliderWidget
    val multipleChoice = MultipleChoiceWidget
    val dateTimeInput = DateTimeInputWidget
    val tabs = TabsWidget
    val modal = ModalWidget
    val video = VideoWidget
    val audioPlayer = AudioPlayerWidget

    /**
     * Returns the core catalog.
     */
    fun asCatalog(): Catalog = CoreCatalog
}
