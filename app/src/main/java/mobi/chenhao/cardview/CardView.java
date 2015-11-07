package mobi.chenhao.cardview;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

public class CardView extends FrameLayout {
	
	private static final int UNIT = 1000; // 计算速率的单位（毫秒）
	private static float MAX_DEGREE = 15;
	private static final int ITEM_SPACE = 40;
	private static float FLING_VELOCITY = 500;
	private static final int DEF_MAX_VISIBLE = 4;
	private static float OUT_DISTANCE_BOUDARY;
	private static float OUT_DISTANCE_BOUDARX;
	
	private Rect topRect;
	private int dataSize;
	private boolean isFirst;
	private float mTouchSlop,alpha;
	private VelocityTracker vTracker;
	private ListAdapter mListAdapter;
	private Spring tranYSpring,tranXSpring,rotateSpring;
	private OnCardViewListener mListener;
	private SparseArray<View> viewHolder = new SparseArray<View>();
	private final BaseSpringSystem mSpringSystem = SpringSystem.create();
	private final ExampleSpringListener mSpringListener = new ExampleSpringListener();
	private int itemSpace = ITEM_SPACE,topPosition,mNextAdapterPosition,
			mHeight, mWidth,itemTop,itemLeft;
	
	public interface OnCardViewListener {
		void onCardSelected(int position);
		void onCardLoadMore();
		void onClick(View v);
	}
	
	public CardView(Context context) {
		super(context);
		init(context);
	}

	public CardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		topRect = new Rect();
		tranYSpring = mSpringSystem.createSpring();
		tranXSpring = mSpringSystem.createSpring();
		rotateSpring = mSpringSystem.createSpring();
		tranYSpring.addListener(mSpringListener);
		tranXSpring.addListener(mSpringListener);
		rotateSpring.addListener(mSpringListener);
		ViewConfiguration configuration = ViewConfiguration.get(context);
		//FLING_VELOCITY = configuration.getScaledMaximumFlingVelocity();
		mTouchSlop = configuration.getScaledTouchSlop();
		setUnSelectCardAlpha(1f);
		isFirst=true;
	}
	
	public void setUnSelectCardAlpha(float f){
		if (f<0.3f) {
			alpha=0.3f;
		} else if(f>1f){
			alpha=1f;
		}else {
			alpha=f;
		}
	}
	
	public int getMaxVisibleCount() {
		return DEF_MAX_VISIBLE;
	}

	public void setItemSpace(int itemSpace) {
		this.itemSpace = itemSpace;
	}

	public int getItemSpace() {
		return itemSpace;
	}

	public ListAdapter getAdapter() {
		return mListAdapter;
	}

	public void setAdapter(ListAdapter adapter,int reDataSize) {
		dataSize=reDataSize;
		if (null!=adapter) {
			mNextAdapterPosition = 0;
			mListAdapter = adapter;
			viewHolder.clear();
			if (isFirst) {
				isFirst=false;
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						removeAllViews();
						ensureFull();
					}
				}, 1000);
			} else {
				removeAllViews();
				ensureFull();
			}
		}
	}

	public void setOnCardViewListener(OnCardViewListener listener) {
		mListener = listener;
	}
	
	private void backFull() {
		int h=getResources().getDisplayMetrics().heightPixels;
		int w=getResources().getDisplayMetrics().widthPixels;
		int itemH=(int)(h*0.68);
		int itemW=(int)(itemH*0.80);
		if (itemW>=w){
			itemW=(int)(w*0.80);
			itemH=(int)(itemW*1.2);
		}
		itemSpace=(int)(10*getResources().getDisplayMetrics().density);
		int top=(getMeasuredHeight()-itemH-(DEF_MAX_VISIBLE*itemSpace))/2;
		if (top<=0) {
			top=(h-itemH-(DEF_MAX_VISIBLE*itemSpace))/2;
		}
		itemTop=(DEF_MAX_VISIBLE - 1) * itemSpace+top;
		itemLeft=(getResources().getDisplayMetrics().widthPixels-itemW)/2;
		int viewIndex=0;
		while (mNextAdapterPosition < mListAdapter.getCount()&& getChildCount() < DEF_MAX_VISIBLE) {
			View view = mListAdapter.getView(mNextAdapterPosition,viewHolder.get(viewIndex), this);
			view.setOnClickListener(null);
			viewHolder.put(viewIndex, view);
			ViewHelper.setScaleX(view,((DEF_MAX_VISIBLE - viewIndex - 1) / (float) DEF_MAX_VISIBLE) * 0.2f + 0.8f);
			int topMargin = (DEF_MAX_VISIBLE - viewIndex - 1) * itemSpace;
			ViewHelper.setAlpha(view, viewIndex == 0 ? 1f :alpha);
			ViewHelper.setTranslationY(view, topMargin+top);
			ViewHelper.setTranslationX(view, itemLeft);
			LayoutParams params = (LayoutParams) view.getLayoutParams();
			if (params == null) {
				params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			}
			params.width=itemW;
			params.height=itemH;
			params.gravity=Gravity.LEFT|Gravity.TOP;
			addViewInLayout(view, 0, params);
			mNextAdapterPosition += 1;
			viewIndex+=1;
		}
	}

	private void ensureFull() {
		int h=getResources().getDisplayMetrics().heightPixels;
		int w=getResources().getDisplayMetrics().widthPixels;
		int itemH=(int)(h*0.68);
		int itemW=(int)(itemH*0.80);
		if (itemW>=w){
			itemW=(int)(w*0.80);
			itemH=(int)(itemW*1.2);
		}
		itemSpace=(int)(10*getResources().getDisplayMetrics().density);
		int top=(getMeasuredHeight()-itemH-(DEF_MAX_VISIBLE*itemSpace))/2;
		if (top<=0) {
			top=(h-itemH-(DEF_MAX_VISIBLE*itemSpace))/2;
		}
		itemTop=(DEF_MAX_VISIBLE - 1) * itemSpace+top;
		itemLeft=(getResources().getDisplayMetrics().widthPixels-itemW)/2;
		while (mNextAdapterPosition < mListAdapter.getCount()&& getChildCount() < DEF_MAX_VISIBLE) {
			int index = mNextAdapterPosition % DEF_MAX_VISIBLE;
			View view = mListAdapter.getView(mNextAdapterPosition,viewHolder.get(index), this);
			view.setOnClickListener(null);
			viewHolder.put(index, view);
			// 添加剩余的View时，始终处在最后
			index = Math.min(mNextAdapterPosition, DEF_MAX_VISIBLE - 1);
			ViewHelper.setScaleX(view,((DEF_MAX_VISIBLE - index - 1) / (float) DEF_MAX_VISIBLE) * 0.2f + 0.8f);
			int topMargin = (DEF_MAX_VISIBLE - index - 1) * itemSpace;
			ViewHelper.setTranslationY(view, topMargin+top);
			ViewHelper.setTranslationX(view, itemLeft);
			ViewHelper.setAlpha(view, mNextAdapterPosition == 0 ? 1f :alpha);
			LayoutParams params = (LayoutParams) view.getLayoutParams();
			if (params == null) {
				params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			}
			params.width=itemW;
			params.height=itemH;
			params.gravity=Gravity.LEFT|Gravity.TOP;
			addViewInLayout(view, 0, params);
			mNextAdapterPosition += 1;
		}
	}

	/**
	 * 重置spring
	 */
	private void resetSpring() {
		if (tranYSpring.isAtRest()) {
			tranYSpring.removeAllListeners();
			tranYSpring.setCurrentValue(0);
			tranYSpring.setEndValue(0);
			tranYSpring.addListener(mSpringListener);
		}
		if (tranXSpring.isAtRest()) {
			tranXSpring.removeAllListeners();
			tranXSpring.setCurrentValue(0);
			tranXSpring.setEndValue(0);
			tranXSpring.addListener(mSpringListener);
		}
		if (rotateSpring.isAtRest()) {
			rotateSpring.removeAllListeners();
			rotateSpring.setCurrentValue(0);
			rotateSpring.setEndValue(0);
			rotateSpring.addListener(mSpringListener);
		}
	}

	public void showNext() {
		if (dataSize>0) {
			resetSpring();
			animOutIfNeeded(mHeight, 0,-mWidth,0);
		}
	}
	
	public void showLast(){
		if (dataSize>0) {
			int p=mNextAdapterPosition-DEF_MAX_VISIBLE-1;
			if (p>=0) {
				topPosition=p;
				mNextAdapterPosition=p;
			}else{
				topPosition=dataSize-1;
				mNextAdapterPosition=dataSize-1;
			}
			if (mNextAdapterPosition<0||topPosition<0) {
				mNextAdapterPosition=0;
				topPosition=0;
			}
			Log.i("ip", "i-"+topPosition+",p-"+mNextAdapterPosition);
			viewHolder.clear();
			removeAllViews();
			backFull();
		}
	}

	private View getTopView() {
		if (getChildCount()>0) {
			return getChildAt(getChildCount() - 1);
		} else {
			return null;
		}
	}

	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		super.onLayout(arg0, arg1, arg2, arg3, arg4);
		View topView=getTopView();
		if (topView != null) {
			topView.setOnClickListener(listener);
		}
		mHeight = getHeight();
		mWidth = getWidth();
		OUT_DISTANCE_BOUDARY = mHeight / 3;
		OUT_DISTANCE_BOUDARX = mWidth / 3;
	}

	private void animOutIfNeeded(float scrollYDis, float velocityY,float scrollXDis, float velocityX) {
		float tranY = 0;
		float tranX = 0;
		if (velocityX > FLING_VELOCITY && (scrollXDis > OUT_DISTANCE_BOUDARX)) {
			if (getChildCount()>1) {
				tranX = mWidth;
				rotateSpring.setAtRest();
			} else {
				rotateSpring.setEndValue(0);
			}
		}else if (velocityX < -FLING_VELOCITY && (scrollXDis < -OUT_DISTANCE_BOUDARX)) {
			if (getChildCount()>1) {
				tranX = -mWidth;
				rotateSpring.setAtRest();
			} else {
				rotateSpring.setEndValue(0);
			}
		} else if (velocityY > FLING_VELOCITY && (scrollYDis > OUT_DISTANCE_BOUDARY)) {// 下移
            rotateSpring.setEndValue(0);
        } else if (velocityY < -FLING_VELOCITY &&(scrollYDis < -OUT_DISTANCE_BOUDARY)) { // 上移
            rotateSpring.setEndValue(0);
        }else {
			rotateSpring.setEndValue(0);
		}
        tranYSpring.setOvershootClampingEnabled(tranY!=0);
        tranXSpring.setOvershootClampingEnabled(tranX!=0);
		tranYSpring.setEndValue(tranY);
		tranXSpring.setEndValue(tranX);
	}

	private class ExampleSpringListener extends SimpleSpringListener {
		@Override
		public void onSpringUpdate(Spring spring) {
			View topView=getTopView();
			if (null==topView) {
				return;
			}
			float value = (float) spring.getCurrentValue();
			String springId = spring.getId();
			if (springId.equals(tranYSpring.getId())) {
                float v=value+itemTop;
                if (ViewHelper.getTranslationY(topView)!=v){
                    ViewHelper.setTranslationY(topView, value+itemTop);
                }
				if (spring.isAtRest()) {
					if (value >= mHeight) {//下
//						ViewHelper.setRotation(topView,0);
//						goDown();
					} else if (value <= -mHeight) {//上
//						ViewHelper.setRotation(topView,0);
//						goDown();
					}
				}
			} else if(springId.equals(tranXSpring.getId())){
                float v=value+itemLeft;
                if (ViewHelper.getTranslationX(topView)!=v){
                    ViewHelper.setTranslationX(topView, value+itemLeft);
                }
				if (spring.isAtRest()) {
                    if (value >= mWidth) {//右
						ViewHelper.setRotation(topView,0);
						goDown();
					} else if (value <= -mWidth) {//左
						ViewHelper.setRotation(topView,0);
						goDown();
					}
				}
			}else if(springId.equals(rotateSpring.getId())){
                if (ViewHelper.getRotation(topView)!=value){
                    ViewHelper.setRotation(topView,value);
                }
			}
		}
	}
	
	/**
	 * 下移所有视图
	 */
	private boolean goDown() {
		final View topView = getTopView();
		if(null==topView){
			return false;
		}
		removeView(topView);
		ensureFull();
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View view = getChildAt(i);
			float scaleX = ViewHelper.getScaleX(view)+ ((float) 1 / DEF_MAX_VISIBLE) * 0.2f;
			float tranlateY = ViewHelper.getTranslationY(view)+ itemSpace;
			if (i == count - 1) {
				bringToTop(view);
			}else {
				if ((count == DEF_MAX_VISIBLE && i != 0)|| count < DEF_MAX_VISIBLE) {
					ViewPropertyAnimator
							.animate(view).translationY(tranlateY)
							.setInterpolator(new AccelerateInterpolator())
							.setListener(null).scaleX(scaleX).setDuration(200);
				}
			}
		}
		if (null!=mListener) {
			int loadIndex=mListAdapter.getCount()-DEF_MAX_VISIBLE-1;
			if (topPosition==loadIndex) {
				mListener.onCardLoadMore();
			}
			mListener.onCardSelected(topPosition);
		}
		if (dataSize>0&&topPosition!=0&&topPosition%dataSize==0) {
			//循环到了第一个
		}
		return true;
	}

	private void bringToTop(final View view) {
		topPosition++;
		float scaleX = ViewHelper.getScaleX(view) + ((float) 1 / DEF_MAX_VISIBLE)* 0.2f;
		float tranlateY = ViewHelper.getTranslationY(view) + itemSpace;
		ViewPropertyAnimator.animate(view).translationY(tranlateY)
				.scaleX(scaleX).setDuration(200).alpha(1)
				.setInterpolator(new AccelerateInterpolator());
	}

	public static Rect getHitRect(Rect rect, View child) {
		rect.left = (int)(child.getLeft()+ViewHelper.getTranslationX(child));
		rect.right = (int)(child.getRight()+ViewHelper.getTranslationX(child));
		rect.top = (int)(child.getTop()+ViewHelper.getTranslationY(child));
		rect.bottom = (int)(child.getBottom()+ViewHelper.getTranslationY(child));
		return rect;
	}
	
	private float downY, downX,distanceY,distanceX;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (checkNull()) {
			return true;
		}
		if (vTracker == null) {
			vTracker = VelocityTracker.obtain();
		}
		vTracker.addMovement(event);
		float currentY = event.getY();
		float currentX = event.getX();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = event.getY();
			downX = event.getX();
			topRect = getHitRect(topRect, getTopView());
			if(!topRect.contains((int)downX, (int)downY)){
				return false;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			distanceY = currentY - downY;
			distanceX = currentX - downX;
			if(Math.abs(distanceY) > mTouchSlop||Math.abs(distanceX) > mTouchSlop){
				if (Math.abs(distanceY) > mTouchSlop) {
					tranYSpring.setEndValue(distanceY);
				}
				if (Math.abs(distanceX) > mTouchSlop) {
					tranXSpring.setEndValue(distanceX);
				}
				float degree = MAX_DEGREE * distanceY / mHeight;
				if (downX < mWidth / 2) {
					degree = -degree;
				}
				rotateSpring.setEndValue(degree);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if(Math.abs(distanceY) > mTouchSlop||Math.abs(distanceX) > mTouchSlop){
				final VelocityTracker tracker = vTracker;
				tracker.computeCurrentVelocity(UNIT);
				float velocityY = tracker.getYVelocity();
				float velocityX = tracker.getXVelocity();
				animOutIfNeeded(currentY - downY, velocityY,currentX - downX, velocityX);
				if (vTracker != null) {
					vTracker.recycle();
					vTracker = null;
				}
			}
			break;
		}
		return true;
	}
	
	private boolean checkNull(){
		return null==mListAdapter||null==topRect||null==getTopView();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (checkNull()) {
			return true;
		}
		if (vTracker == null) {
			vTracker = VelocityTracker.obtain();
		}
		vTracker.addMovement(event);
		float currentY = event.getY();
		float currentX = event.getX();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = event.getY();
			downX = event.getX();
			resetSpring();
			break;
		case MotionEvent.ACTION_MOVE:
			float distanceY = Math.abs(currentY - downY);
			float distanceX = Math.abs(currentX - downX);
			if(distanceY > mTouchSlop||distanceX>mTouchSlop){
				 //拦截，不向下传递
				return true;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			final VelocityTracker tracker = vTracker;
			tracker.computeCurrentVelocity(UNIT);
			float velocityY = tracker.getYVelocity();
			float velocityX = tracker.getXVelocity();
			if (vTracker != null) {
				vTracker.recycle();
				vTracker = null;
			}
			if(velocityY > FLING_VELOCITY||velocityX>FLING_VELOCITY){
				//拦截，不向下传递
				return true;
			}
			break;
		}
		return false;
	}
	
	private OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (null!=mListener){
				mListener.onClick(v);
			}
		}
	};
	
}
