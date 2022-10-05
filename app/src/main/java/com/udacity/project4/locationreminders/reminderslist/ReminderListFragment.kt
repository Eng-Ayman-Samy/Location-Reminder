package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {
    //use Koin to retrieve the ViewModel instance
    override val baseViewModel: RemindersListViewModel by sharedViewModel()
    private lateinit var binding: FragmentRemindersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRemindersBinding.inflate(layoutInflater)

        binding.viewModel = baseViewModel

        //navigate to login screen after logout
//        UserAuthenticationState.observe(viewLifecycleOwner) {
//            if (it == null)
//                findNavController().navigate(
//                    ReminderListFragmentDirections.actionReminderListFragmentToAuthenticationFragment()
//                )
//        }

//        UserAuthenticationState.observe(viewLifecycleOwner) {
//            if (it == null)
//                requireActivity().finishWithLaunch(AuthenticationActivity::class.java)
//        }


        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.logout -> {
                        AuthUI.getInstance().signOut(requireContext())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful)
                                    baseViewModel.navigationCommand.value =
                                        NavigationCommand.To(ReminderListFragmentDirections.actionReminderListFragmentToAuthenticationFragment())
//                                    findNavController().navigate(
//                                        ReminderListFragmentDirections.actionReminderListFragmentToAuthenticationFragment()
//                                    )
                                else {
                                    baseViewModel.showErrorMessage.value =
                                        getString(R.string.logout_failed)
                                }
                            }
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        //setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener {
            baseViewModel.loadReminders()
            binding.refreshLayout.isRefreshing = false
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
//        binding.refreshLayout.setOnRefreshListener {
//            baseViewModel.loadReminders()
//            binding.refreshLayout.isRefreshing = false
//        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        baseViewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        baseViewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {}

//        setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.logout -> {
//                AuthUI.getInstance().signOut(requireContext())
//                findNavController().navigate(
//                    ReminderListFragmentDirections.actionReminderListFragmentToAuthenticationFragment()
//                )
//            }
//        }
//        return super.onOptionsItemSelected(item)
//
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
////        display logout as menu item
//        inflater.inflate(R.menu.main_menu, menu)
//    }

}
