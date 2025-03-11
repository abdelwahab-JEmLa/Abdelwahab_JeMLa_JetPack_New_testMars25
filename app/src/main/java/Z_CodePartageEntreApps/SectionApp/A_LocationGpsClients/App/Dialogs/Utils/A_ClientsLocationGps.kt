package Z_CodePartageEntreApps.SectionApp.A_LocationGpsClients.App.Dialogs.Utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val DEFAULT_LATITUDE = 36.7389350566438
const val DEFAULT_LONGITUDE = 3.1720169070695476

// Update getCurrentLocation function to be suspend
suspend fun getCurrentLocation(context: Context): Location? = withContext(Dispatchers.IO) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return@withContext locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }
    return@withContext null
}
