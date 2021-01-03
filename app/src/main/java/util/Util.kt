package util

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.LifecycleEventObserver
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
    FragmentViewBindingDelegate(this, viewBindingFactory)

//https://medium.com/@Zhuinden/simple-one-liner-viewbinding-in-fragments-and-activities-with-kotlin-961430c6c07c
class FragmentViewBindingDelegate<T : ViewBinding>(
    private val fragment: Fragment,
    private val viewBindingFactory: (View) -> T,
) : ReadOnlyProperty<Fragment, T> {
    private var binding: T? = null

    init {
        fragment.lifecycle.addObserver(LifecycleEventObserver leo1@{ _, event ->
            if (event != Event.ON_CREATE) return@leo1
            fragment.viewLifecycleOwnerLiveData.observe(fragment, { viewLifecycleOwner ->
                viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver leo2@{ _, event ->
                    if (event != ON_DESTROY) return@leo2
                    binding = null
                })
            })
        })

    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return binding ?: run {
            val lifecycle = fragment.viewLifecycleOwner.lifecycle
            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
                throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
            }
            viewBindingFactory(thisRef.requireView()).also { this.binding = it }
        }
    }
}