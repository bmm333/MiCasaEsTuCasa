package com.mobile.micasaestucasa.ui.components.report

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.micasaestucasa.ui.theme.CaptionLabels
import com.mobile.micasaestucasa.ui.theme.CardSurface
import com.mobile.micasaestucasa.ui.theme.ErrorColor
import com.mobile.micasaestucasa.ui.theme.HeadingText
import com.mobile.micasaestucasa.ui.theme.Primario

/**
 * Predefined reasons for reporting a user.
 */
val reportReasons = listOf(
    "Inappropriate content",
    "Scam or fraud",
    "Safety concern",
    "Harassment",
    "Misleading listing",
    "Other"
)

/**
 * Reusable report dialog that can be shown from Chat, Property Detail,
 * or any other screen where a user might want to report another user.
 *
 * @param reportedUserId The ID of the user being reported
 * @param onDismiss Called when the dialog is cancelled
 * @param onSubmit Called with (reason, description) when the user confirms
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDialog(
    reportedUserId: String,
    onDismiss: () -> Unit,
    onSubmit: (reason: String, description: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                "Report User",
                fontWeight = FontWeight.Bold,
                color = HeadingText,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    "Why are you reporting this user?",
                    fontSize = 13.sp,
                    color = CaptionLabels
                )
                Spacer(Modifier.height(12.dp))

                // Reason dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedReason,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Reason") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        shape = RoundedCornerShape(12.dp),
                        isError = showError && selectedReason.isBlank(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primario,
                            cursorColor = Primario
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        reportReasons.forEach { reason ->
                            DropdownMenuItem(
                                text = { Text(reason) },
                                onClick = {
                                    selectedReason = reason
                                    expanded = false
                                    showError = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Additional details (optional)") },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primario,
                        cursorColor = Primario
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (showError && selectedReason.isBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Please select a reason", color = ErrorColor, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (selectedReason.isBlank()) {
                    showError = true
                } else {
                    onSubmit(selectedReason, description.trim())
                }
            }) {
                Text("Submit Report", color = ErrorColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CaptionLabels)
            }
        }
    )
}
