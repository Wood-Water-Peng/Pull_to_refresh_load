# Pull_to_refresh_load

Welcome to follow me on GitHub or Blog

GitHub: https://github.com/Wood-Water-Peng

Blog:www.pengjiebest.com

---
## Pull To Refresh And Load More  

该项目是对<a href="https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh">该项目</a>的再次封装
添加了LoadMore模块

## Usage

**下拉刷新使用方法**

**Config in xml**

''
    <in.srain.cube.views.ptr.PtrFrameLayout
        android:id="@+id/store_house_ptr_frame"
        xmlns:cube_ptr="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        cube_ptr:ptr_resistance="1.7"
        cube_ptr:ptr_ratio_of_header_height_to_refresh="1.2"
        cube_ptr:ptr_duration_to_close="300"
        cube_ptr:ptr_duration_to_close_header="2000"
        cube_ptr:ptr_keep_header_when_refresh="true"
        cube_ptr:ptr_pull_to_fresh="false" >
        
        <LinearLayout
            android:id="@+id/store_house_ptr_image_content"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:background="@color/cube_mints_333333"
            android:clickable="true"
            android:padding="10dp">

            <in.srain.cube.image.CubeImageView
                android:id="@+id/store_house_ptr_image"
                android:layout_width="match_parent"
                android:layout_height="match_parent" />
        </LinearLayout>

    </in.srain.cube.views.ptr.PtrFrameLayout>
    
''
