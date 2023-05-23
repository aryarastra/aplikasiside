package com.google.android.gms.maps.model;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import com.bumptech.glide.load.Key;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.common.internal.safeparcel.AbstractSafeParcelable;
import com.google.android.gms.common.internal.safeparcel.SafeParcelWriter;
import com.google.android.gms.common.util.IOUtils;
import java.io.IOException;

/* compiled from: com.google.android.gms:play-services-maps@@18.0.0 */
/* loaded from: classes.dex */
public final class MapStyleOptions extends AbstractSafeParcelable {
    private String zzb;
    private static final String zza = MapStyleOptions.class.getSimpleName();
    public static final Parcelable.Creator<MapStyleOptions> CREATOR = new zzh();

    public MapStyleOptions(String json) {
        Preconditions.checkNotNull(json, "json must not be null");
        this.zzb = json;
    }

    public static MapStyleOptions loadRawResourceStyle(Context clientContext, int resourceId) throws Resources.NotFoundException {
        try {
            return new MapStyleOptions(new String(IOUtils.readInputStreamFully(clientContext.getResources().openRawResource(resourceId)), Key.STRING_CHARSET_NAME));
        } catch (IOException e) {
            String obj = e.toString();
            StringBuilder sb = new StringBuilder(obj.length() + 37);
            sb.append("Failed to read resource ");
            sb.append(resourceId);
            sb.append(": ");
            sb.append(obj);
            throw new Resources.NotFoundException(sb.toString());
        }
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int i) {
        int beginObjectHeader = SafeParcelWriter.beginObjectHeader(out);
        SafeParcelWriter.writeString(out, 2, this.zzb, false);
        SafeParcelWriter.finishObjectHeader(out, beginObjectHeader);
    }
}
