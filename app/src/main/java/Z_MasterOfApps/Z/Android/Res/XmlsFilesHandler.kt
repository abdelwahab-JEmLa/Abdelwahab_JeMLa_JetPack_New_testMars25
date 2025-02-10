package Z_MasterOfApps.Z.Android.Res

import com.example.abdelwahabjemlajetpack.R

/*
import com.example.clientjetpack.R

import com.example.c_serveur.R

*/

class XmlsFilesHandler {
    companion object {
        val xmlResources = listOf(
            Pair("marker_info_window", R.layout.marker_info_window),
            Pair("info_window_container", R.id.info_window_container),
            Pair("location_arrow", R.drawable.location_arrow),
            Pair("reacticonanimatedjsonurl", R.raw.reacticonanimatedjsonurl)  // Fixed mapping
        )

        fun fixXmlResources(name: String): Int {
            return xmlResources.find { it.first == name }?.second
                ?: throw IllegalStateException("Resource '$name' not found")  // Better error message
        }
    }
}
