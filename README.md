# Pull_to_refresh_load

Welcome to follow me on GitHub or Blog

GitHub: https://github.com/Wood-Water-Peng

Blog:www.pengjiebest.com

---
## Pull To Refresh And Load More  

该项目是对<a href="https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh">Ultra-Pull-To-Refresh</a>的再次封装
添加了LoadMore模块

## Usage

**下拉刷新使用方法**

**Config in xml**

    <com.example.pj.ptr_lib.container.PtrClassicContainer
        android:id="@+id/container"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
    >
    <ListView
        android:id="@+id/content"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
    </ListView>
   
    </com.example.pj.ptr_lib.container.PtrClassicContainer>
    

## Process Refresh

`PtrHandler` 专门处理刷新操作


    public interface PtrHandler {
            /**
             *该方法在刷新时被回调
             */
            void onRefreshBegin();
    }
    
An example:  


     mPtrContainer.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin() {
                mPtrContainer.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPtrContainer.refreshComplete();
                    }
                }, 1000);
            }
        });

## Process Load More

`OnLoadListener`专门处理加载操作
    
      public interface OnLoadListener {
        void onLoad();
    }

An example:

        mPtrContainer.setOnLoadListener(new PtrContainer.OnLoadListener() {
            @Override
            public void onLoad() {
                mPtrContainer.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //load data
                        mAdapter.notifyDataSetChanged();
                        mPtrContainer.loadMoreCompleted();
                    }
                }, 1000);
            }
        });
    
    
## Q&A
* ViewPager的滑动冲突解决：

## Continue
*  给PtrContainer增加自定义属性
*  增加自定义头部
*  增加自定义尾部


        
