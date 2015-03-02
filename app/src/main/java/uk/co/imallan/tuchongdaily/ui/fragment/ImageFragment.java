package uk.co.imallan.tuchongdaily.ui.fragment;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import uk.co.imallan.tuchongdaily.R;
import uk.co.imallan.tuchongdaily.db.Table;
import uk.co.imallan.tuchongdaily.provider.ImageProvider;

/**
 * Created by allan on 15/3/1.
 */
public class ImageFragment extends AbstractFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final String IMAGE_SERVER_ID = "IMAGE_SERVER_ID";

	private static final int LOADER_REQUEST_IMAGE = 1;

	public ImageView mImage;

	private String mServerId;

	@Override
	protected boolean prepareServiceReceiver() {
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arguments = getArguments();
		mServerId = arguments.getString(IMAGE_SERVER_ID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_image, container, false);
		mImage = (ImageView) rootView.findViewById(R.id.image_view_image_framgent);
		initTransitions();
		return rootView;
	}

	private void initTransitions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			//pass
		}
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getLoaderManager().initLoader(LOADER_REQUEST_IMAGE, null, this);

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
			case LOADER_REQUEST_IMAGE:
				return new CursorLoader(getActivity(), ImageProvider.uriImage(mServerId),
						null, null, null, null);
			default:
				return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
			case LOADER_REQUEST_IMAGE:
				if (data.moveToFirst()) {
					Picasso.with(getActivity()).load(data.getString(data.getColumnIndex(Table.Image.COLUMN_URL_LARGE)))
							.noPlaceholder().noFade().into(mImage, new Callback() {
						@Override
						public void onSuccess() {
							startPostponedEnterTransition();
						}

						@Override
						public void onError() {
							startPostponedEnterTransition();
						}
					});
					break;
				}
		}
	}

	private void startPostponedEnterTransition() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getActivity().startPostponedEnterTransition();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}
}