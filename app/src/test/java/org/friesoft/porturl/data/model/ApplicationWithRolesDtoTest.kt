package org.friesoft.porturl.data.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.friesoft.porturl.data.model.ApplicationWithRolesDto
import org.friesoft.porturl.data.model.ApplicationCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class JsonParsingTest {

    @Test
    fun testParseApplicationWithRolesDto() {
        val json = """
        [
            {
                "application": {
                    "id": 4,
                    "name": "GitHub",
                    "url": "https://github.com",
                    "applicationCategories": [
                        {
                            "id": {"applicationId": 4, "categoryId": 20},
                            "category": {"id": 20, "name": "asdf", "sortOrder": 3, "applicationSortMode": "CUSTOM", "icon": null, "description": null, "enabled": true},
                            "sortOrder": 5
                        }
                    ],
                    "iconLarge": null,
                    "iconMedium": null,
                    "iconThumbnail": null,
                    "iconUrlLarge": null,
                    "iconUrlMedium": null,
                    "iconUrlThumbnail": null
                },
                "availableRoles": ["admin", "viewer"]
            }
        ]
        """.trimIndent()

        val gson = Gson()
        val listType = object : TypeToken<List<ApplicationWithRolesDto>>() {}.type
        val result: List<ApplicationWithRolesDto> = gson.fromJson(json, listType)

        assertNotNull(result)
        assertEquals(1, result.size)

        val dto = result[0]
        assertNotNull(dto.application)
        assertEquals(4L, dto.application.id)
        assertEquals("GitHub", dto.application.name)

        // Check categories
        assertNotNull(dto.application.applicationCategories)
        assertEquals(1, dto.application.applicationCategories.size)
        val cat = dto.application.applicationCategories[0]
        assertNotNull(cat.category)
        assertEquals(20L, cat.category?.id)

        // Check availableRoles
        assertNotNull(dto.availableRoles)
        assertEquals(2, dto.availableRoles.size)
        assertEquals("admin", dto.availableRoles[0])
    }

    @Test
    fun testRepositoryMappingLogic() {
        // simulate the mapping logic
        val dto = ApplicationWithRolesDto(
            application = org.friesoft.porturl.data.model.Application(
                id = 1,
                name = "Test",
                url = "http://test.com",
                description = null,
                roles = emptyList(),
                applicationCategories = emptyList(),
                iconLarge = null, iconMedium = null, iconThumbnail = null,
                iconUrlLarge = null, iconUrlMedium = null, iconUrlThumbnail = null
            ),
            availableRoles = listOf("admin")
        )

        val app = dto.application.apply { roles = dto.availableRoles }

        assertEquals(1, app.roles.size)
        assertEquals("admin", app.roles[0])
    }
}
