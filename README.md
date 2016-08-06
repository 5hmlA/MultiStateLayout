# MultiStateLayout
[![License](https://img.shields.io/badge/license-Apache%202-green.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0)
[![](https://jitpack.io/v/mychoices/MultiStateLayout.svg)](https://jitpack.io/#mychoices/MultiStateLayout)

#Gradle
```
allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
	
dependencies {
	 compile 'com.github.mychoices:MultiStateLayout:v1.1'
}
```
![](https://raw.githubusercontent.com/mychoices/MultiStateLayout/master/001.gif)
#自定义属性
```
<attr name="error" format="reference"/> 自定义 error布局
<attr name="loading" format="reference"/>	自定义loading布局
<attr name="empty" format="reference"/>	自定义empty布局
<attr name="state" format="enum">	默认状态
    <enum name="loading" value="0"/>
    <enum name="error" value="1"/>
    <enum name="empty" value="2"/>
    <enum name="except" value="3"/>
</attr>
```
##MultiStateLayout主要包括四种状态
- loading
- empty
- error
- except

#User Guide
```
<jonas.jlayout.MultiStateLayout
        android:layout_width="match_parent"
		app:empty="@layout/custom_empty"
		app:loading="@layout/custom_loading"
		app:error="@layout/custom_error"
		app:state="loading"
        android:layout_height="match_parent">
	 
      <   要展示的布局    >

</jonas.jlayout.MultiStateLayout>
```
###加载完数据后
- showStateLayout(MultiStateLayout.LayoutState.STATE_ERROR);
- showStateLayout(MultiStateLayout.LayoutState.STATE_EMPTY);
- showStateLayout(MultiStateLayout.LayoutState.STATE_EXCEPT);

####*setLoadingCancelAble(true); 加载状态 点击取消*
####*setRevealable(true);  开启reveal效果*
####*CustomStateLayout(View view, @LayoutState int state) 自定义 状态布局*


# License

    Copyright 2016 Yun

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
