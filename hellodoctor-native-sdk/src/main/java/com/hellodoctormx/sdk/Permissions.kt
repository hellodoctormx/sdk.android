@file:OptIn(ExperimentalPermissionsApi::class)

package com.hellodoctormx.sdk

import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.*

@Composable
fun WithVideoCallPermissions(
    permissions: List<String> = listOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.CAMERA),
    rationale: String = "Necesitamos acceder a la cámara y el micrófono de su dispositivo para iniciar su videollamada.",
    permissionNotAvailableContent: @Composable () -> Unit = { },
    content: @Composable () -> Unit = { }
) {
    val permissionsState = rememberMultiplePermissionsState(permissions)
    PermissionsRequired(
        multiplePermissionsState = permissionsState,
        permissionsNotGrantedContent = {
            Rationale(
                text = rationale,
                onRequestPermission = { permissionsState.launchMultiplePermissionRequest() },
            )
        },
        permissionsNotAvailableContent = permissionNotAvailableContent,
        content = content
    )
}

@Composable
private fun Rationale(text: String, onRequestPermission: () -> Unit) {
    Column() {
        Button(onClick = onRequestPermission) {
            Text("Ok")
        }
        AlertDialog(
            onDismissRequest = { /* Don't */ },
            title = {
                Text(text = "Solicitud de permiso")
            },
            text = {
                Text(text)
            },
            confirmButton = {
                Button(onClick = onRequestPermission) {
                    Text("Ok")
                }
            }
        )
    }
}