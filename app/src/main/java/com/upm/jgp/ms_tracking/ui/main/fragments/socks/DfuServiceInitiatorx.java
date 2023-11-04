package com.upm.jgp.healthywear.ui.main.fragments.socks;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelUuid;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.upm.jgp.healthywear.R;

import org.jetbrains.annotations.Nullable;

import java.security.InvalidParameterException;
import java.util.UUID;

import no.nordicsemi.android.dfu.DfuServiceController;
import vpno.nordicsemi.android.dfu.DfuBaseService;
import vpno.nordicsemi.android.dfu.DfuServiceInitiator;

public class DfuServiceInitiatorx {
    public static final int DEFAULT_PRN_VALUE = 12;

    /** Constant used to narrow the scope of the update to system components (SD+BL) only. */
    public static final int SCOPE_SYSTEM_COMPONENTS = 7578;
    /** Constant used to narrow the scope of the update to application only. */
    public static final int SCOPE_APPLICATION = 3542;

    private final String deviceAddress;
    private String deviceName;

    private boolean disableNotification = false;
    private boolean startAsForegroundService = true;

    private Uri fileUri;
    private String filePath;
    private int fileResId;

    private Uri initFileUri;
    private String initFilePath;
    private int initFileResId;

    private String mimeType;
    private int fileType = -1;

    private boolean keepBond;
    private boolean restoreBond;
    private boolean forceDfu = false;
    private boolean enableUnsafeExperimentalButtonlessDfu = false;

    private Boolean packetReceiptNotificationsEnabled;
    private int numberOfPackets = 12;

    private int mtu = 517;

    private Parcelable[] legacyDfuUuids;
    private Parcelable[] secureDfuUuids;
    private Parcelable[] experimentalButtonlessDfuUuids;
    private Parcelable[] buttonlessDfuWithoutBondSharingUuids;
    private Parcelable[] buttonlessDfuWithBondSharingUuids;

    public DfuServiceInitiatorx(@NonNull final String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }
    public DfuServiceInitiatorx setDeviceName(@Nullable final String name) {
        this.deviceName = name;
        return this;
    }
    public DfuServiceInitiatorx setDisableNotification(final boolean disableNotification) {
        this.disableNotification = disableNotification;
        return this;
    }
    public DfuServiceInitiatorx setForeground(final boolean foreground) {
        this.startAsForegroundService = foreground;
        return this;
    }
    public DfuServiceInitiatorx setKeepBond(final boolean keepBond) {
        this.keepBond = keepBond;
        return this;
    }public DfuServiceInitiatorx setRestoreBond(final boolean restoreBond) {
        this.restoreBond = restoreBond;
        return this;
    }public DfuServiceInitiatorx setPacketsReceiptNotificationsEnabled(final boolean enabled) {
        this.packetReceiptNotificationsEnabled = enabled;
        return this;
    }public DfuServiceInitiatorx setPacketsReceiptNotificationsValue(final int number) {
        this.numberOfPackets = number > 0 ? number : DEFAULT_PRN_VALUE;
        return this;
    }public DfuServiceInitiatorx setForceDfu(final boolean force) {
        this.forceDfu = force;
        return this;
    }public DfuServiceInitiatorx setMtu(final int mtu) {
        this.mtu = mtu;
        return this;
    }public DfuServiceInitiatorx disableMtuRequest() {
        this.mtu = 0;
        return this;
    }public DfuServiceInitiatorx setScope(final int scope) {
        if (!DfuBaseService.MIME_TYPE_ZIP.equals(mimeType))
            throw new UnsupportedOperationException("Scope can be set only for a ZIP file");
        if (scope == SCOPE_APPLICATION)
            fileType = DfuBaseService.TYPE_APPLICATION;
        else if (scope == SCOPE_SYSTEM_COMPONENTS)
            fileType = DfuBaseService.TYPE_SOFT_DEVICE | DfuBaseService.TYPE_BOOTLOADER;
        else throw new UnsupportedOperationException("Unknown scope");
        return this;
    }public DfuServiceInitiatorx setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(final boolean enable) {
        this.enableUnsafeExperimentalButtonlessDfu = enable;
        return this;
    }public DfuServiceInitiatorx setCustomUuidsForLegacyDfu(@Nullable final UUID dfuServiceUuid,
                                                           @Nullable final UUID dfuControlPointUuid,
                                                           @Nullable final UUID dfuPacketUuid,
                                                           @Nullable final UUID dfuVersionUuid) {
        final ParcelUuid[] uuids = new ParcelUuid[4];
        uuids[0] = dfuServiceUuid      != null ? new ParcelUuid(dfuServiceUuid)      : null;
        uuids[1] = dfuControlPointUuid != null ? new ParcelUuid(dfuControlPointUuid) : null;
        uuids[2] = dfuPacketUuid       != null ? new ParcelUuid(dfuPacketUuid)       : null;
        uuids[3] = dfuVersionUuid      != null ? new ParcelUuid(dfuVersionUuid)      : null;
        legacyDfuUuids = uuids;
        return this;
    }public DfuServiceInitiatorx setCustomUuidsForSecureDfu(@Nullable final UUID dfuServiceUuid,
                                                           @Nullable final UUID dfuControlPointUuid,
                                                           @Nullable final UUID dfuPacketUuid) {
        final ParcelUuid[] uuids = new ParcelUuid[3];
        uuids[0] = dfuServiceUuid      != null ? new ParcelUuid(dfuServiceUuid)      : null;
        uuids[1] = dfuControlPointUuid != null ? new ParcelUuid(dfuControlPointUuid) : null;
        uuids[2] = dfuPacketUuid       != null ? new ParcelUuid(dfuPacketUuid)       : null;
        secureDfuUuids = uuids;
        return this;
    }public DfuServiceInitiatorx setCustomUuidsForExperimentalButtonlessDfu(@Nullable final UUID buttonlessDfuServiceUuid,
                                                                           @Nullable final UUID buttonlessDfuControlPointUuid) {
        final ParcelUuid[] uuids = new ParcelUuid[2];
        uuids[0] = buttonlessDfuServiceUuid      != null ? new ParcelUuid(buttonlessDfuServiceUuid)      : null;
        uuids[1] = buttonlessDfuControlPointUuid != null ? new ParcelUuid(buttonlessDfuControlPointUuid) : null;
        experimentalButtonlessDfuUuids = uuids;
        return this;
    }public DfuServiceInitiatorx setCustomUuidsForButtonlessDfuWithBondSharing(@Nullable final UUID buttonlessDfuServiceUuid,
                                                                              @Nullable final UUID buttonlessDfuControlPointUuid) {
        final ParcelUuid[] uuids = new ParcelUuid[2];
        uuids[0] = buttonlessDfuServiceUuid      != null ? new ParcelUuid(buttonlessDfuServiceUuid)      : null;
        uuids[1] = buttonlessDfuControlPointUuid != null ? new ParcelUuid(buttonlessDfuControlPointUuid) : null;
        buttonlessDfuWithBondSharingUuids = uuids;
        return this;
    }public DfuServiceInitiatorx setCustomUuidsForButtonlessDfuWithoutBondSharing(@Nullable final UUID buttonlessDfuServiceUuid,
                                                                                 @Nullable final UUID buttonlessDfuControlPointUuid) {
        final ParcelUuid[] uuids = new ParcelUuid[2];
        uuids[0] = buttonlessDfuServiceUuid      != null ? new ParcelUuid(buttonlessDfuServiceUuid)      : null;
        uuids[1] = buttonlessDfuControlPointUuid != null ? new ParcelUuid(buttonlessDfuControlPointUuid) : null;
        buttonlessDfuWithoutBondSharingUuids = uuids;
        return this;
    }

    public DfuServiceInitiatorx setZip(@NonNull final Uri uri) {
        return init(uri, null, 0, DfuBaseService.TYPE_AUTO, DfuBaseService.MIME_TYPE_ZIP);
    }

    public DfuServiceInitiatorx setZip(@NonNull final String path) {
        return init(null, path, 0, DfuBaseService.TYPE_AUTO, DfuBaseService.MIME_TYPE_ZIP);
    }public DfuServiceInitiatorx setZip(final int rawResId) {
        return init(null, null, rawResId, DfuBaseService.TYPE_AUTO, DfuBaseService.MIME_TYPE_ZIP);
    }public DfuServiceInitiatorx setZip(@Nullable final Uri uri, @Nullable final String path) {
        return init(uri, path, 0, DfuBaseService.TYPE_AUTO, DfuBaseService.MIME_TYPE_ZIP);
    }@Deprecated
    public DfuServiceInitiatorx setBinOrHex(final int fileType, @NonNull final Uri uri) {
        if (fileType == DfuBaseService.TYPE_AUTO)
            throw new UnsupportedOperationException("You must specify the file type");
        return init(uri, null, 0, fileType, DfuBaseService.MIME_TYPE_OCTET_STREAM);
    }@Deprecated
    public DfuServiceInitiatorx setBinOrHex(final int fileType, @NonNull final String path) {
        if (fileType == DfuBaseService.TYPE_AUTO)
            throw new UnsupportedOperationException("You must specify the file type");
        return init(null, path, 0, fileType, DfuBaseService.MIME_TYPE_OCTET_STREAM);
    }@Deprecated
    public DfuServiceInitiatorx setBinOrHex(final int fileType, @Nullable final Uri uri, @Nullable final String path) {
        if (fileType == DfuBaseService.TYPE_AUTO)
            throw new UnsupportedOperationException("You must specify the file type");
        return init(uri, path, 0, fileType, DfuBaseService.MIME_TYPE_OCTET_STREAM);
    }@Deprecated
    public DfuServiceInitiatorx setBinOrHex(final int fileType, final int rawResId) {
        if (fileType == DfuBaseService.TYPE_AUTO)
            throw new UnsupportedOperationException("You must specify the file type");
        return init(null, null, rawResId, fileType, DfuBaseService.MIME_TYPE_OCTET_STREAM);
    }@Deprecated
    public DfuServiceInitiatorx setInitFile(@NonNull final Uri initFileUri) {
        return init(initFileUri, null, 0);
    }@Deprecated
    public DfuServiceInitiatorx setInitFile(@Nullable final String initFilePath) {
        return init(null, initFilePath, 0);
    }@Deprecated
    public DfuServiceInitiatorx setInitFile(final int initFileResId) {
        return init(null, null, initFileResId);
    }@Deprecated
    public DfuServiceInitiatorx setInitFile(@Nullable final Uri initFileUri, @Nullable final String initFilePath) {
        return init(initFileUri, initFilePath, 0);
    }
    /*public DfuServiceController start(@NonNull final Context context, @NonNull final Class<? extends DfuBaseService> service) {
        if (fileType == -1)
            throw new UnsupportedOperationException("You must specify the firmware file before starting the service");

        final Intent intent = new Intent(context, service);

        intent.putExtra(DfuBaseService.EXTRA_DEVICE_ADDRESS, deviceAddress);
        intent.putExtra(DfuBaseService.EXTRA_DEVICE_NAME, deviceName);
        intent.putExtra(DfuBaseService.EXTRA_DISABLE_NOTIFICATION, disableNotification);
        intent.putExtra(DfuBaseService.EXTRA_FOREGROUND_SERVICE, startAsForegroundService);
        intent.putExtra(DfuBaseService.EXTRA_FILE_MIME_TYPE, mimeType);
        intent.putExtra(DfuBaseService.EXTRA_FILE_TYPE, fileType);
        intent.putExtra(DfuBaseService.EXTRA_FILE_URI, fileUri);
        intent.putExtra(DfuBaseService.EXTRA_FILE_PATH, filePath);
        intent.putExtra(DfuBaseService.EXTRA_FILE_RES_ID, fileResId);
        intent.putExtra(DfuBaseService.EXTRA_INIT_FILE_URI, initFileUri);
        intent.putExtra(DfuBaseService.EXTRA_INIT_FILE_PATH, initFilePath);
        intent.putExtra(DfuBaseService.EXTRA_INIT_FILE_RES_ID, initFileResId);
        intent.putExtra(DfuBaseService.EXTRA_KEEP_BOND, keepBond);
        intent.putExtra(DfuBaseService.EXTRA_RESTORE_BOND, restoreBond);
        intent.putExtra(DfuBaseService.EXTRA_FORCE_DFU, forceDfu);
        if (mtu > 0)
            intent.putExtra(DfuBaseService.EXTRA_MTU, mtu);
        intent.putExtra(DfuBaseService.EXTRA_UNSAFE_EXPERIMENTAL_BUTTONLESS_DFU, enableUnsafeExperimentalButtonlessDfu);
        if (packetReceiptNotificationsEnabled != null) {
            intent.putExtra(DfuBaseService.EXTRA_PACKET_RECEIPT_NOTIFICATIONS_ENABLED, packetReceiptNotificationsEnabled);
            intent.putExtra(DfuBaseService.EXTRA_PACKET_RECEIPT_NOTIFICATIONS_VALUE, numberOfPackets);
        } else {
            // For backwards compatibility:
            // If the setPacketsReceiptNotificationsEnabled(boolean) has not been called, the PRN state and value are taken from
            // SharedPreferences the way they were read in DFU Library in 1.0.3 and before, or set to default values.
            // Default values: PRNs enabled on Android 4.3 - 5.1 and disabled starting from Android 6.0. Default PRN value is 12.
        }
        if (legacyDfuUuids != null)
            intent.putExtra(DfuBaseService.EXTRA_CUSTOM_UUIDS_FOR_LEGACY_DFU, legacyDfuUuids);
        if (secureDfuUuids != null)
            intent.putExtra(DfuBaseService.EXTRA_CUSTOM_UUIDS_FOR_SECURE_DFU, secureDfuUuids);
        if (experimentalButtonlessDfuUuids != null)
            intent.putExtra(DfuBaseService.EXTRA_CUSTOM_UUIDS_FOR_EXPERIMENTAL_BUTTONLESS_DFU, experimentalButtonlessDfuUuids);
        if (buttonlessDfuWithoutBondSharingUuids != null)
            intent.putExtra(DfuBaseService.EXTRA_CUSTOM_UUIDS_FOR_BUTTONLESS_DFU_WITHOUT_BOND_SHARING, buttonlessDfuWithoutBondSharingUuids);
        if (buttonlessDfuWithBondSharingUuids != null)
            intent.putExtra(DfuBaseService.EXTRA_CUSTOM_UUIDS_FOR_BUTTONLESS_DFU_WITH_BOND_SHARING, buttonlessDfuWithBondSharingUuids);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && startAsForegroundService) {
            // On Android Oreo and above the service must be started as a foreground service to make it accessible from
            // a killed application.
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
        return new DfuServiceController(context);
    }*/

    private DfuServiceInitiatorx init(@Nullable final Uri initFileUri,
                                     @Nullable final String initFilePath,
                                     final int initFileResId) {
        if (DfuBaseService.MIME_TYPE_ZIP.equals(mimeType))
            throw new InvalidParameterException("Init file must be located inside the ZIP");

        this.initFileUri = initFileUri;
        this.initFilePath = initFilePath;
        this.initFileResId = initFileResId;
        return this;
    }

    private DfuServiceInitiatorx init(@Nullable final Uri fileUri,
                                     @Nullable final String filePath,
                                     final int fileResId, final int fileType,
                                     @NonNull final String mimeType) {
        this.fileUri = fileUri;
        this.filePath = filePath;
        this.fileResId = fileResId;
        this.fileType = fileType;
        this.mimeType = mimeType;

        // If the MIME TYPE implies it's a ZIP file then the init file must be included in the file.
        if (DfuBaseService.MIME_TYPE_ZIP.equals(mimeType)) {
            this.initFileUri = null;
            this.initFilePath = null;
            this.initFileResId = 0;
        }
        return this;
    }
    /*
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createDfuNotificationChannel(@NonNull final Context context) {
        final NotificationChannel channel =
                new NotificationChannel(DfuBaseService.NOTIFICATION_CHANNEL_DFU, context.getString(R.string.dfu_channel_name), NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(context.getString(R.string.dfu_channel_description));
        channel.setShowBadge(false);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }*/

}
