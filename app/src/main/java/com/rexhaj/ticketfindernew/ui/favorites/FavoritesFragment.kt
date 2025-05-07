package com.rexhaj.ticketfindernew.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.rexhaj.ticketfindernew.databinding.FragmentFavoritesBinding

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val favoritesViewModel =
            ViewModelProvider(this).get(FavoritesViewModel::class.java)

        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        val root: View = binding.root


        // IF NOT SIGNED IN, DISABLE AND SHOW MESSAGE
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            binding.textViewPleaseSignIn.setVisibility(VISIBLE)
        } else {
            binding.textViewPleaseSignIn.setVisibility(GONE)
            binding.recyclerViewFavorites.setVisibility(VISIBLE)
            // TODO: POPULATE RECYCLERVIEW ADAPTER WITH FAVORITES PULLED FROM DB,
            //  AND IMPLEMENT FAVORITING AN ITEM IN RECYCLERVIEW
        }

        //val textView: TextView = binding.textFavorites
        //favoritesViewModel.text.observe(viewLifecycleOwner) {
        //    textView.text = it
        //}
        return root
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}