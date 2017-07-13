/*
 * Copyright 2013 Roman Nurik, Tim Roes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nullify.travi;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

public final class SwipeDismissList implements View.OnTouchListener {
	
	// Cached ViewConfiguration and system-wide constant values
	private int mSlop;
	private int mMinFlingVelocity;
	private int mMaxFlingVelocity;
	private long mAnimationTime;
	
	// Fixed properties
	private AbsListView mListView;
	private OnDismissCallback mCallback;
	private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero
	
	// Transient properties
	private SortedSet<PendingDismissData> mPendingDismisses = new TreeSet<PendingDismissData>();
	private int mDismissAnimationRefCount = 0;
	private float mDownX;
	private boolean mSwiping;
	private VelocityTracker mVelocityTracker;
	private int mDownPosition;
	private View mDownView;
	private boolean mPaused;
	private float mDensity;
	private boolean mSwipeDisabled;

	private UndoMode mMode;
	private List<Undoable> mUndoActions;
	private Handler mHandler;

	private PopupWindow mUndoPopup;
	private TextView mUndoText;
	private RelativeLayout mUndoButton;

	private SwipeDirection mSwipeDirection = SwipeDirection.BOTH;
	private int mAutoHideDelay = 5000;
	private String mDeleteString = "Item deleted";
	private String mDeleteMultipleString = "%d items deleted";
	private boolean mTouchBeforeAutoHide = true;

	private int mDelayedMsgId;


	public enum UndoMode {
		SINGLE_UNDO,

		MULTI_UNDO,

		COLLAPSED_UNDO
	};

	public enum SwipeDirection {
		BOTH,
		START,
		END
	}


	public interface OnDismissCallback {

		Undoable onDismiss(AbsListView listView, int position);
	}

	public abstract static class Undoable {


		public String getTitle() {
			return null;
		}


		public abstract void undo();


		public void discard() { }
		
	}


	public SwipeDismissList(AbsListView listView, OnDismissCallback callback) {
		this(listView, callback, UndoMode.SINGLE_UNDO);
	}


	public SwipeDismissList(AbsListView listView, OnDismissCallback callback, UndoMode mode) {

		if(listView == null) {
			throw new IllegalArgumentException("listview must not be null.");
		}
		mHandler = new HideUndoPopupHandler();
		mListView = listView;
		mCallback = callback;
		mMode = mode;
		
		ViewConfiguration vc = ViewConfiguration.get(listView.getContext());
		mSlop = vc.getScaledTouchSlop();
		mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
		mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
		mAnimationTime = listView.getContext().getResources().getInteger(
			android.R.integer.config_shortAnimTime);

		mDensity = mListView.getResources().getDisplayMetrics().density;

		// -- Load undo popup --
		LayoutInflater inflater = (LayoutInflater) mListView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.undo_popup, null);
		mUndoButton = (RelativeLayout)v.findViewById(R.id.undo);
		mUndoButton.setOnClickListener(new UndoHandler());
		mUndoButton.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				mDelayedMsgId++;
				return false;
			}
		});
		mUndoText = (TextView)v.findViewById(R.id.text);
		
		mUndoPopup = new PopupWindow(v);
		mUndoPopup.setAnimationStyle(R.style.fade_animation);
        int xdensity = (int)(mListView.getContext().getResources().getDisplayMetrics().widthPixels / mDensity);
        if(xdensity < 300) {
    		mUndoPopup.setWidth((int)(mDensity * 280));
        } else if(xdensity < 350) {
            mUndoPopup.setWidth((int)(mDensity * 300));
        } else if(xdensity < 500) {
            mUndoPopup.setWidth((int)(mDensity * 330));
        } else {
            mUndoPopup.setWidth((int)(mDensity * 450));
        }
		mUndoPopup.setHeight((int)(mDensity * 56));
		// -- END Load undo popu --

		listView.setOnTouchListener(this);
		listView.setOnScrollListener(this.makeScrollListener());

		switch(mode) {
			case SINGLE_UNDO:
				mUndoActions = new ArrayList<Undoable>(1);
				break;
			default:
				mUndoActions = new ArrayList<Undoable>(10);
				break;
		}

	}


	private void setEnabled(boolean enabled) {
		mPaused = !enabled;
	}


	public void setAutoHideDelay(int delay) {
		mAutoHideDelay = delay;
	}


	public void setSwipeDirection(SwipeDirection direction) {
		mSwipeDirection = direction;
	}


	public void setUndoString(String msg) {
		mDeleteString = msg;
	}


	public void setUndoMultipleString(String msg) {
		mDeleteMultipleString = msg;
	}
	

	public void setRequireTouchBeforeDismiss(boolean require) {
		mTouchBeforeAutoHide = require;
	}


    public void discardUndo() {
        for(Undoable undoable : mUndoActions) {
            undoable.discard();
        }
        mUndoActions.clear();
        mUndoPopup.dismiss();
    }


	private AbsListView.OnScrollListener makeScrollListener() {
		return new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int scrollState) {
				setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
			}

			@Override
			public void onScroll(AbsListView absListView, int i, int i1, int i2) {
			}
		};
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (this.mSwipeDisabled) {
			return false;
		}
				
		if (mViewWidth < 2) {
			mViewWidth = mListView.getWidth();
		}

		switch (motionEvent.getActionMasked()) {
			case MotionEvent.ACTION_DOWN: {
				if (mPaused) {
					return false;
				}
				Rect rect = new Rect();
				int childCount = mListView.getChildCount();
				int[] listViewCoords = new int[2];
				mListView.getLocationOnScreen(listViewCoords);
				int x = (int) motionEvent.getRawX() - listViewCoords[0];
				int y = (int) motionEvent.getRawY() - listViewCoords[1];
				View child;
				for (int i = 0; i < childCount; i++) {
					child = mListView.getChildAt(i);
					child.getHitRect(rect);
					if (rect.contains(x, y)) {
						mDownView = child;
						break;
					}
				}

				if (mDownView != null) {
					mDownX = motionEvent.getRawX();
					mDownPosition = mListView.getPositionForView(mDownView);

					mVelocityTracker = VelocityTracker.obtain();
					mVelocityTracker.addMovement(motionEvent);
				}
				view.onTouchEvent(motionEvent);
				return true;
			}

			case MotionEvent.ACTION_UP: {
				if (mVelocityTracker == null) {
					break;
				}

				float deltaX = motionEvent.getRawX() - mDownX;
				mVelocityTracker.addMovement(motionEvent);
				mVelocityTracker.computeCurrentVelocity(1000);
				float velocityX = Math.abs(mVelocityTracker.getXVelocity());
				float velocityY = Math.abs(mVelocityTracker.getYVelocity());
				boolean dismiss = false;
				boolean dismissRight = false;
				if (Math.abs(deltaX) > mViewWidth / 2 && mSwiping) {
					dismiss = true;
					dismissRight = deltaX > 0;
				} else if (mMinFlingVelocity <= velocityX && velocityX <= mMaxFlingVelocity
					&& velocityY < velocityX && mSwiping && isDirectionValid(mVelocityTracker.getXVelocity())
					&& deltaX >= mViewWidth * 0.2f) {
					dismiss = true;
					dismissRight = mVelocityTracker.getXVelocity() > 0;
				}
				if (dismiss) {
					// dismiss
					final View downView = mDownView; // mDownView gets null'd before animation ends
					final int downPosition = mDownPosition;
					++mDismissAnimationRefCount;
					animate(mDownView)
						.translationX(dismissRight ? mViewWidth : -mViewWidth)
						.alpha(0)
						.setDuration(mAnimationTime)
						.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							performDismiss(downView, downPosition);
						}
					});
				} else {
					animate(mDownView)
						.translationX(0)
						.alpha(1)
						.setDuration(mAnimationTime)
						.setListener(null);
				}
				mVelocityTracker = null;
				mDownX = 0;
				mDownView = null;
				mDownPosition = ListView.INVALID_POSITION;
				mSwiping = false;
				break;
			}

			case MotionEvent.ACTION_MOVE: {
				if(mTouchBeforeAutoHide && mUndoPopup.isShowing()) {	
					// Send a delayed message to hide popup
					mHandler.sendMessageDelayed(mHandler.obtainMessage(mDelayedMsgId), 
						mAutoHideDelay);
				}
				
				if (mVelocityTracker == null || mPaused) {
					break;
				}

				mVelocityTracker.addMovement(motionEvent);
				float deltaX = motionEvent.getRawX() - mDownX;
				// Only start swipe in correct direction
				if(isDirectionValid(deltaX)) {
					if (Math.abs(deltaX) > mSlop) {
						mSwiping = true;
						mListView.requestDisallowInterceptTouchEvent(true);

						// Cancel ListView's touch (un-highlighting the item)
						MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
						cancelEvent.setAction(MotionEvent.ACTION_CANCEL
							| (motionEvent.getActionIndex()
							<< MotionEvent.ACTION_POINTER_INDEX_SHIFT));
						mListView.onTouchEvent(cancelEvent);
					}
				} else {
					// If we swiped into wrong direction, act like this was the new
					// touch down point
					mDownX = motionEvent.getRawX();
					deltaX = 0;
				}

				if (mSwiping) {
					setTranslationX(mDownView, deltaX);
					setAlpha(mDownView, Math.max(0f, Math.min(1f,
						1f - 2f * Math.abs(deltaX) / mViewWidth)));
					return true;
				}
				break;
			}
		}
		return false;
	}

	private boolean isDirectionValid(float deltaX) {

		int rtlSign = 1;
		// On API level 17 and above, check if we are in a Right-To-Left layout
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			if(mListView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
				rtlSign = -1;
			}
		}

		// Check if swipe has been done in the corret direction
		switch(mSwipeDirection) {
			default:
			case BOTH:
				return true;
			case START:
				return rtlSign * deltaX < 0;
			case END:
				return rtlSign * deltaX > 0;
		}

	}

	class PendingDismissData implements Comparable<PendingDismissData> {

		public int position;
		public View view;

		public PendingDismissData(int position, View view) {
			this.position = position;
			this.view = view;
		}

		@Override
		public int compareTo(PendingDismissData other) {
			// Sort by descending position
			return other.position - position;
		}
	}

	private void performDismiss(final View dismissView, final int dismissPosition) {
		// Animate the dismissed list item to zero-height and fire the dismiss callback when
		// all dismissed list item animations have completed. This triggers layout on each animation
		// frame; in the future we may want to do something smarter and more performant.

		final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
		final int originalHeight = dismissView.getHeight();

		ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);

		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				--mDismissAnimationRefCount;
				if (mDismissAnimationRefCount == 0) {
					// No active animations, process all pending dismisses.

					for(PendingDismissData dismiss : mPendingDismisses) {
						if(mMode == UndoMode.SINGLE_UNDO) {
							for(Undoable undoable : mUndoActions) {
								undoable.discard();
							}
							mUndoActions.clear();
						}
						Undoable undoable = mCallback.onDismiss(mListView, dismiss.position);
						if(undoable != null) {
							mUndoActions.add(undoable);
						}
						mDelayedMsgId++;
					}

					if(!mUndoActions.isEmpty()) {
						changePopupText();
						changeButtonLabel();

						// Show undo popup
						mUndoPopup.showAtLocation(mListView, 
							Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM,
							0, (int)(mDensity * 15));
						
						// Queue the dismiss only if required
						if(!mTouchBeforeAutoHide) {	
							// Send a delayed message to hide popup
							mHandler.sendMessageDelayed(mHandler.obtainMessage(mDelayedMsgId), 
								mAutoHideDelay);
						}
					}

					ViewGroup.LayoutParams lp;
					for (PendingDismissData pendingDismiss : mPendingDismisses) {
						// Reset view presentation
						setAlpha(pendingDismiss.view, 1f);
						setTranslationX(pendingDismiss.view, 0);
						lp = pendingDismiss.view.getLayoutParams();
						lp.height = originalHeight;
						pendingDismiss.view.setLayoutParams(lp);
					}

					mPendingDismisses.clear();
				}
			}
		});

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				lp.height = (Integer) valueAnimator.getAnimatedValue();
				dismissView.setLayoutParams(lp);
			}
		});

		mPendingDismisses.add(new PendingDismissData(dismissPosition, dismissView));
		animator.start();
	}

	/**
	 * Changes text in the popup depending on stored undos.
	 */
	private void changePopupText() {
		String msg = "";
		if(mUndoActions.size() > 1 && mDeleteMultipleString != null) {
			msg = String.format(mDeleteMultipleString, mUndoActions.size());
		} else if(mUndoActions.size() >= 1) {
			// Set title from single undoable or when no multiple deletion string
			// is given
			if(mUndoActions.get(mUndoActions.size() - 1).getTitle() != null) {
				msg = mUndoActions.get(mUndoActions.size() - 1).getTitle();
			} else {
				msg = mDeleteString;
			}
		}
		mUndoText.setText(msg);
	}

	private void changeButtonLabel() {
		String msg;
		if(mUndoActions.size() > 1 && mMode == UndoMode.COLLAPSED_UNDO) {
			msg = mListView.getResources().getString(R.string.undoall);
		} else {
			msg = mListView.getResources().getString(R.string.undo);
		}
	}

	/**
	 * Takes care of undoing a dismiss. This will be added as a 
	 * {@link View.OnClickListener} to the undo button in the undo popup.
	 */
	private class UndoHandler implements View.OnClickListener {

		public void onClick(View v) {
			if(!mUndoActions.isEmpty()) {
				switch(mMode) {
					case SINGLE_UNDO:
						mUndoActions.get(0).undo();
						mUndoActions.clear();
						break;
					case COLLAPSED_UNDO:
						Collections.reverse(mUndoActions);
						for(Undoable undo : mUndoActions) {
							undo.undo();	
						}
						mUndoActions.clear();
						break;
					case MULTI_UNDO:
						mUndoActions.get(mUndoActions.size() - 1).undo();
						mUndoActions.remove(mUndoActions.size() - 1);
						break;
				}
			}

			// Dismiss dialog or change text
			if(mUndoActions.isEmpty()) {
				mUndoPopup.dismiss();
			} else {
				changePopupText();
				changeButtonLabel();
			}

			mDelayedMsgId++;

		}
		
	}

	/**
	 * Handler used to hide the undo popup after a special delay.
	 */
	private class HideUndoPopupHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == mDelayedMsgId) {
				for(Undoable undo : mUndoActions) {
					undo.discard();
				}
				mUndoActions.clear();
				mUndoPopup.dismiss();

			}
		}
		
	}
	
	/**
	 * Enable/disable swipe.
	 */
	public void setSwipeDisabled(boolean disabled) {
		this.mSwipeDisabled = disabled;
	}
}
