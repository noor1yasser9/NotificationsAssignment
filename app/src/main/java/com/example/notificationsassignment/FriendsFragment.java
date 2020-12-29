package com.example.notificationsassignment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FriendsFragment extends ListFragment {

	private SelectionListener mCallback;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, MainActivity.FRIENDS));
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);

		/*
		 * This makes sure that the container activity has implemented
		 * the callback interface. If not, it throws an exception
		 */
		try {
			mCallback = (SelectionListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement SelectionListener");
		}

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}

	@Override
	public void onListItemClick(@NonNull ListView listView, @NonNull View view, int position, long id) {
		// Send the event to the host activity
		mCallback.onItemSelected(position);
	}

}
