package org.zenu.home;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;

/**
 * <pre>
 * reference by:
 *   SortableListViewLayout
 *   http://visible-true.blogspot.jp/2011/01/android22-gridviewlistview.html
 * 
 * getParams での設定で FLAG_LAYOUT_IN_SCREEN を追加すると
 * onTouchEventのタッチ位置(last_touch_pos_)にステータスバーが含まれない
 * その上でドラッグ開始時に子Viewの位置とタッチ位置の補正を行い(view_to_touch_gap_pos_)
 * 子Viewをそのままスライドするように見せかける
 * 
 * 課題:
 *   * ドラッグ開始時に開始元になる子ビューへ変化(色変更、アニメーション)を付けたい
 *     + 子View(getChildAt)への操作自体は簡単だが
 *       SDK内部でスクロール時にViewを使いまわしされる(?)ためドラッグしながらスクロールすると
 *       関係のない所にも影響してしまう
 *     + スクロールして領域外に消えたときに状態クリア、戻ったら状態追加しないといけない?
 *       setAdapterしたときにラッパークラスに差し替えてやり
 *       getViewをごにょごにょすればいけるかも??
 *   
 *   * ドラッグ中のビュー(getDragItem)に変化を付けたい
 *     + startAnimationをしても何も起こらない??　setAlphaやsetBackgroundColorは効く
 *       未解決のため何もせず
 *     + アニメーション決め打ちもいやなのでonItemDragで外部からsetAlphaさせている
 *       ちょっと面倒くさい
 * </pre>
 */
public class SortableGridView
	extends GridView
	implements OnItemLongClickListener
{
	public SortableGridView(Context context)
	{
		this(context, null);
	}

	public SortableGridView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		setOnItemLongClickListener(this);
	}

	private SortableAdapter adapter_;
	@Override
	public void setAdapter(ListAdapter adapter)
	{
		if(!(adapter instanceof SortableAdapter))
		{
			throw new RuntimeException("adapter not implements SortableAdapter");
		}
		
		adapter_ = (SortableAdapter) adapter;
		super.setAdapter(adapter);
	}

	private ImageView drag_image_;
	public ImageView getDragItem()
	{
		if(drag_image_ == null) {drag_image_ = new ImageView(getContext());}
		return(drag_image_);
	}

	private WindowManager window_manager_;
	public WindowManager getWindowManager()
	{
		if(window_manager_ == null) {window_manager_ = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);}
		return(window_manager_);
	}

	private WindowManager.LayoutParams layout_params_;
	public WindowManager.LayoutParams getParams()
	{
		if(layout_params_ == null)
		{
			layout_params_ = new WindowManager.LayoutParams();
			layout_params_.gravity = Gravity.TOP | Gravity.LEFT;

			layout_params_.height = WindowManager.LayoutParams.WRAP_CONTENT;
			layout_params_.width = WindowManager.LayoutParams.WRAP_CONTENT;
			layout_params_.flags =
					0
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
					| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
					| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
					| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
					;
			layout_params_.format = PixelFormat.TRANSLUCENT;
			layout_params_.windowAnimations = 0;
			layout_params_.x = 0;
			layout_params_.y = 0;
		}
		return(layout_params_);
	}

	private int drag_start_pos_ = -1;
	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int pos, long id)
	{
		drag_start_pos_ = pos;
		
		View convertView = getAdapter().getView(pos, view, null);
		convertView.setDrawingCacheEnabled(true);
		convertView.buildDrawingCache();
		getDragItem().setImageBitmap(convertView.getDrawingCache());
		moveDragItem(pos, last_touch_pos_, true);
		return(true);
	}

	public boolean isDragging()
	{
		return(drag_start_pos_ >= 0);
	}

	public void clearDragging()
	{
		drag_start_pos_ = -1;
	}

	private PointF last_touch_pos_ = new PointF();
	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		switch(ev.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			last_touch_pos_.x = ev.getX();
			last_touch_pos_.y = ev.getY();
			break;
		case MotionEvent.ACTION_UP:
			last_touch_pos_.x = ev.getX();
			last_touch_pos_.y = ev.getY();
			if(isDragging())
			{
				stopScroll();
				drop(drag_start_pos_, last_touch_pos_);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			last_touch_pos_.x = ev.getX();
			last_touch_pos_.y = ev.getY();
			if(isDragging())
			{
				moveDragItem(drag_start_pos_, last_touch_pos_, false);
				if(last_touch_pos_.y - getDragItem().getHeight() / 4 < 0)
				{
					isloop_ = true;
					startPrevScroll();
				}
				else if(last_touch_pos_.y + getDragItem().getHeight() / 4 > getHeight())
				{
					isloop_ = true;
					startNextScroll();
				}
				else
				{
					stopScroll();
				}
			}
			break;
		}
		return(super.onTouchEvent(ev));
	}

	private Point view_to_touch_gap_pos_ = new Point();
	public void moveDragItem(int from, PointF last_touch_pos, boolean isdrag_start)
	{
		if(isdrag_start)
		{
			View view = ((AdapterView<?>) this).getChildAt(from - getFirstVisiblePosition());
			int onscreen[] = new int[2];
			view.getLocationOnScreen(onscreen);
			
			view_to_touch_gap_pos_.x = (int) last_touch_pos.x - onscreen[0];
			view_to_touch_gap_pos_.y = (int) last_touch_pos.y - onscreen[1];
		}
		getParams().x = (int) last_touch_pos.x - view_to_touch_gap_pos_.x;
		getParams().y = (int) last_touch_pos.y - view_to_touch_gap_pos_.y;

		if(isdrag_start)
		{
			getWindowManager().addView(getDragItem(), getParams());
		}
		else
		{
			getWindowManager().updateViewLayout(getDragItem(), getParams());
		}
		
		if(OnItemDragListener_ != null)
		{
			int to = pointToPosition((int) last_touch_pos.x, (int) last_touch_pos.y);
			float x = 0;
			float y = 0;
			View view;
			if(to >= 0 && (view = getChildAt(to - getFirstVisiblePosition())) != null)
			{
				Rect frame = new Rect();
				view.getHitRect(frame);

				x = (last_touch_pos.x - view_to_touch_gap_pos_.x - frame.left) / frame.width();
				y = (last_touch_pos.y - view_to_touch_gap_pos_.y - frame.top) / frame.height();
			}
			OnItemDragListener_.onItemDrag(this, from, to, x, y, isdrag_start);
		}
	}

	public void drop(int from, PointF last_touch_pos)
	{
		getWindowManager().removeView(getDragItem());

		int to = pointToPosition((int) last_touch_pos.x, (int) last_touch_pos.y);
		boolean skip = false;
		if(OnItemDropListener_ != null)
		{
			skip = OnItemDropListener_.onItemDrop(this, from, to, last_touch_pos.x, last_touch_pos.y);
		}
		if(!skip)
		{
			if(to < 0)
			{
				adapter_.appendLast(from);
			}
			else
			{
				adapter_.shiftPosition(from, to);
			}
		}

		clearDragging();
		invalidateViews();
	}
	
	private OnItemDragListener OnItemDragListener_ = null;
	public void setOnItemDragListener(OnItemDragListener listener)
	{
		OnItemDragListener_ = listener;
	}
	
	private OnItemDropListener OnItemDropListener_ = null;
	public void setOnItemDropListener(OnItemDropListener listener)
	{
		OnItemDropListener_ = listener;
	}

	private boolean isloop_ = false;
	public void startPrevScroll()
	{
		getHandler().postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					smoothScrollBy(-100, 400);
					if(isloop_)
					{
						startPrevScroll();
					}
				}
			}, 200);
	}

	public void startNextScroll()
	{
		getHandler().postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					smoothScrollBy(100, 400);
					if(isloop_)
					{
						startNextScroll();
					}
				}
			}, 200);
	}

	public void stopScroll()
	{
		isloop_ = false;
	}
}
