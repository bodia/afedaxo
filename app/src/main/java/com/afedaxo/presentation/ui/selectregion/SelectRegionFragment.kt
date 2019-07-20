package com.afedaxo.presentation.ui.selectregion

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.afedaxo.R
import com.afedaxo.databinding.FragmentSelectRegionBinding
import com.afedaxo.model.room.DishEntity
import com.afedaxo.presentation.ui.base.BaseFragment
import kotlinx.android.synthetic.main.dialog_dish_detected.view.*
import kotlinx.android.synthetic.main.fragment_select_region.*
import kotlinx.android.synthetic.main.fragment_select_region.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel
import java.text.DecimalFormat

class SelectRegionFragment : BaseFragment() {

    lateinit var binding: FragmentSelectRegionBinding

    val viewModel : SelectRegionViewModel by viewModel()

    fun navigateToFoodList(dish: DishEntity) {

    }

    fun initViewWithUri(fromFile: Uri) {
        cropImageView.setImageUriAsync(fromFile)
    }

    fun showDishNotDetectedToast() {
        Toast.makeText(activity, getString(R.string.select_with_price), Toast.LENGTH_SHORT)
            .show()
    }

    fun showRetakePhotoButton() {
        ac_sr_retake_btn.visibility = View.VISIBLE
    }


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_select_region, container, false
        )

        binding.viewModel = viewModel

        viewModel.dishDetectionResult.observe(this, Observer<DishDetectionResult> {
            if (it.status == DishDetectionResult.Status.FAILURE) {
                showRetakePhotoButton()
                showDishNotDetectedToast()
            }
            else {
                it.detectedDish?.let { it1 -> dishDetected(it1) }
            }
        })

        viewModel.photoInit.observe(this, Observer<Uri> {
            initViewWithUri(it)
        })

//        viewModel.initCropView(
//            intent.extras.getString(SelectRegionActivity.FULL_FILENAME))

                binding.root.ac_sr_retake_btn.setOnClickListener {
            //viewModel.onRetakePhoto(intent.extras.getString(SelectRegionActivity.FULL_FILENAME))
        }

                binding.root.ac_sr_select_btn.setOnClickListener {
            //            viewModel.onSelectRegion(cropImageView.getCroppedImage(),
//                intent.extras.getString(SelectRegionActivity.FULL_FILENAME), intent.extras.getInt(SESSION_ID))
        }

            return binding.root
    }

    @UiThread
    fun dishDetected(dish: DishEntity) {
        val regex = "[0-9]+[.,]?[0-9]?[0-9]?".toRegex()

        val mDialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_dish_detected, null)
        // Initialize a new instance of
        val builder = AlertDialog.Builder(activity!!)
            .setView(mDialogView)

        val regexVal = regex.find(dish.priceVal)?.value

        var dialog: AlertDialog? = null

        if (regexVal != null) {
            val priceFloat = regexVal
                .replace(',', '.').toFloat()

            val df = DecimalFormat("###,###,##0.00")
            val formatted = df.format(priceFloat)

            // Display a message on alert dialog
            mDialogView.dialog_dishdtc_val.setText(
                getString(R.string.dish_detect_dialog_with_price, formatted)

            )

            // Set a positive button and its click listener on alert dialog
            mDialogView.dialog_dishdtc_confirm_btn.setOnClickListener {
                lifecycleScope.launch(Dispatchers.Main) {
                    viewModel.dishConfirmed(dish)
                }
            }


            // Display a negative button on alert dialog
            mDialogView.dialog_dishdtc_selectag_btn.setOnClickListener  {
                lifecycleScope.launch(Dispatchers.Main) {
                    viewModel.disposeDish(dish);
                    dialog?.dismiss()
                }
            }

            dialog = builder.show()
            dialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

    }

    fun onBackPressed() {
        //startActivity(PhotoTakingActivity.getIntent(this, intent.extras.getInt(SESSION_ID)))
    }
}