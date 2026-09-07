package com.vector.launch

import com.vector.launch.core.model.AppInfo
import com.vector.launch.launcher.APP_ALPHABET
import com.vector.launch.launcher.APP_NAVIGATION_ITEMS
import com.vector.launch.launcher.FAVORITES_ITEM
import com.vector.launch.launcher.AppBrowserListItem
import com.vector.launch.launcher.alphabetIndexForTouch
import com.vector.launch.launcher.buildAppBrowserListModel
import com.vector.launch.launcher.findNearestAlphabetSectionIndex
import com.vector.launch.launcher.waveInfluence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VectorAlphabetNavigationTest {

    @Test
    fun favoritesRemainTheFirstEdgeNavigationTarget() {
        assertEquals(FAVORITES_ITEM, APP_NAVIGATION_ITEMS.first())
        assertEquals(APP_ALPHABET, APP_NAVIGATION_ITEMS.drop(1))
    }

    @Test
    fun touchPositionMapsAcrossTheWholeAlphabet() {
        val top = alphabetIndexForTouch(touchY = 0f, height = 1_000f, itemCount = APP_ALPHABET.size)
        val middle = alphabetIndexForTouch(touchY = 500f, height = 1_000f, itemCount = APP_ALPHABET.size)
        val bottom = alphabetIndexForTouch(touchY = 1_000f, height = 1_000f, itemCount = APP_ALPHABET.size)

        assertEquals('A', APP_ALPHABET[top!!])
        assertEquals('N', APP_ALPHABET[middle!!])
        assertEquals('#', APP_ALPHABET[bottom!!])
    }

    @Test
    fun touchPositionClampsAndRejectsInvalidBars() {
        assertEquals(0, alphabetIndexForTouch(-100f, 1_000f, APP_ALPHABET.size))
        assertEquals(APP_ALPHABET.lastIndex, alphabetIndexForTouch(1_200f, 1_000f, APP_ALPHABET.size))
        assertNull(alphabetIndexForTouch(10f, 0f, APP_ALPHABET.size))
        assertNull(alphabetIndexForTouch(10f, 100f, 0))
    }

    @Test
    fun waveInfluenceFallsOffSmoothlyFromTheFinger() {
        val atFinger = waveInfluence(distance = 0f, sigma = 100f)
        val nearby = waveInfluence(distance = 50f, sigma = 100f)
        val farAway = waveInfluence(distance = 300f, sigma = 100f)

        assertEquals(1f, atFinger, 0.0001f)
        assertTrue(nearby in farAway..atFinger)
        assertTrue(farAway < 0.02f)
        assertEquals(0f, waveInfluence(distance = 10f, sigma = 0f), 0f)
    }

    @Test
    fun missingLetterResolvesToTheNextSectionThenThePreviousOne() {
        val sectionIndices = mapOf('A' to 0, 'B' to 5, 'E' to 12, 'K' to 20)

        assertEquals(12, findNearestAlphabetSectionIndex('C', sectionIndices))
        assertEquals(20, findNearestAlphabetSectionIndex('Z', sectionIndices))
        assertNull(findNearestAlphabetSectionIndex('C', emptyMap()))
    }

    @Test
    fun symbolSectionRemainsReachableAfterZ() {
        val sectionIndices = mapOf('A' to 0, '#' to 8)

        assertEquals(8, findNearestAlphabetSectionIndex('Z', sectionIndices))
        assertEquals(8, findNearestAlphabetSectionIndex('#', sectionIndices))
    }

    @Test
    fun appBrowserModelKeepsSymbolsAtTheBottomAndIndexesHeaders() {
        val model = buildAppBrowserListModel(
            listOf(
                AppInfo("two.factor", "2FA"),
                AppInfo("beta", "Beta"),
                AppInfo("almanac", "Almanac"),
                AppInfo("alpha", "Alpha")
            )
        )

        assertEquals(listOf('A', 'B', '#'), model.sectionIndices.keys.toList())
        assertEquals(mapOf('A' to 0, 'B' to 3, '#' to 5), model.sectionIndices)
        assertTrue(model.items[0] is AppBrowserListItem.Header)
        assertEquals("Almanac", (model.items[1] as AppBrowserListItem.AppRow).app.label)
        assertEquals("Alpha", (model.items[2] as AppBrowserListItem.AppRow).app.label)
    }
}
