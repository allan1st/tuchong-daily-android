package uk.co.imallan.tuchongdaily.ui.fragment;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import uk.co.imallan.tuchongdaily.R;
import uk.co.imallan.tuchongdaily.db.Table;
import uk.co.imallan.tuchongdaily.provider.ImageProvider;
import uk.co.imallan.tuchongdaily.provider.PostProvider;
import uk.co.imallan.tuchongdaily.ui.adapter.PostImagesRecyclerViewAdapter;
import uk.co.imallan.tuchongdaily.utils.ImageUtils;

/**
 * Created by allan on 15/2/25.
 */
public class PostFragment extends AbstractFragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

	public static final String POST_SERVER_ID = "POST_SERVER_ID";

	private static final int LOADER_POST = 1;

	private static final int LOADER_POST_IMAGES = 2;

	private String mServerId;

	private View mRootView;

	private View mPostInfoContainer;

	private TextView mTitleTextView;

	private ImageView mImage;

	public RecyclerView mRecyclerView;

	public PostImagesRecyclerViewAdapter mAdapter;

	private View mGalleryButton;

	private boolean isGalleryShown = false;

	private long mLastClickTimestamp = 0;

	private boolean isSingleImagePost = false;

	private String mTitleStr;

	private String mPostURL;

	private String mImageUrl;

	private String mCameraInfo;

	private String mLensInfo;

	private ImageView mAuthorImage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arguments = getArguments();
		mServerId = arguments.getString(POST_SERVER_ID);
	}

	@Override
	protected boolean prepareServiceReceiver() {
		return false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_post, container, false);
		initElements();
		initTransitions();
		return mRootView;
	}

	private void initElements() {
		mTitleTextView = (TextView) mRootView.findViewById(R.id.text_post_title);
		mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_post_images);
		mImage = (ImageView) mRootView.findViewById(R.id.image_post);
		mPostInfoContainer = mRootView.findViewById(R.id.container_post_info);
		mGalleryButton = mRootView.findViewById(R.id.button_show_gallery);
		mAuthorImage = (ImageView) mRootView.findViewById(R.id.image_post_author);
	}

	private void initTransitions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		}
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mGalleryButton.setOnClickListener(this);
		mImage.setOnClickListener(this);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
		mAdapter = new PostImagesRecyclerViewAdapter(getActivity());
		mRecyclerView.setAdapter(mAdapter);
		getLoaderManager().initLoader(LOADER_POST_IMAGES, null, this);
		getLoaderManager().initLoader(LOADER_POST, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
			case LOADER_POST_IMAGES:
				return new CursorLoader(getActivity(), ImageProvider.uriPostImages(mServerId), null, null, null, null);
			case LOADER_POST:
				return new CursorLoader(getActivity(), PostProvider.uriPost(mServerId), null, null, null, null);
			default:
				return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
			case LOADER_POST_IMAGES:
				boolean isImageSet = false;
				for (int i = 0; i < data.getCount(); i++) {
					if (data.moveToPosition(i)) {
						final int width = data.getInt(data.getColumnIndex(Table.Image.COLUMN_WIDTH));
						final int height = data.getInt(data.getColumnIndex(Table.Image.COLUMN_HEIGHT));
						if (width < height) {
							//portrait mode
							setPosterImage(data, i);
							isImageSet = true;
							break;
						}
					}
				}
				if (!isImageSet) {
					setPosterImage(data, 0);
				}
				mGalleryButton.setVisibility(View.VISIBLE);
				isSingleImagePost = false;
				mAdapter.swapCursor(data);
				break;
			case LOADER_POST:
				if (data.moveToPosition(0)) {
					mTitleStr = data.getString(data.getColumnIndex(Table.Post.COLUMN_TITLE));
					mTitleTextView.setText(mTitleStr);
					Picasso.with(getActivity())
							.load(data.getString(data.getColumnIndex(Table.Author.COLUMN_ICON)))
							.fit()
							.centerCrop()
							.into(mAuthorImage);
					mPostURL = data.getString(data.getColumnIndex(Table.Post.COLUMN_URL));
				}
				break;
		}
	}

	private void setPosterImage(Cursor data, int position) {
		if (data != null && data.moveToPosition(position)) {
			final String url = data.getString(data.getColumnIndex(Table.Image.COLUMN_URL_FULL));
			this.mImageUrl = url;
			Picasso.with(getActivity()).load(url)
					.transform(new ImageUtils.LimitImageSizeTransformation(ImageUtils.LimitImageSizeTransformation.QUALITY.QUALITY_1440P))
					.fit().centerCrop()
					.into(mImage);
			mCameraInfo = data.getString(data.getColumnIndex(Table.Image.COLUMN_CAMERA));
			mLensInfo = data.getString(data.getColumnIndex(Table.Image.COLUMN_LENS));
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (!TextUtils.isEmpty(mImageUrl)) {
			Picasso.with(getActivity()).load(mImageUrl)
					.transform(new ImageUtils.LimitImageSizeTransformation(ImageUtils.LimitImageSizeTransformation.QUALITY.QUALITY_1440P))
					.fit().centerCrop()
					.into(mImage);
		}
	}

	private void toggleGalleryRecyclerView() {
		if (isGalleryShown) {
			ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, mRecyclerView.getHeight());
			valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					mPostInfoContainer.setTranslationY((Float) animation.getAnimatedValue());
				}
			});
			valueAnimator.start();
			isGalleryShown = false;
		} else {
			ValueAnimator valueAnimator = ValueAnimator.ofFloat(mRecyclerView.getHeight(), 0);
			valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					mPostInfoContainer.setTranslationY((Float) animation.getAnimatedValue());
				}
			});
			valueAnimator.start();
			isGalleryShown = true;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	public void onPageScrollFrom(float positionOffset, int positionOffsetPixels) {
		mImage.setTranslationX(positionOffsetPixels / 1.2f);
	}

	public void onPageScrollto(float positionOffset, int positionOffsetPixels) {
		mImage.setTranslationX((positionOffsetPixels - mImage.getWidth()) / 1.2f);
	}

	public void onPageSelected() {
		mImage.setTranslationX(0);
	}

	@Override
	public void onClick(View v) {
		//prevent multiple clicks
		if (System.currentTimeMillis() - mLastClickTimestamp <= 500) {
			return;
		} else {
			mLastClickTimestamp = System.currentTimeMillis();
		}
		switch (v.getId()) {
			case R.id.image_post:
				toggleGalleryRecyclerView();
				break;
			case R.id.button_show_gallery:
				toggleGalleryRecyclerView();
				break;
		}
	}

	public void share() {
		if (!TextUtils.isEmpty(mPostURL)) {
			Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_TEXT, mTitleStr + " " + mPostURL);
			sendIntent.setType("text/plain");
			startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.action_share)));
		}
	}

	public void openInBrowser() {
		if (!TextUtils.isEmpty(mPostURL)) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(mPostURL));
			startActivity(intent);
		}
	}

}
