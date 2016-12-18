package com.honjane.tagtextlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by honjane on 2015/10/14.
 */

public class TagTextView extends ViewGroup {


    private LayoutInflater mInflater;
    //与父view边距
    private int mViewBorder;
    //item之间边距
    private int mItemBorder;
    private int mTextColor;
    //item背景
    private int mTagBackground;
    private float mTextSize;
    private int mWidthSize;
    private int mHeightSize;
    private int mArrowIconWidth;
    private int mImageHeight;
    private int mArrowResId;
    private int mMoreTextWidth;
    private String mMoreTextStr;

    private boolean mSingleLine;
    private boolean mShowArrow;
    private boolean mShowMore;

    private ImageView mArrowIv;
    private TextView mMoreTextTv;
    private List<String> mTags;
    private ITagClickListener mTagClickListener;


    public TagTextView(Context context) {
        this(context, null);
    }

    public TagTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //1 初始化自定义属性
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TagTextViewStyle, defStyleAttr, defStyleAttr);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.TagTextViewStyle_t_textSize, 12);
        mTextColor = typedArray.getColor(R.styleable.TagTextViewStyle_t_textColor, Color.BLACK);
        mTagBackground = typedArray.getResourceId(R.styleable.TagTextViewStyle_t_tagBackground, R.drawable.bg_item);
        mViewBorder = typedArray.getDimensionPixelSize(R.styleable.TagTextViewStyle_t_viewBorder, 5);
        mItemBorder = typedArray.getDimensionPixelSize(R.styleable.TagTextViewStyle_t_itemBorder, 5);

        mSingleLine = typedArray.getBoolean(R.styleable.TagTextViewStyle_t_singleLine, false);
        mArrowIconWidth = typedArray.getDimensionPixelSize(R.styleable.TagTextViewStyle_t_imageWidth, 10);
        mImageHeight = typedArray.getDimensionPixelSize(R.styleable.TagTextViewStyle_t_imageHeight, 10);
        mArrowResId = typedArray.getInteger(R.styleable.TagTextViewStyle_t_rightArrow, R.mipmap.arrow_right);
        mShowArrow = typedArray.getBoolean(R.styleable.TagTextViewStyle_t_showArrow, false);
        mShowMore = typedArray.getBoolean(R.styleable.TagTextViewStyle_t_showMore, false);
        mMoreTextStr = typedArray.getString(R.styleable.TagTextViewStyle_t_moreTextStr);
        mMoreTextWidth = typedArray.getDimensionPixelSize(R.styleable.TagTextViewStyle_t_moreTextWidth, 60);
        typedArray.recycle();
        mInflater = LayoutInflater.from(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //计算 ViewGroup 上级容器为其推荐的宽高
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        mWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        mHeightSize = MeasureSpec.getSize(heightMeasureSpec);

        //计算子view
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        initSingleLineView(widthMeasureSpec, heightMeasureSpec);
        //计算tag view 实际需要高度
        int totalWidth = 0;
        int totalHeight = mViewBorder;
        if (mSingleLine) {
            totalHeight = getSingleTotalHeight(totalWidth, totalHeight);
        } else {
            totalHeight = getMultiTotalHeight(totalWidth, totalHeight);
        }
        //根据高度设置
        setMeasuredDimension(mWidthSize, heightMode == MeasureSpec.EXACTLY ? mHeightSize : totalHeight);

    }

    private void initSingleLineView(int widthMeasureSpec, int heightMeasureSpec) {
        if (!mSingleLine) {
            return;
        }
        if (mShowArrow) {
            mArrowIv = new ImageView(getContext());
            mArrowIv.setImageResource(mArrowResId);
            mArrowIv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            mArrowIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            measureChild(mArrowIv, widthMeasureSpec, heightMeasureSpec);
            mArrowIconWidth = mArrowIv.getMeasuredWidth();
            mImageHeight = mArrowIv.getMeasuredHeight();
            addView(mArrowIv);
        }

        if (mShowMore) {
            mMoreTextTv = (TextView) mInflater.inflate(R.layout.layout_item, null);
            mMoreTextTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
            mMoreTextTv.setTextColor(mTextColor);
            mMoreTextTv.setBackgroundResource(mTagBackground);
            @SuppressLint("DrawAllocation")
            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            mMoreTextTv.setLayoutParams(layoutParams);
            mMoreTextTv.setText(mMoreTextStr == null || mMoreTextStr.equals("") ? "..." : mMoreTextStr);
            measureChild(mMoreTextTv, widthMeasureSpec, heightMeasureSpec);
            mMoreTextWidth = mMoreTextTv.getMeasuredWidth();
            mMoreTextTv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mTagClickListener == null) {
                        return;
                    }
                    mTagClickListener.onMoreClick();
                }
            });
            addView(mMoreTextTv);
        }

    }

    private int getSingleTotalHeight(int totalWidth, int totalHeight) {
        int childWidth;
        int childHeight = 0;

        totalWidth += mViewBorder;

        int textTotalWidth = getTextTotalWidth();
        //items 总宽度小于 viewgroup - 箭头图片width 不用显示更多item
        if (textTotalWidth < mWidthSize - mArrowIconWidth) {
            mMoreTextStr = null;
            mMoreTextWidth = 0;
        }
        //计算item中宽度并确定布局
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            childWidth = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();

            if (i == 0) {
                totalWidth += childWidth;
                totalHeight = childHeight + mViewBorder;
            } else {
                totalWidth += childWidth + mItemBorder;
            }

            if (totalWidth + mItemBorder + mViewBorder + mViewBorder + mMoreTextWidth + mArrowIconWidth < mWidthSize) {
                child.layout(
                        totalWidth - childWidth + mItemBorder,
                        totalHeight - childHeight,
                        totalWidth + mItemBorder,
                        totalHeight);
            } else {
                totalWidth -= childWidth + mViewBorder;
                break;
            }
        }
        //更多item
        if (mMoreTextTv != null) {
            mMoreTextTv.layout(
                    totalWidth + mViewBorder + mItemBorder,
                    totalHeight - childHeight,
                    totalWidth + mViewBorder + mItemBorder + mMoreTextWidth,
                    totalHeight);
        }

        totalHeight += mViewBorder;
        //箭头
        if (mArrowIv != null) {
            mArrowIv.layout(
                    mWidthSize - mArrowIconWidth - mViewBorder,
                    (totalHeight - mImageHeight) / 2,
                    mWidthSize - mViewBorder,
                    (totalHeight - mImageHeight) / 2 + mImageHeight);
        }

        return totalHeight;
    }

    private int getTextTotalWidth() {
        if (getChildCount() == 0) {
            return 0;
        }
        int totalChildWidth = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            totalChildWidth += child.getMeasuredWidth() + mViewBorder;
        }
        return totalChildWidth + mItemBorder * 2;
    }

    private int getMultiTotalHeight(int totalWidth, int totalHeight) {
        int childWidth;
        int childHeight;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            childWidth = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();
            totalWidth += childWidth + mItemBorder;

            //设置第一行高度 因为layout t = totalHeight - childHeight
            //要让totalHeight - childHeight不为负数 所以先设置一行高度
            if (i == 0) {
                totalHeight = childHeight + mItemBorder;
            }
            if (totalWidth + mItemBorder + mViewBorder > mWidthSize) {
                //items 宽度超过ViewGroup宽度 需要换行显示
                totalWidth = mItemBorder;
                //高度 = 原高度 + item高度 + item间距
                totalHeight += childHeight + mItemBorder;
                child.layout(
                        totalWidth + mViewBorder,
                        totalHeight - childHeight,
                        totalWidth + childWidth + mViewBorder,
                        totalHeight);
                totalWidth += childWidth;
            } else {
                //横排：起始 间隔viewboder距离开始，到总的width（items+item间距）+view间距
                //竖排：起始 离顶部viewboder间距开始，到总的height
                child.layout(totalWidth - childWidth + mViewBorder,
                        totalHeight - childHeight,
                        totalWidth + mViewBorder,
                        totalHeight);
            }
        }

        return totalHeight + mItemBorder;
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

    }


    public void setTags(List<String> tagDatas) {
        if (tagDatas == null) {
            return;
        }
        mTags = tagDatas;
        removeAllViews();
        String tag;
        for (int i = 0; i < mTags.size(); i++) {
            tag = tagDatas.get(i);
            TextView tagView = (TextView) mInflater.inflate(R.layout.layout_item, null);
            tagView.setText(tag);
            tagView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
            tagView.setBackgroundResource(mTagBackground);
            tagView.setTextColor(mTextColor);
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            tagView.setLayoutParams(params);
            addView(tagView);
            final int position = i;
            tagView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mTagClickListener == null) {
                        return;
                    }
                    mTagClickListener.onTagClick(position);
                }
            });
        }

        postInvalidate();
    }

    public void setSingleLine(boolean singleLine) {
        mSingleLine = singleLine;
        setTags(mTags);
    }

    public interface ITagClickListener {
        void onTagClick(int position);

        void onMoreClick();
    }

    public void setTagClickListener(ITagClickListener listener) {
        mTagClickListener = listener;
    }
}
