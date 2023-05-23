package im.delight.android.location;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import java.util.Random;

/* loaded from: classes.dex */
public class SimpleLocation {
    private static final long INTERVAL_DEFAULT = 600000;
    private static final float KILOMETER_TO_METER = 1000.0f;
    private static final float LATITUDE_TO_KILOMETER = 111.133f;
    private static final float LONGITUDE_TO_KILOMETER_AT_ZERO_LATITUDE = 111.32f;
    private static final String PROVIDER_COARSE = "network";
    private static final String PROVIDER_FINE = "gps";
    private static final String PROVIDER_FINE_PASSIVE = "passive";
    private static Location mCachedPosition;
    private int mBlurRadius;
    private final long mInterval;
    private Listener mListener;
    private LocationListener mLocationListener;
    private final LocationManager mLocationManager;
    private final boolean mPassive;
    private Location mPosition;
    private final boolean mRequireFine;
    private final boolean mRequireNewLocation;
    private static final Random mRandom = new Random();
    private static final double SQUARE_ROOT_TWO = Math.sqrt(2.0d);

    /* loaded from: classes.dex */
    public interface Listener {
        void onPositionChanged();
    }

    /* loaded from: classes.dex */
    public static class Point implements Parcelable {
        public static final Parcelable.Creator<Point> CREATOR = new Parcelable.Creator<Point>() { // from class: im.delight.android.location.SimpleLocation.Point.1
            @Override // android.os.Parcelable.Creator
            public Point createFromParcel(Parcel in) {
                return new Point(in);
            }

            @Override // android.os.Parcelable.Creator
            public Point[] newArray(int size) {
                return new Point[size];
            }
        };
        public final double latitude;
        public final double longitude;

        public Point(double lat, double lon) {
            this.latitude = lat;
            this.longitude = lon;
        }

        public String toString() {
            return "(" + this.latitude + ", " + this.longitude + ")";
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            out.writeDouble(this.latitude);
            out.writeDouble(this.longitude);
        }

        private Point(Parcel in) {
            this.latitude = in.readDouble();
            this.longitude = in.readDouble();
        }
    }

    public SimpleLocation(Context context) {
        this(context, false);
    }

    public SimpleLocation(Context context, boolean requireFine) {
        this(context, requireFine, false);
    }

    public SimpleLocation(Context context, boolean requireFine, boolean passive) {
        this(context, requireFine, passive, INTERVAL_DEFAULT);
    }

    public SimpleLocation(Context context, boolean requireFine, boolean passive, long interval) {
        this(context, requireFine, passive, interval, false);
    }

    public SimpleLocation(Context context, boolean requireFine, boolean passive, long interval, boolean requireNewLocation) {
        this.mLocationManager = (LocationManager) context.getApplicationContext().getSystemService("location");
        this.mRequireFine = requireFine;
        this.mPassive = passive;
        this.mInterval = interval;
        this.mRequireNewLocation = requireNewLocation;
        if (!requireNewLocation) {
            this.mPosition = getCachedPosition();
            cachePosition();
        }
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public boolean hasLocationEnabled() {
        return hasLocationEnabled(getProviderName());
    }

    private boolean hasLocationEnabled(String providerName) {
        try {
            return this.mLocationManager.isProviderEnabled(providerName);
        } catch (Exception e) {
            return false;
        }
    }

    public void beginUpdates() {
        if (this.mLocationListener != null) {
            endUpdates();
        }
        if (!this.mRequireNewLocation) {
            this.mPosition = getCachedPosition();
        }
        this.mLocationListener = createLocationListener();
        this.mLocationManager.requestLocationUpdates(getProviderName(), this.mInterval, 0.0f, this.mLocationListener);
    }

    public void endUpdates() {
        LocationListener locationListener = this.mLocationListener;
        if (locationListener != null) {
            this.mLocationManager.removeUpdates(locationListener);
            this.mLocationListener = null;
        }
    }

    private Location blurWithRadius(Location originalLocation) {
        if (this.mBlurRadius <= 0) {
            return originalLocation;
        }
        Location newLocation = new Location(originalLocation);
        double d = SQUARE_ROOT_TWO;
        double blurMeterLong = calculateRandomOffset(this.mBlurRadius) / d;
        double blurMeterLat = calculateRandomOffset(this.mBlurRadius) / d;
        newLocation.setLongitude(newLocation.getLongitude() + meterToLongitude(blurMeterLong, newLocation.getLatitude()));
        newLocation.setLatitude(newLocation.getLatitude() + meterToLatitude(blurMeterLat));
        return newLocation;
    }

    private static int calculateRandomOffset(int radius) {
        return mRandom.nextInt((radius + 1) * 2) - radius;
    }

    public Point getPosition() {
        Location location = this.mPosition;
        if (location == null) {
            return null;
        }
        Location position = blurWithRadius(location);
        return new Point(position.getLatitude(), position.getLongitude());
    }

    public double getLatitude() {
        Location location = this.mPosition;
        if (location == null) {
            return 0.0d;
        }
        Location position = blurWithRadius(location);
        return position.getLatitude();
    }

    public double getLongitude() {
        Location location = this.mPosition;
        if (location == null) {
            return 0.0d;
        }
        Location position = blurWithRadius(location);
        return position.getLongitude();
    }

    public long getTimestampInMilliseconds() {
        Location location = this.mPosition;
        if (location == null) {
            return 0L;
        }
        return location.getTime();
    }

    public long getElapsedTimeInNanoseconds() {
        if (this.mPosition == null) {
            return 0L;
        }
        if (Build.VERSION.SDK_INT >= 17) {
            return this.mPosition.getElapsedRealtimeNanos();
        }
        return ((SystemClock.elapsedRealtime() + getTimestampInMilliseconds()) - System.currentTimeMillis()) * 1000000;
    }

    public float getSpeed() {
        Location location = this.mPosition;
        if (location == null) {
            return 0.0f;
        }
        return location.getSpeed();
    }

    public double getAltitude() {
        Location location = this.mPosition;
        if (location == null) {
            return 0.0d;
        }
        return location.getAltitude();
    }

    public void setBlurRadius(int blurRadius) {
        this.mBlurRadius = blurRadius;
    }

    private LocationListener createLocationListener() {
        return new LocationListener() { // from class: im.delight.android.location.SimpleLocation.1
            @Override // android.location.LocationListener
            public void onLocationChanged(Location location) {
                SimpleLocation.this.mPosition = location;
                SimpleLocation.this.cachePosition();
                if (SimpleLocation.this.mListener != null) {
                    SimpleLocation.this.mListener.onPositionChanged();
                }
            }

            @Override // android.location.LocationListener
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override // android.location.LocationListener
            public void onProviderEnabled(String provider) {
            }

            @Override // android.location.LocationListener
            public void onProviderDisabled(String provider) {
            }
        };
    }

    private String getProviderName() {
        return getProviderName(this.mRequireFine);
    }

    private String getProviderName(boolean requireFine) {
        if (requireFine) {
            return this.mPassive ? PROVIDER_FINE_PASSIVE : PROVIDER_FINE;
        } else if (hasLocationEnabled(PROVIDER_COARSE)) {
            if (!this.mPassive) {
                return PROVIDER_COARSE;
            }
            throw new RuntimeException("There is no passive provider for the coarse location");
        } else if (!hasLocationEnabled(PROVIDER_FINE) && !hasLocationEnabled(PROVIDER_FINE_PASSIVE)) {
            return PROVIDER_COARSE;
        } else {
            return getProviderName(true);
        }
    }

    private Location getCachedPosition() {
        Location location = mCachedPosition;
        if (location != null) {
            return location;
        }
        try {
            return this.mLocationManager.getLastKnownLocation(getProviderName());
        } catch (Exception e) {
            return null;
        }
    }

    public void cachePosition() {
        Location location = this.mPosition;
        if (location != null) {
            mCachedPosition = location;
        }
    }

    public static void openSettings(Context context) {
        context.startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
    }

    public static double latitudeToKilometer(double latitude) {
        return 111.13300323486328d * latitude;
    }

    public static double kilometerToLatitude(double kilometer) {
        return kilometer / latitudeToKilometer(1.0d);
    }

    public static double latitudeToMeter(double latitude) {
        return latitudeToKilometer(latitude) * 1000.0d;
    }

    public static double meterToLatitude(double meter) {
        return meter / latitudeToMeter(1.0d);
    }

    public static double longitudeToKilometer(double longitude, double latitude) {
        return 111.31999969482422d * longitude * Math.cos(Math.toRadians(latitude));
    }

    public static double kilometerToLongitude(double kilometer, double latitude) {
        return kilometer / longitudeToKilometer(1.0d, latitude);
    }

    public static double longitudeToMeter(double longitude, double latitude) {
        return longitudeToKilometer(longitude, latitude) * 1000.0d;
    }

    public static double meterToLongitude(double meter, double latitude) {
        return meter / longitudeToMeter(1.0d, latitude);
    }

    public static double calculateDistance(Point start, Point end) {
        return calculateDistance(start.latitude, start.longitude, end.latitude, end.longitude);
    }

    public static double calculateDistance(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        float[] results = new float[3];
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
        return results[0];
    }
}
