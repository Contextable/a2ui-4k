package com.contextable.a2ui4k.example.widgets

/**
 * Sample JSON definitions for each widget type (v0.8 format).
 *
 * v0.8 uses nested component format:
 * "component": { "WidgetType": { ...properties } }
 */
object WidgetSamples {

    fun getJson(widgetName: String): String = when (widgetName) {
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
        "MultipleChoice" -> MULTIPLE_CHOICE_SAMPLE
        "DateTimeInput" -> DATE_TIME_SAMPLE
        "Tabs" -> TABS_SAMPLE
        "Modal" -> MODAL_SAMPLE
        "Video" -> VIDEO_SAMPLE
        "AudioPlayer" -> AUDIO_PLAYER_SAMPLE
        else -> MINIMAL_SAMPLE
    }

    private val TEXT_SAMPLE = """
{
  "surfaceId": "text-demo",
  "root": "root",
  "components": {
    "root": {
      "id": "root",
      "component": {
        "Column": {
          "children": {"explicitList": ["h1", "h2", "body", "caption"]}
        }
      }
    },
    "h1": {
      "id": "h1",
      "component": {
        "Text": {
          "text": "Heading 1",
          "usageHint": "h1"
        }
      }
    },
    "h2": {
      "id": "h2",
      "component": {
        "Text": {
          "text": "Heading 2",
          "usageHint": "h2"
        }
      }
    },
    "body": {
      "id": "body",
      "component": {
        "Text": {
          "text": "This is **body text** with _markdown_ support."
        }
      }
    },
    "caption": {
      "id": "caption",
      "component": {
        "Text": {
          "text": "Small caption text",
          "usageHint": "caption"
        }
      }
    }
  }
}
    """.trimIndent()

    private val TEXT_FIELD_SAMPLE = """
{
  "surfaceId": "textfield-demo",
  "root": "root",
  "components": {
    "root": {
      "id": "root",
      "component": {
        "Column": {
          "children": {"explicitList": ["label", "input"]}
        }
      }
    },
    "label": {
      "id": "label",
      "component": {
        "Text": {
          "text": "Enter your name:",
          "usageHint": "label"
        }
      }
    },
    "input": {
      "id": "input",
      "component": {
        "TextField": {
          "label": "Name",
          "placeholder": "John Doe",
          "value": {"path": "/form/name"}
        }
      }
    }
  }
}
    """.trimIndent()

    private val BUTTON_SAMPLE = """
{
  "surfaceId": "button-demo",
  "root": "root",
  "components": {
    "root": {
      "id": "root",
      "component": {
        "Column": {
          "children": {"explicitList": ["primary-btn", "secondary-btn", "feedback"]}
        }
      }
    },
    "primary-btn": {
      "id": "primary-btn",
      "component": {
        "Button": {
          "child": "primary-text",
          "primary": true,
          "action": {
            "name": "submit",
            "dataUpdates": [
              {"path": "/ui/lastClicked", "value": "Primary Button clicked!"}
            ]
          }
        }
      }
    },
    "primary-text": {
      "id": "primary-text",
      "component": {
        "Text": {
          "text": "Primary Button"
        }
      }
    },
    "secondary-btn": {
      "id": "secondary-btn",
      "component": {
        "Button": {
          "child": "secondary-text",
          "primary": false,
          "action": {
            "name": "cancel",
            "dataUpdates": [
              {"path": "/ui/lastClicked", "value": "Secondary Button clicked!"}
            ]
          }
        }
      }
    },
    "secondary-text": {
      "id": "secondary-text",
      "component": {
        "Text": {
          "text": "Secondary Button"
        }
      }
    },
    "feedback": {
      "id": "feedback",
      "component": {
        "Text": {
          "text": {"path": "/ui/lastClicked"},
          "usageHint": "caption"
        }
      }
    }
  }
}
    """.trimIndent()

    private val IMAGE_SAMPLE = """
{
  "surfaceId": "image-demo",
  "root": "root",
  "components": {
    "root": {
      "id": "root",
      "component": {
        "Column": {
          "children": {"explicitList": ["label", "image"]}
        }
      }
    },
    "label": {
      "id": "label",
      "component": {
        "Text": {
          "text": "Image from URL:",
          "usageHint": "label"
        }
      }
    },
    "image": {
      "id": "image",
      "component": {
        "Image": {
          "url": "https://picsum.photos/400/200",
          "contentDescription": "Random placeholder image",
          "height": 200
        }
      }
    }
  }
}
    """.trimIndent()

    private val ICON_SAMPLE = """
{
  "surfaceId": "icon-demo",
  "root": "root",
  "components": {
    "root": {
      "id": "root",
      "component": {
        "Row": {
          "children": {"explicitList": ["home", "settings", "search", "star", "favorite"]}
        }
      }
    },
    "home": {
      "id": "home",
      "component": {
        "Icon": {"name": "home"}
      }
    },
    "settings": {
      "id": "settings",
      "component": {
        "Icon": {"name": "settings"}
      }
    },
    "search": {
      "id": "search",
      "component": {
        "Icon": {"name": "search"}
      }
    },
    "star": {
      "id": "star",
      "component": {
        "Icon": {"name": "star"}
      }
    },
    "favorite": {
      "id": "favorite",
      "component": {
        "Icon": {"name": "favorite"}
      }
    }
  }
}
    """.trimIndent()

    private val COLUMN_SAMPLE = """
{
  "surfaceId": "column-demo",
  "root": "root",
  "components": {
    "root": {
      "id": "root",
      "component": {
        "Column": {
          "distribution": "spaceEvenly",
          "children": {"explicitList": ["item1", "item2", "item3"]}
        }
      }
    },
    "item1": {
      "id": "item1",
      "component": {
        "Text": {"text": "First item"}
      }
    },
    "item2": {
      "id": "item2",
      "component": {
        "Text": {"text": "Second item"}
      }
    },
    "item3": {
      "id": "item3",
      "component": {
        "Text": {"text": "Third item"}
      }
    }
  }
}
    """.trimIndent()

    private val ROW_SAMPLE = """
{
  "surfaceId": "row-demo",
  "root": "root",
  "components": {
    "root": {
      "id": "root",
      "component": {
        "Row": {
          "distribution": "spaceBetween",
          "children": {"explicitList": ["left", "center", "right"]}
        }
      }
    },
    "left": {
      "id": "left",
      "component": {
        "Text": {"text": "Left"}
      }
    },
    "center": {
      "id": "center",
      "component": {
        "Text": {"text": "Center"}
      }
    },
    "right": {
      "id": "right",
      "component": {
        "Text": {"text": "Right"}
      }
    }
  }
}
    """.trimIndent()

    private val LIST_SAMPLE = """
{
  "surfaceId": "list-demo",
  "root": "root",
  "components": {
    "root": {
      "id": "root",
      "component": {
        "List": {
          "direction": "vertical",
          "children": {"explicitList": ["item1", "item2", "item3", "item4"]}
        }
      }
    },
    "item1": {
      "id": "item1",
      "component": {
        "Card": {"child": "text1"}
      }
    },
    "text1": {
      "id": "text1",
      "component": {
        "Text": {"text": "List Item 1"}
      }
    },
    "item2": {
      "id": "item2",
      "component": {
        "Card": {"child": "text2"}
      }
    },
    "text2": {
      "id": "text2",
      "component": {
        "Text": {"text": "List Item 2"}
      }
    },
    "item3": {
      "id": "item3",
      "component": {
        "Card": {"child": "text3"}
      }
    },
    "text3": {
      "id": "text3",
      "component": {
        "Text": {"text": "List Item 3"}
      }
    },
    "item4": {
      "id": "item4",
      "component": {
        "Card": {"child": "text4"}
      }
    },
    "text4": {
      "id": "text4",
      "component": {
        "Text": {"text": "List Item 4"}
      }
    }
  }
}
    """.trimIndent()

    private val CARD_SAMPLE = """
{
  "surfaceId": "card-demo",
  "root": "root",
  "components": {
    "root": {
      "id": "root",
      "component": {
        "Column": {
          "children": {"explicitList": ["card1", "card2"]}
        }
      }
    },
    "card1": {
      "id": "card1",
      "component": {
        "Card": {"child": "card1-content"}
      }
    },
    "card1-content": {
      "id": "card1-content",
      "component": {
        "Column": {
          "children": {"explicitList": ["card1-title", "card1-body"]}
        }
      }
    },
    "card1-title": {
      "id": "card1-title",
      "component": {
        "Text": {
          "text": "Card Title",
          "usageHint": "h3"
        }
      }
    },
    "card1-body": {
      "id": "card1-body",
      "component": {
        "Text": {
          "text": "This is the card content with some descriptive text."
        }
      }
    },
    "card2": {
      "id": "card2",
      "component": {
        "Card": {"child": "card2-text"}
      }
    },
    "card2-text": {
      "id": "card2-text",
      "component": {
        "Text": {
          "text": "Simple card with text only"
        }
      }
    }
  }
}
    """.trimIndent()

    private val DIVIDER_SAMPLE = """
{
  "surfaceId": "divider-demo",
  "root": "root",
  "components": {
    "root": {
      "id": "root",
      "component": {
        "Column": {
          "children": {"explicitList": ["above", "divider", "below"]}
        }
      }
    },
    "above": {
      "id": "above",
      "component": {
        "Text": {"text": "Content above divider"}
      }
    },
    "divider": {
      "id": "divider",
      "component": {
        "Divider": {}
      }
    },
    "below": {
      "id": "below",
      "component": {
        "Text": {"text": "Content below divider"}
      }
    }
  }
}
    """.trimIndent()

    private val CHECKBOX_SAMPLE = """
{
  "surfaceId": "checkbox-demo",
  "root": "root",
  "components": {
    "root": {
      "id": "root",
      "component": {
        "Column": {
          "children": {"explicitList": ["check1", "check2", "check3"]}
        }
      }
    },
    "check1": {
      "id": "check1",
      "component": {
        "CheckBox": {
          "label": "Accept terms and conditions",
          "value": {"path": "/form/acceptTerms"}
        }
      }
    },
    "check2": {
      "id": "check2",
      "component": {
        "CheckBox": {
          "label": "Subscribe to newsletter",
          "value": {"path": "/form/newsletter"}
        }
      }
    },
    "check3": {
      "id": "check3",
      "component": {
        "CheckBox": {
          "label": "Enable notifications",
          "value": {"path": "/form/notifications"}
        }
      }
    }
  }
}
    """.trimIndent()

    private val SLIDER_SAMPLE = """
{
  "surfaceId": "slider-demo",
  "root": "root",
  "components": {
    "root": {
      "id": "root",
      "component": {
        "Column": {
          "children": {"explicitList": ["label", "slider", "value-text"]}
        }
      }
    },
    "label": {
      "id": "label",
      "component": {
        "Text": {
          "text": "Volume Control",
          "usageHint": "label"
        }
      }
    },
    "slider": {
      "id": "slider",
      "component": {
        "Slider": {
          "label": "Volume",
          "min": 0,
          "max": 100,
          "value": {"path": "/settings/volume"}
        }
      }
    },
    "value-text": {
      "id": "value-text",
      "component": {
        "Text": {
          "text": {"path": "/settings/volume"}
        }
      }
    }
  }
}
    """.trimIndent()

    private val MULTIPLE_CHOICE_SAMPLE = """
{
  "surfaceId": "choice-demo",
  "root": "root",
  "components": {
    "root": {
      "id": "root",
      "component": {
        "Column": {
          "children": {"explicitList": ["label", "picker"]}
        }
      }
    },
    "label": {
      "id": "label",
      "component": {
        "Text": {
          "text": "Select your favorite color:",
          "usageHint": "label"
        }
      }
    },
    "picker": {
      "id": "picker",
      "component": {
        "MultipleChoice": {
          "label": "Color",
          "options": [
            {"label": "Red", "value": "red"},
            {"label": "Green", "value": "green"},
            {"label": "Blue", "value": "blue"},
            {"label": "Yellow", "value": "yellow"}
          ],
          "value": {"path": "/form/color"}
        }
      }
    }
  }
}
    """.trimIndent()

    private val DATE_TIME_SAMPLE = """
{
  "surfaceId": "datetime-demo",
  "root": "root",
  "components": {
    "root": {
      "id": "root",
      "component": {
        "Column": {
          "children": {"explicitList": ["label", "datetime"]}
        }
      }
    },
    "label": {
      "id": "label",
      "component": {
        "Text": {
          "text": "Select date and time:",
          "usageHint": "label"
        }
      }
    },
    "datetime": {
      "id": "datetime",
      "component": {
        "DateTimeInput": {
          "label": "Appointment",
          "enableDate": true,
          "enableTime": true,
          "value": {"path": "/form/appointment"}
        }
      }
    }
  }
}
    """.trimIndent()

    private val TABS_SAMPLE = """
{
  "surfaceId": "tabs-demo",
  "root": "root",
  "components": {
    "root": {
      "id": "root",
      "component": {
        "Tabs": {
          "tabItems": [
            {"title": "Home", "child": "tab1-content"},
            {"title": "Profile", "child": "tab2-content"},
            {"title": "Settings", "child": "tab3-content"}
          ]
        }
      }
    },
    "tab1-content": {
      "id": "tab1-content",
      "component": {
        "Text": {"text": "Welcome to the Home tab!"}
      }
    },
    "tab2-content": {
      "id": "tab2-content",
      "component": {
        "Text": {"text": "This is your Profile tab."}
      }
    },
    "tab3-content": {
      "id": "tab3-content",
      "component": {
        "Text": {"text": "Adjust your Settings here."}
      }
    }
  }
}
    """.trimIndent()

    private val MODAL_SAMPLE = """
{
  "surfaceId": "modal-demo",
  "root": "root",
  "components": {
    "root": {
      "id": "root",
      "component": {
        "Column": {
          "children": {"explicitList": ["info", "open-btn", "modal"]}
        }
      }
    },
    "info": {
      "id": "info",
      "component": {
        "Text": {
          "text": "Click the button below to open the modal dialog."
        }
      }
    },
    "open-btn": {
      "id": "open-btn",
      "component": {
        "Button": {
          "child": "open-btn-text",
          "primary": true,
          "action": {
            "name": "openModal",
            "dataUpdates": [
              {"path": "/ui/modalOpen", "value": true}
            ]
          }
        }
      }
    },
    "open-btn-text": {
      "id": "open-btn-text",
      "component": {
        "Text": {
          "text": "Open Modal"
        }
      }
    },
    "modal": {
      "id": "modal",
      "component": {
        "Modal": {
          "title": "Confirmation Dialog",
          "open": {"path": "/ui/modalOpen"},
          "child": "modal-content"
        }
      }
    },
    "modal-content": {
      "id": "modal-content",
      "component": {
        "Column": {
          "children": {"explicitList": ["modal-text", "modal-details"]}
        }
      }
    },
    "modal-text": {
      "id": "modal-text",
      "component": {
        "Text": {
          "text": "Are you sure you want to proceed?",
          "usageHint": "h3"
        }
      }
    },
    "modal-details": {
      "id": "modal-details",
      "component": {
        "Text": {
          "text": "This action cannot be undone."
        }
      }
    }
  }
}
    """.trimIndent()

    private val VIDEO_SAMPLE = """
{
  "surfaceId": "video-demo",
  "root": "root",
  "components": {
    "root": {
      "id": "root",
      "component": {
        "Column": {
          "children": {"explicitList": ["label", "video"]}
        }
      }
    },
    "label": {
      "id": "label",
      "component": {
        "Text": {
          "text": "Video Player (placeholder)",
          "usageHint": "h2"
        }
      }
    },
    "video": {
      "id": "video",
      "component": {
        "Video": {
          "url": "https://example.com/video.mp4",
          "description": "Sample video content"
        }
      }
    }
  }
}
    """.trimIndent()

    private val AUDIO_PLAYER_SAMPLE = """
{
  "surfaceId": "audio-demo",
  "root": "root",
  "components": {
    "root": {
      "id": "root",
      "component": {
        "Column": {
          "children": {"explicitList": ["label", "audio"]}
        }
      }
    },
    "label": {
      "id": "label",
      "component": {
        "Text": {
          "text": "Audio Player (placeholder)",
          "usageHint": "h2"
        }
      }
    },
    "audio": {
      "id": "audio",
      "component": {
        "AudioPlayer": {
          "url": "https://example.com/audio.mp3",
          "description": "Sample audio content"
        }
      }
    }
  }
}
    """.trimIndent()

    private val MINIMAL_SAMPLE = """
{
  "surfaceId": "demo",
  "root": "text",
  "components": {
    "text": {
      "id": "text",
      "component": {
        "Text": {
          "text": "Hello, A2UI!"
        }
      }
    }
  }
}
    """.trimIndent()
}
