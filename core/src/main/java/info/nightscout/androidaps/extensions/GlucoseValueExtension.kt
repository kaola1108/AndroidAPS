package info.nightscout.androidaps.extensions

import info.nightscout.androidaps.Constants
import info.nightscout.androidaps.database.entities.GlucoseValue
import info.nightscout.androidaps.utils.DateUtil
import info.nightscout.androidaps.utils.DecimalFormatter
import org.json.JSONObject

fun GlucoseValue.valueToUnits(units: String): Double =
    if (units == Constants.MGDL) value
    else value * Constants.MGDL_TO_MMOLL

fun GlucoseValue.valueToUnitsString(units: String): String =
    if (units == Constants.MGDL) DecimalFormatter.to0Decimal(value)
    else DecimalFormatter.to1Decimal(value * Constants.MGDL_TO_MMOLL)

fun GlucoseValue.toJson(dateUtil: DateUtil): JSONObject =
    JSONObject()
        .put("device", sourceSensor.text)
        .put("date", timestamp)
        .put("dateString", dateUtil.toISOString(timestamp))
        .put("sgv", value)
        .put("direction", trendArrow.text)
        .put("type", "sgv").also {
            if (interfaceIDs.nightscoutId != null) it.put("_id", interfaceIDs.nightscoutId)
        }
