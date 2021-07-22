package com.example.booksmart.ui.listings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.booksmart.Client;
import com.example.booksmart.helpers.EndlessRecyclerViewScrollListener;
import com.example.booksmart.helpers.ItemClickSupport;
import com.example.booksmart.R;
import com.example.booksmart.adapters.ListingAdapter;
import com.example.booksmart.models.Item;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ListingsFragment extends Fragment {

    public static final String TAG = "ListingsFragment";
    public static final int GRID_SPAN = 2;
    public static final int NEW_LISTING_FRAGMENT_REQUEST_CODE = 1;

    ListingsViewModel listingsViewModel;
    ListingDetailViewModel listingDetailViewModel;
    SwipeRefreshLayout swipeContainer;
    EndlessRecyclerViewScrollListener scrollListener;
    RecyclerView rvListings;
    ListingAdapter adapter;
    GridLayoutManager gridLayoutManager;
    FloatingActionButton btnCompose;
    ProgressBar pb;
    Boolean fragmentRecreated;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_listings, container, false);

        btnCompose = view.findViewById(R.id.btnAddListing);
        rvListings = view.findViewById(R.id.rvListing);
        pb = view.findViewById(R.id.pbLoadingListings);

        fragmentRecreated = true;

        gridLayoutManager = new GridLayoutManager(getContext(), GRID_SPAN);
        rvListings.setLayoutManager(gridLayoutManager);

        listingDetailViewModel = new ViewModelProvider(requireActivity()).get(ListingDetailViewModel.class);
        listingsViewModel = new ViewModelProvider(requireActivity()).get(ListingsViewModel.class);
        listingsViewModel.getItems().observe(getViewLifecycleOwner(), itemsUpdateObserver);

        setEndlessScrollListener();
        setSwipeContainer(view);

        btnCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goListingForm();
            }
        });

        rvListings.setVisibility(View.GONE);
        pb.setVisibility(View.VISIBLE);

        return view;
    }

    private Observer<List<Item>> itemsUpdateObserver = new Observer<List<Item>>(){
        @Override
        public void onChanged(List<Item> items) {
            if (fragmentRecreated){
                adapter = new ListingAdapter(getContext(), items);
                rvListings.setAdapter(adapter);
                scrollListener.resetState();

                fragmentRecreated = false;
                rvListings.setVisibility(View.VISIBLE);
                pb.setVisibility(View.GONE);
            }

            adapter.notifyDataSetChanged();
            scrollListener.setLoading(false);
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ItemClickSupport.addTo(rvListings).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Item item = listingsViewModel.getItem(position);
                        listingDetailViewModel.select(item);
                        goDetailView();
                    }
                }
        );
    }

    private void setEndlessScrollListener() {
        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "onLoadMore()");
                scrollListener.setLoading(true);
                listingsViewModel.fetchMoreItems();
            }
        };

        rvListings.addOnScrollListener(scrollListener);
        scrollListener.setLoading(false);
    }

    private void setSwipeContainer(View view){
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listingsViewModel.resetList();
                swipeContainer.setRefreshing(false);
                scrollListener.resetState();
                scrollListener.setLoading(false);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void goListingForm(){
        Fragment fragment = new ListingFormFragment();
        replaceFragment(fragment);
    }

    private void goDetailView(){
        Fragment fragment = new ListingDetailFragment();
        replaceFragment(fragment);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in, R.anim.slide_out_left);
        transaction.replace(R.id.nav_host_fragment_activity_main, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}