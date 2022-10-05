package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.color.DynamicColors
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply dynamic color
        DynamicColors.applyToActivitiesIfAvailable(application)

//        binding = DataBindingUtil.setContentView(
//            this,
//            R.layout.activity_reminder_description
//        )
        binding = ActivityReminderDescriptionBinding.inflate(layoutInflater)

        //https://stackoverflow.com/questions/73019160/android-getparcelableextra-deprecated
        binding.reminderDataItem =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(EXTRA_ReminderDataItem, ReminderDataItem::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(EXTRA_ReminderDataItem)
            }


        setContentView(binding.root)

        //https://stackoverflow.com/questions/3323074/android-difference-between-parcelable-and-serializable
        //https://stackoverflow.com/questions/72571804/getserializableextra-deprecated-what-is-the-alternative
//        binding.reminderDataItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            intent.getSerializableExtra(EXTRA_ReminderDataItem, ReminderDataItem::class.java)
//        } else {
//            @Suppress("DEPRECATION")
//            intent.getParcelableExtra(EXTRA_ReminderDataItem)
//        }

//        TODO: Add the implementation of the reminder details -> done
    }
}
