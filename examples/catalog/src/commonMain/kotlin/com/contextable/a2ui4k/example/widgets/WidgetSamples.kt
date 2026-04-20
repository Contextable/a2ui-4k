/*
 * Copyright 2025 Contextable LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.contextable.a2ui4k.example.widgets

/**
 * Sample definitions for each widget type (v0.9 format).
 *
 * Each sample properly separates:
 * - components: Layout definitions with path bindings to data
 * - data: Actual content values
 *
 * v0.9 format uses flat component definitions:
 * - `{"id":"x", "component":"Text", "text": {"path":"/msg"}, "variant":"h1"}`
 * - Children are plain arrays: `["c1","c2"]`
 * - Literal values are plain: `"foo"`, `42`, `true`
 * - Path bindings remain: `{"path":"/x"}`
 */
data class WidgetSample(
    val components: String,
    val data: String
)

object WidgetSamples {

    fun getSample(widgetName: String): WidgetSample = when (widgetName) {
        "Custom" -> CUSTOM_SAMPLE
        "Text" -> TEXT_SAMPLE
        "TextField" -> TEXT_FIELD_SAMPLE
        "Button" -> BUTTON_SAMPLE
        "Image" -> IMAGE_SAMPLE
        "Icon" -> ICON_SAMPLE
        "Column" -> COLUMN_SAMPLE
        "Row" -> ROW_SAMPLE
        "List" -> LIST_SAMPLE
        "Card" -> CARD_SAMPLE
        "Divider" -> DIVIDER_SAMPLE
        "CheckBox" -> CHECKBOX_SAMPLE
        "Slider" -> SLIDER_SAMPLE
        "DateTimeInput" -> DATE_TIME_SAMPLE
        "Tabs" -> TABS_SAMPLE
        "Modal" -> MODAL_SAMPLE
        "Video" -> VIDEO_SAMPLE
        "AudioPlayer" -> AUDIO_PLAYER_SAMPLE
        else -> MINIMAL_SAMPLE
    }

    private val TEXT_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "Column", "children": ["h1", "h2", "body", "caption"]},
  {"id": "h1", "component": "Text", "text": {"path": "/heading1"}, "variant": "h1"},
  {"id": "h2", "component": "Text", "text": {"path": "/heading2"}, "variant": "h2"},
  {"id": "body", "component": "Text", "text": {"path": "/bodyText"}},
  {"id": "caption", "component": "Text", "text": {"path": "/captionText"}, "variant": "caption"}
]
        """.trimIndent(),
        data = """{
  "heading1": "Heading 1",
  "heading2": "Heading 2",
  "bodyText": "This is **body text** with _markdown_ support.",
  "captionText": "Small caption text"
}"""
    )

    private val TEXT_FIELD_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "Column", "children": ["input"]},
  {"id": "input", "component": "TextField", "label": {"path": "/inputLabel"}, "value": {"path": "/name"}}
]
        """.trimIndent(),
        data = """{
  "inputLabel": "Enter your name",
  "name": ""
}"""
    )

    private val BUTTON_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "Column", "children": ["primary-btn", "secondary-btn", "feedback"]},
  {"id": "primary-btn", "component": "Button", "child": "primary-text", "variant": "filled", "action": {"event": {"name": "submit", "context": {"source": "primary"}}, "dataUpdates": [{"path": "/lastClicked", "value": "Primary Button clicked!"}]}},
  {"id": "primary-text", "component": "Text", "text": {"path": "/primaryLabel"}},
  {"id": "secondary-btn", "component": "Button", "child": "secondary-text", "variant": "outlined", "action": {"event": {"name": "cancel", "context": {"source": "secondary"}}, "dataUpdates": [{"path": "/lastClicked", "value": "Secondary Button clicked!"}]}},
  {"id": "secondary-text", "component": "Text", "text": {"path": "/secondaryLabel"}},
  {"id": "feedback", "component": "Text", "text": {"path": "/lastClicked"}, "variant": "caption"}
]
        """.trimIndent(),
        data = """{
  "primaryLabel": "Primary Button",
  "secondaryLabel": "Secondary Button",
  "lastClicked": "Click a button..."
}"""
    )

    private val IMAGE_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "Column", "children": ["label", "image"]},
  {"id": "label", "component": "Text", "text": {"path": "/label"}, "variant": "body"},
  {"id": "image", "component": "Image", "url": {"path": "/imageUrl"}, "fit": "cover", "variant": "mediumFeature"}
]
        """.trimIndent(),
        data = """{
  "label": "Image from URL:",
  "imageUrl": "https://picsum.photos/400/200"
}"""
    )

    private val ICON_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "Row", "children": ["home", "settings", "search", "star", "favorite"]},
  {"id": "home", "component": "Icon", "name": {"path": "/icons/0"}},
  {"id": "settings", "component": "Icon", "name": {"path": "/icons/1"}},
  {"id": "search", "component": "Icon", "name": {"path": "/icons/2"}},
  {"id": "star", "component": "Icon", "name": {"path": "/icons/3"}},
  {"id": "favorite", "component": "Icon", "name": {"path": "/icons/4"}}
]
        """.trimIndent(),
        data = """{
  "icons": ["home", "settings", "search", "star", "favorite"]
}"""
    )

    private val COLUMN_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "Column", "justify": "spaceEvenly", "children": ["item1", "item2", "item3"]},
  {"id": "item1", "component": "Text", "text": {"path": "/items/0"}},
  {"id": "item2", "component": "Text", "text": {"path": "/items/1"}},
  {"id": "item3", "component": "Text", "text": {"path": "/items/2"}}
]
        """.trimIndent(),
        data = """{
  "items": ["First item", "Second item", "Third item"]
}"""
    )

    private val ROW_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "Row", "justify": "spaceBetween", "children": ["left", "center", "right"]},
  {"id": "left", "component": "Text", "text": {"path": "/positions/left"}},
  {"id": "center", "component": "Text", "text": {"path": "/positions/center"}},
  {"id": "right", "component": "Text", "text": {"path": "/positions/right"}}
]
        """.trimIndent(),
        data = """{
  "positions": {
    "left": "Left",
    "center": "Center",
    "right": "Right"
  }
}"""
    )

    private val LIST_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "List", "direction": "vertical", "children": {"componentId": "item-template", "path": "/items"}},
  {"id": "item-template", "component": "Card", "child": "item-text"},
  {"id": "item-text", "component": "Text", "text": {"path": "/title"}}
]
        """.trimIndent(),
        data = """{
  "items": [
    {"title": "List Item 1"},
    {"title": "List Item 2"},
    {"title": "List Item 3"},
    {"title": "List Item 4"}
  ]
}"""
    )

    private val CARD_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "Column", "children": ["card1", "card2"]},
  {"id": "card1", "component": "Card", "child": "card1-content"},
  {"id": "card1-content", "component": "Column", "children": ["card1-title", "card1-body"]},
  {"id": "card1-title", "component": "Text", "text": {"path": "/cards/0/title"}, "variant": "h3"},
  {"id": "card1-body", "component": "Text", "text": {"path": "/cards/0/body"}},
  {"id": "card2", "component": "Card", "child": "card2-text"},
  {"id": "card2-text", "component": "Text", "text": {"path": "/cards/1/body"}}
]
        """.trimIndent(),
        data = """{
  "cards": [
    {"title": "Card Title", "body": "This is the card content with some descriptive text."},
    {"body": "Simple card with text only"}
  ]
}"""
    )

    private val DIVIDER_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "Column", "children": ["above", "divider", "below"]},
  {"id": "above", "component": "Text", "text": {"path": "/above"}},
  {"id": "divider", "component": "Divider"},
  {"id": "below", "component": "Text", "text": {"path": "/below"}}
]
        """.trimIndent(),
        data = """{
  "above": "Content above divider",
  "below": "Content below divider"
}"""
    )

    private val CHECKBOX_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "Column", "children": ["check1", "check2", "check3"]},
  {"id": "check1", "component": "CheckBox", "label": "Accept terms and conditions", "value": {"path": "/acceptTerms"}},
  {"id": "check2", "component": "CheckBox", "label": "Subscribe to newsletter", "value": {"path": "/newsletter"}},
  {"id": "check3", "component": "CheckBox", "label": "Enable notifications", "value": {"path": "/notifications"}}
]
        """.trimIndent(),
        data = """{
  "acceptTerms": false,
  "newsletter": true,
  "notifications": false
}"""
    )

    private val SLIDER_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "Column", "children": ["label", "slider", "value-text"]},
  {"id": "label", "component": "Text", "text": {"path": "/label"}, "variant": "body"},
  {"id": "slider", "component": "Slider", "value": {"path": "/volume"}, "min": 0, "max": 100},
  {"id": "value-text", "component": "Text", "text": {"path": "/volume"}}
]
        """.trimIndent(),
        data = """{
  "label": "Volume Control",
  "volume": 50
}"""
    )

    private val DATE_TIME_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "Column", "children": ["label", "datetime"]},
  {"id": "label", "component": "Text", "text": {"path": "/label"}, "variant": "body"},
  {"id": "datetime", "component": "DateTimeInput", "value": {"path": "/appointment"}, "enableDate": true, "enableTime": true}
]
        """.trimIndent(),
        data = """{
  "label": "Select date and time:",
  "appointment": ""
}"""
    )

    private val TABS_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "Tabs", "tabs": [{"title": {"path": "/tabs/0/title"}, "child": "tab1-content"}, {"title": {"path": "/tabs/1/title"}, "child": "tab2-content"}, {"title": {"path": "/tabs/2/title"}, "child": "tab3-content"}]},
  {"id": "tab1-content", "component": "Text", "text": {"path": "/tabs/0/content"}},
  {"id": "tab2-content", "component": "Text", "text": {"path": "/tabs/1/content"}},
  {"id": "tab3-content", "component": "Text", "text": {"path": "/tabs/2/content"}}
]
        """.trimIndent(),
        data = """{
  "tabs": [
    {"title": "Home", "content": "Welcome to the Home tab!"},
    {"title": "Profile", "content": "This is your Profile tab."},
    {"title": "Settings", "content": "Adjust your Settings here."}
  ]
}"""
    )

    private val MODAL_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "Modal", "trigger": "trigger", "content": "modal-content"},
  {"id": "trigger", "component": "Card", "child": "trigger-text"},
  {"id": "trigger-text", "component": "Text", "text": {"path": "/triggerLabel"}},
  {"id": "modal-content", "component": "Column", "children": ["modal-title", "modal-body"]},
  {"id": "modal-title", "component": "Text", "text": {"path": "/modal/title"}, "variant": "h3"},
  {"id": "modal-body", "component": "Text", "text": {"path": "/modal/body"}}
]
        """.trimIndent(),
        data = """{
  "triggerLabel": "Tap to open modal",
  "modal": {
    "title": "Confirmation Dialog",
    "body": "Are you sure you want to proceed? This action cannot be undone."
  }
}"""
    )

    private val VIDEO_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "Column", "children": ["label", "video"]},
  {"id": "label", "component": "Text", "text": {"path": "/label"}, "variant": "h2"},
  {"id": "video", "component": "Video", "url": {"path": "/videoUrl"}}
]
        """.trimIndent(),
        data = """{
  "label": "Video Player (placeholder)",
  "videoUrl": "https://example.com/video.mp4"
}"""
    )

    private val AUDIO_PLAYER_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "Column", "children": ["label", "audio"]},
  {"id": "label", "component": "Text", "text": {"path": "/label"}, "variant": "h2"},
  {"id": "audio", "component": "AudioPlayer", "url": {"path": "/audioUrl"}}
]
        """.trimIndent(),
        data = """{
  "label": "Audio Player (placeholder)",
  "audioUrl": "https://example.com/audio.mp3"
}"""
    )

    private val MINIMAL_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "Text", "text": {"path": "/message"}}
]
        """.trimIndent(),
        data = """{"message": "Hello, A2UI!"}"""
    )

    private val CUSTOM_SAMPLE = WidgetSample(
        components = """
[
  {"id": "root", "component": "Text", "text": {"path": "/message"}}
]
        """.trimIndent(),
        data = """{"message": "Edit this sample!"}"""
    )
}
