/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.tiptime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiptime.ui.theme.TipTimeTheme
import androidx.compose.material3.Icon
import java.text.DecimalFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TipTimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    BmiCalculatorLayout()
                }
            }
        }
    }
}

@Composable
fun BmiCalculatorLayout() {
    var heightInput by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }
    var bmiResult by remember { mutableStateOf("") }

    var heightUnit by remember { mutableStateOf("cm") }
    val heightUnits = listOf("cm", "in")
    var weightUnit by remember { mutableStateOf("kg") }
    val weightUnits = listOf("kg", "lbs")

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 40.dp)
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.calculate_bmi),
            modifier = Modifier
                .padding(bottom = 16.dp, top = 40.dp)
                .align(alignment = Alignment.Start)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
        ){
            EditNumberField(
                value = heightInput,
                onValueChanged = { heightInput = it },
                label = stringResource(R.string.height_amount),
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            UnitDropdownMenu(
                selectedUnit = heightUnit,
                units = heightUnits,
                onUnitSelected = { heightUnit = it }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
        )
        {
            EditNumberField(
                value = weightInput,
                onValueChanged = { weightInput = it },
                label = stringResource(R.string.weight_amount),
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            UnitDropdownMenu(
                selectedUnit = weightUnit,
                units = weightUnits,
                onUnitSelected = { weightUnit = it }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
        )
        {
            Button(
                onClick = {
                    val heightInCm = when (heightUnit) {
                        "cm" -> heightInput.toDoubleOrNull() ?: 0.0
                        "in" -> convertInchToCm(heightInput.toDoubleOrNull() ?: 0.0)
                        else -> 0.0
                    }
                    val weightInKg = when (weightUnit) {
                        "kg" -> weightInput.toDoubleOrNull() ?: 0.0
                        "lbs" -> convertPoundsToKg(weightInput.toDoubleOrNull() ?: 0.0)
                        else -> 0.0
                    }

                    bmiResult = calculateBMI(heightInCm, weightInKg)
                },
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .weight(2f)
            ) {
                Text(text = stringResource(R.string.calculate_button))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    heightInput = ""
                    weightInput = ""
                    bmiResult = ""
                },
                modifier = Modifier
                    .padding(bottom = 16.dp)
            ) {
                Text(text = stringResource(R.string.clear_button))
            }
        }
        if (bmiResult.isNotEmpty()) {
            Text(
                text = stringResource(R.string.bmi_amount, bmiResult),
                style = MaterialTheme.typography.displaySmall
            )
        }
        Spacer(modifier = Modifier.height(150.dp))
    }
}

@Composable
fun EditNumberField(
    value: String,
    onValueChanged: (String) -> Unit,
    label: String,
    modifier: Modifier
) {
    TextField(
        value = value,
        singleLine = true,
        modifier = modifier,
        onValueChange = { newValue ->
            // Filter out non-numeric characters except decimal point
            val filteredValue = newValue.filter { char ->
                char.isDigit() || char == '.'
            }
            onValueChanged(filteredValue)
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
fun UnitDropdownMenu(
    selectedUnit: String,
    units: List<String>,
    onUnitSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.width(100.dp)) {
        TextField(
            value = selectedUnit,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown Arrow",
                    modifier = Modifier.clickable { expanded = true }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun convertInchToCm(inches: Double): Double {
    return inches * 2.54
}

private fun convertPoundsToKg(pounds: Double): Double {
    return pounds * 0.453592
}

private fun calculateBMI(height: Double, weight: Double): String {
    if (height == 0.0) return "0.0" // Avoid division by zero
    val heightInMeters = height / 100 //convert cm to m

    val bmi = weight / (heightInMeters * heightInMeters)
    val df = DecimalFormat("#.##")
    return df.format(bmi)
}

@Preview(showBackground = true)
@Composable
fun TipTimeLayoutPreview() {
    TipTimeTheme {
        BmiCalculatorLayout()
    }
}

