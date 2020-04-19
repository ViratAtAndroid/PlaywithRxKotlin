package au.com.abhishek.android.rxkotlin.rxbus


import android.os.Bundle
import android.view.Gravity.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import au.com.abhishek.android.rxkotlin.MainActivity
import au.com.abhishek.android.rxkotlin.R
import au.com.abhishek.android.rxkotlin.fragments.BaseFragment
import au.com.abhishek.android.rxkotlin.utils.colorInt
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx

/**
 * Created by Abhishek Pathak on 19/04/2020.
 */

class RxBusDemo_TopFragment : BaseFragment() {

    private lateinit var _rxBus: RxBus

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val layout = with(ctx) {
            frameLayout {
                lparams(height = matchParent, width = matchParent)
                backgroundColor = colorInt { R.color.green }
                textView(R.string.msg_demo_rxbus_1) {
                    lparams(gravity = CENTER_HORIZONTAL)
                    this.gravity = CENTER
                }
                button("Tap") {
                    lparams (height = dip(90), width = dip(90), gravity = CENTER)
                    backgroundResource = R.drawable.btn_round
                    textSize = 24f
                    textColor = colorInt { android.R.color.white }
                    onClick (onTapButtonClicked)
                }
            }
        }
        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        _rxBus = (activity as MainActivity).rxBusSingleton
    }

    val onTapButtonClicked = { v: View? ->
        if (_rxBus.hasObservers()){
            _rxBus.send(RxBusDemoFragment.TapEvent())
        }
    }
}
