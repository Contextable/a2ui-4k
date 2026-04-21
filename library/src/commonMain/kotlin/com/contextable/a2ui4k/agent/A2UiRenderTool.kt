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

package com.contextable.a2ui4k.agent

import com.contextable.a2ui4k.extension.A2UIExtension
import com.contextable.a2ui4k.state.SurfaceStateManager
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

/**
 * Canonical, transport-agnostic helper for the `render_a2ui` client-side tool.
 *
 * ## Intent
 *
 * Client apps that expose a2ui-4k's rendering engine to an agent as a
 * server-callable tool (AG-UI `ToolExecutor`, OpenAI function-calling, Gemini
 * tools, Anthropic `tool_use`, …) all end up doing the same three things:
 * extract `surfaceId`/`catalogId`/`components`/`data` out of the tool
 * arguments, feed three `processMessage` envelopes to a [SurfaceStateManager],
 * and return a success payload. This class packages that flow once.
 *
 * ## What this class does NOT do
 *
 * - It does not implement any SDK's `ToolExecutor` interface — that lives in
 *   consumer code so this library stays transport-free.
 * - It does no I/O, suspends no coroutines, and touches no threading.
 * - It does not deeply validate component JSON beyond "array shape". The
 *   surface state manager and widget parsers do their own downstream
 *   validation.
 *
 * ## Typical consumer adapter (AG-UI Kotlin SDK — ~4 lines)
 *
 * ```kotlin
 * class RenderA2UiToolExecutor(private val tool: A2UiRenderTool) : ToolExecutor {
 *     override val name = tool.name
 *     override val description = tool.description
 *     override val parameters = tool.parameters
 *     override suspend fun execute(arguments: JsonObject) = try {
 *         ToolExecutionResult.success(tool.render(arguments).toString())
 *     } catch (e: A2UiRenderException) {
 *         ToolExecutionResult.failure(e.message ?: "render_a2ui failed")
 *     }
 * }
 * ```
 *
 * @param surfaceStateManager the [SurfaceStateManager] instance the renderer
 *   reads from. Must be the same instance the Compose `A2UISurface` composable
 *   is bound to, otherwise rendered surfaces go into a different state store
 *   and never appear on screen.
 * @param description human-readable tool description the LLM reads when
 *   deciding whether to call the tool. Overridable; defaults to
 *   [DEFAULT_DESCRIPTION].
 */
class A2UiRenderTool(
    private val surfaceStateManager: SurfaceStateManager,
    val description: String = DEFAULT_DESCRIPTION,
) {
    /** Canonical tool name. Matches the React ecosystem (`@copilotkit/a2ui-renderer`). */
    val name: String = TOOL_NAME

    /** JSON Schema (OpenAI / Gemini function-calling compatible). */
    val parameters: JsonObject = DEFAULT_PARAMETERS

    /**
     * Drives [surfaceStateManager] through createSurface → updateComponents →
     * (optional) updateDataModel, using the A2UI v0.9 wire envelope.
     *
     * @param arguments tool-call arguments object — see [parameters] for the
     *   schema. The agent is expected to have conformed to that schema, but
     *   this method defends against missing / wrong-typed fields by throwing
     *   [A2UiRenderException] rather than silently emitting a half-rendered
     *   surface.
     * @return `{"status":"rendered","surfaceId":"<id>"}`. Consumer adapters
     *   typically `.toString()` this for their SDK's string-valued tool
     *   result.
     * @throws A2UiRenderException if `surfaceId` or `components` is missing
     *   or wrong-typed.
     */
    fun render(arguments: JsonObject): JsonObject {
        val surfaceId = requireStringField(arguments, "surfaceId")
        val catalogId = optionalStringField(arguments, "catalogId") ?: DEFAULT_CATALOG_ID
        val components = arguments["components"].let {
            when {
                it == null -> throw A2UiRenderException(
                    A2UiRenderException.ValidationCode.MISSING_REQUIRED_FIELD,
                    "components",
                    "render_a2ui: 'components' is required",
                )
                it !is JsonArray -> throw A2UiRenderException(
                    A2UiRenderException.ValidationCode.WRONG_TYPE,
                    "components",
                    "render_a2ui: 'components' must be a JSON array",
                )
                else -> it
            }
        }
        // 'data' is typed as object in the schema; a non-object value (array,
        // primitive, null) is treated as "no data model update" rather than a
        // hard error — agents sometimes send `data: null` instead of omitting.
        val data = arguments["data"] as? JsonObject

        surfaceStateManager.processMessage(
            buildJsonObject {
                put("version", JsonPrimitive(SurfaceStateManager.PROTOCOL_VERSION))
                put("createSurface", buildJsonObject {
                    put("surfaceId", JsonPrimitive(surfaceId))
                    put("catalogId", JsonPrimitive(catalogId))
                })
            }
        )
        surfaceStateManager.processMessage(
            buildJsonObject {
                put("version", JsonPrimitive(SurfaceStateManager.PROTOCOL_VERSION))
                put("updateComponents", buildJsonObject {
                    put("surfaceId", JsonPrimitive(surfaceId))
                    put("components", components)
                })
            }
        )
        if (data != null) {
            surfaceStateManager.processMessage(
                buildJsonObject {
                    put("version", JsonPrimitive(SurfaceStateManager.PROTOCOL_VERSION))
                    put("updateDataModel", buildJsonObject {
                        put("surfaceId", JsonPrimitive(surfaceId))
                        put("path", JsonPrimitive("/"))
                        put("value", data)
                    })
                }
            )
        }

        return buildJsonObject {
            put("status", JsonPrimitive("rendered"))
            put("surfaceId", JsonPrimitive(surfaceId))
        }
    }

    private fun requireStringField(args: JsonObject, field: String): String {
        val raw = args[field] ?: throw A2UiRenderException(
            A2UiRenderException.ValidationCode.MISSING_REQUIRED_FIELD,
            field,
            "render_a2ui: '$field' is required",
        )
        val prim = raw as? JsonPrimitive
        if (prim == null || !prim.isString) {
            throw A2UiRenderException(
                A2UiRenderException.ValidationCode.WRONG_TYPE,
                field,
                "render_a2ui: '$field' must be a string",
            )
        }
        if (prim.content.isBlank()) {
            throw A2UiRenderException(
                A2UiRenderException.ValidationCode.EMPTY_VALUE,
                field,
                "render_a2ui: '$field' must not be blank",
            )
        }
        return prim.content
    }

    private fun optionalStringField(args: JsonObject, field: String): String? {
        val prim = args[field] as? JsonPrimitive ?: return null
        return if (prim.isString) prim.content else null
    }

    companion object {
        /** Canonical tool name. */
        const val TOOL_NAME: String = "render_a2ui"

        /**
         * Default [catalogId] used when the tool call omits it. Set to
         * [A2UIExtension.STANDARD_CATALOG_URI]; [A2UIExtension.BASIC_CATALOG_URI_V09]
         * is also accepted on inbound envelopes (see
         * [SurfaceStateManager.processMessage]).
         */
        const val DEFAULT_CATALOG_ID: String = A2UIExtension.STANDARD_CATALOG_URI

        internal val DEFAULT_DESCRIPTION: String = """
            Renders an A2UI v0.9 surface on the client. A2UI is a declarative UI
            protocol: the server sends a component tree and (optionally) an initial
            data model, and the client renders a native UI that streams user events
            (button clicks, form input, data changes) back to the server.

            Arguments:
            - surfaceId (string, required): stable identifier for this surface.
              Reuse the same surfaceId to update an existing surface in place —
              components with matching ids are replaced, new components are added,
              unmentioned components are left untouched. Use a new surfaceId to
              create a separate, independently-rendered surface.
            - catalogId (string, optional): URI of the component catalog. Defaults
              to the A2UI v0.9 standard catalog. Only override if rendering from a
              custom catalog.
            - components (array, required): flat list of component objects, each
              shaped { "id": "<unique-id>", "component": "<WidgetType>", ...props }.
              The root component MUST have id "root". Container widgets (Column,
              Row, List, Card, Tabs) reference children by id via a "children"
              array. Literal values are plain JSON ("hello", 42, true); data
              bindings are { "path": "/pointer" }; function calls are
              { "call": "fnName", "args": {...} }.
            - data (object, optional): initial data model for this surface, merged
              at path "/". Paths referenced by component data bindings resolve
              against this model. Omit if components are fully literal.

            Returns { "status": "rendered", "surfaceId": "<id>" } on success.
        """.trimIndent()

        internal val DEFAULT_PARAMETERS: JsonObject = buildJsonObject {
            put("type", JsonPrimitive("object"))
            put("properties", buildJsonObject {
                put("surfaceId", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put(
                        "description",
                        JsonPrimitive(
                            "Stable identifier. Reuse to update in place; use a new value to create a separate surface."
                        )
                    )
                })
                put("catalogId", buildJsonObject {
                    put("type", JsonPrimitive("string"))
                    put(
                        "description",
                        JsonPrimitive(
                            "URI of the component catalog. Defaults to the A2UI v0.9 standard catalog."
                        )
                    )
                    put("default", JsonPrimitive(DEFAULT_CATALOG_ID))
                })
                put("components", buildJsonObject {
                    put("type", JsonPrimitive("array"))
                    put(
                        "description",
                        JsonPrimitive(
                            "Flat list of A2UI component objects. The root must have id 'root'."
                        )
                    )
                    put("items", buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("required", buildJsonArray {
                            add(JsonPrimitive("id"))
                            add(JsonPrimitive("component"))
                        })
                        put("properties", buildJsonObject {
                            put("id", buildJsonObject { put("type", JsonPrimitive("string")) })
                            put("component", buildJsonObject { put("type", JsonPrimitive("string")) })
                        })
                        // Widget props vary by component — leave the item schema open.
                        put("additionalProperties", JsonPrimitive(true))
                    })
                })
                put("data", buildJsonObject {
                    put("type", JsonPrimitive("object"))
                    put(
                        "description",
                        JsonPrimitive(
                            "Optional initial data model, merged at path '/'. Referenced by component data bindings of the form {\"path\": \"/...\"}."
                        )
                    )
                    put("additionalProperties", JsonPrimitive(true))
                })
            })
            put("required", buildJsonArray {
                add(JsonPrimitive("surfaceId"))
                add(JsonPrimitive("components"))
            })
            // Block LLMs from inventing extra top-level fields — only the four
            // above are meaningful.
            put("additionalProperties", JsonPrimitive(false))
        }
    }
}

/**
 * Thrown by [A2UiRenderTool.render] when tool-call arguments are missing or
 * wrong-typed. Carries a structured [code] + [field] so consumer adapters can
 * surface the human-readable [message] to the LLM and log the structured
 * cause separately.
 *
 * Extends [IllegalArgumentException] so callers that don't catch it get a
 * sensible default error.
 */
class A2UiRenderException(
    val code: ValidationCode,
    val field: String,
    message: String,
) : IllegalArgumentException(message) {

    /** Machine-readable cause, for structured logging / adapter branching. */
    enum class ValidationCode {
        /** The field was absent from the arguments object. */
        MISSING_REQUIRED_FIELD,

        /** The field was present but of the wrong JSON type. */
        WRONG_TYPE,

        /** The field was present, the right type, but blank/empty. */
        EMPTY_VALUE,
    }
}
