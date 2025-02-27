package com.feilongproject.baassetsdownloader

import android.os.Build
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.feilongproject.baassetsdownloader.util.ApkAssetInfo
import java.io.*
import javax.net.ssl.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageDownload(modifier: Modifier, padding: PaddingValues, selectServer: String) {
    var assetLoadProgress by remember { mutableStateOf(0f) }
    var assetLoadStatus: String? by remember { mutableStateOf(null) }
    var downloadProgress by remember { mutableStateOf(0f) }
    val context = LocalContext.current
    val apkAssetInfo: ApkAssetInfo? by remember { mutableStateOf(ApkAssetInfo(context, selectServer)) }
    apkAssetInfo!!.versionCheck { p, i ->
        assetLoadProgress = p
        downloadProgress = p
        assetLoadStatus = i
    }

    Log.d("FLP_DEBUG", "@Composable:PageDownload $selectServer")

    Column(modifier = modifier.padding(paddingValues = padding).padding(vertical = 4.dp)) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            modifier = maxWidth.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Column(modifier = maxWidth.padding(10.dp)) {
                Row(
                    maxWidth,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.fillMaxWidth(0.7f).padding(end = 5.dp)) {
                        Text(
                            stringResource(R.string.nowSelect) + stringResource(if (selectServer == "jpServer") R.string.jpServer else R.string.globalServer)
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            Divider(modifier = maxWidth.padding(top = 5.dp, bottom = 5.dp))
                            Text(stringResource(R.string.forAndroid11DownloadAssets))
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        AssistChip(
                            onClick = {
                                apkAssetInfo!!.versionCheck { p, i ->
                                    assetLoadProgress = p
                                    downloadProgress = p
                                    assetLoadStatus = i
                                }
                            },
                            label = { Text(stringResource(R.string.flash)) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.onPrimary)
                        )//刷新
                        AssistChip(
                            onClick = {
                                apkAssetInfo!!.downloadApk { p, i ->
                                    downloadProgress = p
                                    assetLoadStatus = i
                                }
                            },
                            label = { Text(stringResource(R.string.installApk)) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.onPrimary)
                        )//安装包
                        if (selectServer == "jpServer") AssistChip(
                            onClick = {
                                apkAssetInfo!!.downloadObb { p, i ->
                                    downloadProgress = p
                                    assetLoadStatus = i
                                }
                            },
                            label = { Text(stringResource(R.string.installObb)) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.onPrimary)
                        )//数据包
                    }
                }
                Divider(modifier = maxWidth.padding(top = 5.dp, bottom = 5.dp))
                Crossfade(targetState = assetLoadProgress) { p ->
                    when (p) {
                        1f, -1f -> Column {
                            AssistChip(
                                modifier = Modifier.padding(5.dp),
                                onClick = {
                                    apkAssetInfo!!.versionCheck { p, i ->
                                        assetLoadProgress = p
                                        assetLoadStatus = i
                                    }
                                },
                                label = {
                                    assetLoadStatus?.let { Text(it) }
                                },
                                leadingIcon = {
                                    when (downloadProgress) {
                                        1f -> Icon(Icons.Default.Done, stringResource(R.string.getSuccess))
                                        -1f -> Icon(Icons.Default.Error, stringResource(R.string.getError))
                                        else -> CircularProgressIndicator(
                                            progress = downloadProgress,
                                            modifier = Modifier.size(AssistChipDefaults.IconSize)/* color = Color.Blue*/
                                        )
                                    }
                                },
                                colors = AssistChipDefaults.assistChipColors(containerColor = if (downloadProgress == -1f || p == -1f) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary)
                            )
                            val sNotFound = stringResource(R.string.notFound)
                            Text(
                                stringResource(R.string.serverVersionName) +
                                        (apkAssetInfo?.serverVersionName ?: sNotFound)
                            )
                            Text(
                                stringResource(R.string.localVersionName) + (apkAssetInfo?.localVersionName
                                    ?: sNotFound)
                            )
                            if (apkAssetInfo?.localVersionName != apkAssetInfo?.serverVersionName)
                                Text(stringResource(R.string.apkNotSameServerVersion))
                            else Text(stringResource(R.string.apkSameServerVersion))
                            if (apkAssetInfo?.serverType == "jpServer") {
                                if (apkAssetInfo?.localObbFile?.length == apkAssetInfo?.serverObbLength && apkAssetInfo?.serverObbLength != null) {
                                    Text(stringResource(R.string.obbSameServerVersion))
                                } else Text(stringResource(R.string.obbNotSameServerVersion))
                                //Text("${apkAssetInfo?.localObbFile?.length} == ${apkAssetInfo?.serverObbLength}")
                            }
                        }

                        else -> {
                            Row(
                                maxWidth,
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = Color.Blue)
                                Text(stringResource(R.string.loading))
                            }
                        }
                    }
                }
            }
        }
    }
}