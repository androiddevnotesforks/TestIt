package com.mitteloupe.testit.generator

import com.mitteloupe.testit.model.DataType
import com.mitteloupe.testit.model.TypedParameter
import com.nhaarman.mockitokotlin2.UseConstructor
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MockerCodeGeneratorTest {
    private lateinit var cut: MockerCodeGenerator

    @Mock
    lateinit var mockableTypeQualifier: MockableTypeQualifier

    @Before
    fun setUp() {
        cut = mock(
            defaultAnswer = Mockito.CALLS_REAL_METHODS,
            useConstructor = UseConstructor.withArguments(mockableTypeQualifier)
        )
    }

    @Test
    fun `Given non-mockable data type when getMockedValue then returns default value`() {
        // Given
        val variableName = "variableName"
        val variableType = DataType.Specific("non-mockable data type")
        val expected = "default value"
        val concreteValue = getConcreteValue(variableName, variableType, expected)
        given { mockableTypeQualifier.getNonMockableType(variableType.name) }.willReturn(concreteValue)

        // When
        val actualValue = cut.getMockedValue(variableName, variableType)

        // Then
        assertEquals(expected, actualValue)
    }

    @Test
    fun `Given mockable data type when getMockedValue then returns mocked instance`() {
        // Given
        val variableName = "variableName"
        val variableType = DataType.Specific("mockable data type")
        given { mockableTypeQualifier.getNonMockableType(variableType.name) }.willReturn(null)
        val expected = "mocked instance"
        given { cut.getMockedInstance(variableType) }.willReturn(expected)

        // When
        val actualValue = cut.getMockedValue(variableName, variableType)

        // Then
        assertEquals(expected, actualValue)
    }

    @Test
    fun `Given non-mockable typed parameter when getMockedVariableDefinition then returns default value`() {
        // Given
        val variableName = "variableName"
        val variableType = DataType.Specific("non-mockable data type")
        val constructorMock = "\"constructor mock\""
        val concreteValue = getConcreteValue(variableName, variableType, constructorMock)
        given { mockableTypeQualifier.getNonMockableType(variableType.name) }.willReturn(concreteValue)
        val parameter = TypedParameter(variableName, type = variableType)
        val expected = "private val variableName = $constructorMock"

        // When
        val actualValue = cut.getMockedVariableDefinition(parameter)

        // Then
        assertEquals(expected, actualValue)
    }

    @Test
    fun `Given mockable typed parameter when getMockedVariableDefinition then returns constructor mock`() {
        // Given
        val variableName = "variableName"
        val variableType = DataType.Specific("mockable data type")
        given { mockableTypeQualifier.getNonMockableType(variableType.name) }.willReturn(null)
        val expected = "constructor mock"
        given { cut.getConstructorMock(variableName, variableType) }.willReturn(expected)
        val parameter = TypedParameter(variableName, type = variableType)

        // When
        val actualValue = cut.getMockedVariableDefinition(parameter)

        // Then
        assertEquals(expected, actualValue)
    }

    @Test
    fun `Given non-mockable parameter when TypedParameter#isMockable then returns false`() {
        // Given
        val receiver = mock<TypedParameter>()
        given { mockableTypeQualifier.isMockable(receiver) }.willReturn(false)

        // When
        val actualValue = with(cut) {
            receiver.isMockable()
        }

        // Then
        assertFalse(actualValue)
    }

    @Test
    fun `Given mockable parameter when TypedParameter#isMockable then returns true`() {
        // Given
        val receiver = mock<TypedParameter>()
        given { mockableTypeQualifier.isMockable(receiver) }.willReturn(true)

        // When
        val actualValue = with(cut) {
            receiver.isMockable()
        }

        // Then
        assertTrue(actualValue)
    }

    @Test
    fun `Given non-mockable when DataType#isMockable then returns false`() {
        // Given
        val receiver = mock<DataType>()
        given { mockableTypeQualifier.isMockable(receiver) }.willReturn(false)

        // When
        val actualValue = with(cut) {
            receiver.isMockable()
        }

        // Then
        assertFalse(actualValue)
    }

    @Test
    fun `Given mockable when DataType#isMockable then returns true`() {
        // Given
        val receiver = mock<DataType>()
        given { mockableTypeQualifier.isMockable(receiver) }.willReturn(true)

        // When
        val actualValue = with(cut) {
            receiver.isMockable()
        }

        // Then
        assertTrue(actualValue)
    }

    private fun getConcreteValue(
        variableName: String,
        variableType: DataType.Specific,
        expected: String
    ) = ConcreteValue("") { name, type ->
        assertEquals(variableName, name)
        assertEquals(variableType, type)
        expected
    }
}