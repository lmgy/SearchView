# SearchView

[![](https://jitpack.io/v/lmgy/SearchView.svg)](https://jitpack.io/#lmgy/SearchView)
[![API](https://img.shields.io/badge/API-16%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=16)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a6e5290665204149bd3662517c256db6)](https://www.codacy.com/manual/lmgy/SearchView?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=lmgy/SearchView&amp;utm_campaign=Badge_Grade)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Flmgy%2FSearchView.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Flmgy%2FSearchView?ref=badge_shield)

## Install

Step 1. Add the JitPack repository to your build file

``` groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add the dependency

``` groovy
dependencies {
	implementation 'com.github.lmgy:SearchView:1.0.0'
}
```

## Usage

* Add SearchView to your layout file with Toolbar

``` xml
<FrameLayout
    android:id="@+id/toolbar_container"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <androidx.appcompat.widget.Toolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="?attr/colorPrimary"
        app:titleTextColor="@android:color/white" />

    <com.lmgy.searchview.SearchView
        android:id="@+id/searchView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="This is hint" />

</FrameLayout>
```

* Add the search item into the menu file

``` xml
<item
    android:id="@+id/action_search"
    android:icon="@drawable/ic_action_action_search"
    android:orderInCategory="100"
    android:title="search"
    app:showAsAction="always" />
```

* Define it in the onCreateOptionsMenu

``` kotlin
override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_search_view, menu)
    val item = menu?.findItem(R.id.action_search)
    searchView.setMenuItem(item)
    return true
}
```

* Set the listener

``` kotlin
searchView.setOnQueryTextListener(object : OnQueryTextListener{
    override fun onQueryTextChange(newText: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
})
        
searchView.setOnSearchViewListener(object : SearchView.SearchViewListener{
    override fun onSearchViewClosed() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSearchViewShown() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
})
```

## Requirements

* Minimum Android version: >= 4.1 (API 16)

## License

Copyright (C) 2018 lmgy

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
